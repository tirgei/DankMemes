package com.gelostech.dankmemes.data.repositories

import android.net.Uri
import com.gelostech.dankmemes.data.Result
import com.gelostech.dankmemes.data.models.Fave
import com.gelostech.dankmemes.data.models.Meme
import com.gelostech.dankmemes.data.models.Report
import com.gelostech.dankmemes.data.wrappers.ObservableMeme
import com.gelostech.dankmemes.utils.Constants
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.StorageReference
import io.reactivex.Observable
import kotlinx.coroutines.tasks.await

class MemesRepository constructor(private val firestoreDatabase: FirebaseFirestore,
                                  private val firebaseDatabase: DatabaseReference,
                                  private val storageReference: StorageReference) {

    private val db = firestoreDatabase.collection(Constants.MEMES)
    private val memesQuery = db.orderBy(Constants.TIME, Query.Direction.DESCENDING).limit(Constants.MEMES_COUNT)

    /**
     * Fetch all memes
     */
    suspend fun fetchMemes(loadBefore: String? = null, loadAfter: String? = null): List<ObservableMeme> {
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

    fun postMeme(imageUri: Uri, meme: Meme, onResult: (Result<Boolean>) -> Unit) {
        val id = db.document().id
        val storageDb = storageReference.child(Constants.MEMES).child(meme.memePosterID!!).child(id)

        val uploadTask = storageDb.putFile(imageUri)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                onResult(Result.Error("Error posting meme. Please try again"))
            }

            // Continue with the task to get the download URL
            storageDb.downloadUrl

        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                meme.apply {
                    this.id = id
                    this.imageUrl = task.result.toString()
                }

                db.document(id).set(meme)
                        .addOnCompleteListener { postingTask ->
                            onResult(Result.Success(postingTask.isSuccessful))
                        }

            } else onResult(Result.Error("Error posting meme. Please try again"))
        }
    }

    fun deleteMeme(memeId: String, onResult: (Result<Boolean>) -> Unit) {
        db.document(memeId).delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) onResult(Result.Success(true))
                    else onResult(Result.Error("Error deleting meme. Please try again"))
                }
    }

    fun likeMeme(memeId: String, userId: String, onResult: (Result<Boolean>) -> Unit) {
        val memeReference = db.document(memeId)

        firestoreDatabase.runTransaction {
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

        }.addOnCompleteListener { task ->
            if (task.isSuccessful) onResult(Result.Success(true))
            else onResult(Result.Error("Error liking meme. Please try again"))
        }
    }


    fun faveMeme(memeId: String, userId: String, onResult: (Result<Boolean>) -> Unit) {
        val memeReference = db.document(memeId)

        firestoreDatabase.runTransaction {
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

        }.addOnCompleteListener { task ->
            if (task.isSuccessful) onResult(Result.Success(true))
            else onResult(Result.Error("Error favoring meme. Please try again"))
        }
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