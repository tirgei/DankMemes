package com.gelostech.dankmemes.data.repositories

import android.net.Uri
import com.gelostech.dankmemes.data.Result
import com.gelostech.dankmemes.data.models.Fave
import com.gelostech.dankmemes.data.models.Meme
import com.gelostech.dankmemes.data.models.Report
import com.gelostech.dankmemes.data.wrappers.ObservableMeme
import com.gelostech.dankmemes.ui.callbacks.StorageUploadListener
import com.gelostech.dankmemes.utils.AppUtils
import com.gelostech.dankmemes.utils.Constants
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.StorageReference
import io.reactivex.Observable
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import kotlin.Exception

class MemesRepository constructor(private val firestoreDatabase: FirebaseFirestore,
                                  private val firebaseDatabase: DatabaseReference,
                                  private val storageReference: StorageReference) {

    private val db = firestoreDatabase.collection(Constants.MEMES)
    private val memesQuery = db.orderBy(Constants.TIME, Query.Direction.DESCENDING).limit(Constants.MEMES_COUNT)

    /**
     * Fetch all memes
     */
    suspend fun fetchMemes(loadBefore: String? = null, loadAfter: String? = null): List<ObservableMeme> {
        Timber.e("Fetching memes...")
        var query: Query = memesQuery

        loadBefore?.let {
            val meme = db.document(it).get().await()
            query = memesQuery.endBefore(meme)
        }

        loadAfter?.let {
            val meme = db.document(it).get().await()
            query = memesQuery.startAfter(meme)
        }

        return query.get().await().map { ObservableMeme(it.id, getObservableMeme(it.id)) }
    }

    /**
     * Fetch all memes by user
     * @param userId - ID of the User
     */
    suspend fun fetchMemesByUser(userId: String, loadBefore: String? = null, loadAfter: String? = null): List<ObservableMeme> {
        var query: Query = memesQuery

        loadBefore?.let {
            val meme = db.document(it).get().await()
            query = memesQuery.endBefore(meme)
        }

        loadAfter?.let {
            val meme = db.document(it).get().await()
            query = memesQuery.startAfter(meme)
        }

        return query.whereEqualTo(Constants.POSTER_ID, userId).get().await()
                .map { ObservableMeme(it.id, getObservableMeme(it.id)) }
    }


    /**
     * Create an observable meme object
     * @param memeId - ID of the meme
     */
    private fun getObservableMeme(memeId: String): Observable<Meme> = Observable.create<Meme> { emitter ->
        db.document(memeId)
                .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null)
                        emitter.onError(firebaseFirestoreException)
                    else if (documentSnapshot != null && documentSnapshot.exists())
                        emitter.onNext(documentSnapshot.toObject(Meme::class.java)!!)
                    else
                        emitter.onError(Throwable("Meme not found"))
                }
    }

    /**
     * Function to post a new Meme
     * @param imageUri - Selected Meme
     * @param meme - Meme model
     * @param callback - Callback response
     */
    fun postMeme(imageUri: Uri, meme: Meme, callback: (Result<Boolean>) -> Unit) {
        val id = db.document().id
        val storageDb = storageReference.child(Constants.MEMES).child(meme.memePosterID!!).child(id)
        val errorMessage = "Error posting meme. Please try again"

        AppUtils.uploadFileToFirebaseStorage(storageDb, imageUri, object : StorageUploadListener {
            override fun onFileUploaded(downloadUrl: String?) {
                GlobalScope.launch {
                    if (downloadUrl.isNullOrEmpty()) {
                        Timber.e("Meme not uploaded...")
                        callback(Result.Error(errorMessage))
                    } else {
                        Timber.e("Meme uploaded...")

                        try {
                            meme.apply {
                                this.id = id
                                this.imageUrl = downloadUrl
                            }

                            db.document(id).set(meme).await()
                            callback(Result.Success(true))

                        } catch (e: Exception) {
                            Timber.e("Error posting meme: ${e.localizedMessage}")
                            callback(Result.Error(errorMessage))
                        }
                    }
                }
            }
        })
    }

    /**
     * Function to delete meme
     * @param memeId - ID of the meme to delete
     */
    suspend fun deleteMeme(memeId: String): Result<Boolean> {
        return try {
            db.document(memeId).delete().await()
            Result.Success(true)
        } catch (e: Exception) {
            Timber.e("Error deleting meme: ${e.localizedMessage}")
            Result.Error("Error deleting meme")
        }
    }

    /**
     * Function to (un)like meme
     * @param memeId - ID of the meme
     * @param userId - ID of the logged in user
     */
    suspend fun likeMeme(memeId: String, userId: String): Result<Boolean> {
        val memeReference = db.document(memeId)

        val result = firestoreDatabase.runTransaction {
            val meme = it[memeReference].toObject(Meme::class.java)
            val likes = meme!!.likes
            var likesCount = meme.likesCount

            if (likes.containsKey(userId)) {
                likesCount -= 1
                likes.remove(userId)

            } else {
                likesCount += 1
                likes[userId] = true
            }

            it.update(memeReference, Constants.LIKES, likes)
            it.update(memeReference, Constants.LIKES_COUNT, likesCount)

        }.await()

        if (result != null) {
            return Result.Success(true)
        }

        return Result.Error("Error liking meme. Please try again")
    }

    /**
     * Function to (un)fave meme
     * @param memeId - ID of the meme
     * @param userId - ID of the logged in User
     */
    suspend fun faveMeme(memeId: String, userId: String): Result<Boolean> {
        val memeReference = db.document(memeId)

        val result = firestoreDatabase.runTransaction {
            val meme = it[memeReference].toObject(Meme::class.java)
            val faves = meme!!.faves

            if (faves.containsKey(userId)) {
                faves.remove(userId)

                firestoreDatabase.collection(Constants.FAVORITES)
                        .document(userId)
                        .collection(Constants.USER_FAVES)
                        .document(memeId)
                        .delete()

            } else {
                faves[userId] = true

                val fave = Fave()
                fave.id = meme.id!!
                fave.imageUrl = meme.imageUrl!!
                fave.time = meme.time!!

                firestoreDatabase.collection(Constants.FAVORITES)
                        .document(userId)
                        .collection(Constants.USER_FAVES)
                        .document(memeId)
                        .set(fave)
            }

            it.update(memeReference, Constants.FAVES, faves)
        }.await()

        if (result != null) {
            return Result.Success(true)
        }

        return Result.Error("Error faving meme. Please try again")
    }

    fun reportMeme(report: Report, onResult: (Result<Boolean>) -> Unit) {
        val reportsDb = firebaseDatabase.child(Constants.REPORTS)

        val id = reportsDb.push().key
        report.apply { this.id = id }

        reportsDb.child(id!!).setValue(report)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) onResult(Result.Success(true))
                    else onResult(Result.Error("Error reporting meme. Please try again"))
                }
    }

}