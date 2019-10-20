package com.gelostech.dankmemes.data.repositories

import android.net.Uri
import com.gelostech.dankmemes.data.models.Fave
import com.gelostech.dankmemes.data.models.Meme
import com.gelostech.dankmemes.data.models.Report
import com.gelostech.dankmemes.utils.Constants
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.StorageReference

class MemesRepository constructor(private val firestoreDatabase: FirebaseFirestore,
                                  private val firebaseDatabase: DatabaseReference,
                                  private val storageReference: StorageReference ) {

    private val db = firestoreDatabase.collection(Constants.MEMES)

    private val initialQuery = db.orderBy(Constants.TIME, Query.Direction.DESCENDING).limit(Constants.MEMES_COUNT)
    private var nextQuery: Query? = null

    fun fetchMemes(onSuccess: (List<Meme>) -> Unit) {
        if (nextQuery != null) {
            nextQuery!!.get()
                    .addOnSuccessListener { querySnapshot ->
                        onSuccess(handleFetchedData(querySnapshot))
                    }
        } else {
            initialQuery.get()
                    .addOnSuccessListener { querySnapshot ->
                        onSuccess(handleFetchedData(querySnapshot))
                    }
        }
    }

    fun fetchMemesByUser(userId: String, onSuccess: (List<Meme>) -> Unit) {
        if (nextQuery != null) {
            nextQuery!!.whereEqualTo(Constants.POSTER_ID, userId).get()
                    .addOnSuccessListener { querySnapshot ->
                        onSuccess(handleFetchedData(querySnapshot))
                    }
        } else {
            initialQuery.whereEqualTo(Constants.POSTER_ID, userId).get()
                    .addOnSuccessListener { querySnapshot ->
                        onSuccess(handleFetchedData(querySnapshot))
                    }
        }
    }

    private fun handleFetchedData(querySnapshot: QuerySnapshot): List<Meme> {
        val lastFetchedMeme = querySnapshot.documents[querySnapshot.size()-1]
        nextQuery = initialQuery.startAfter(lastFetchedMeme)

        return querySnapshot.map { snapshot ->
            snapshot.toObject(Meme::class.java)
        }
    }

    fun postMeme(imageUri: Uri, meme: Meme, posted: (Boolean) -> Unit) {
        val id = db.document().id
        val storageDb = storageReference.child(Constants.MEMES).child(meme.memePosterID!!).child(id)

        val uploadTask = storageDb.putFile(imageUri)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                posted(false)
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
                            posted(postingTask.isSuccessful)
                        }

            } else posted (false)
        }
    }

    fun deleteMeme(memeId: String, deleted: (Boolean) -> Unit) {
        db.document(memeId).delete()
                .addOnCompleteListener { task ->
                    deleted(task.isSuccessful)
                }
    }

    fun likeMeme(memeId: String, userId: String, liked: (Boolean) -> Unit) {
        val memeReference = db.document(memeId)

        firestoreDatabase.runTransaction {
            val meme =  it[memeReference].toObject(Meme::class.java)
            val likes = meme!!.likes
            var likesCount = meme.likesCount

            if (likes.containsKey(userId)) {
                likesCount -= 1
                likes.remove(userId)

            } else  {
                likesCount += 1
                likes[userId] = true
            }

            it.update(memeReference, Constants.LIKES, likes)
            it.update(memeReference, Constants.LIKES_COUNT, likesCount)

        }.addOnCompleteListener { task ->
            liked(task.isSuccessful)
        }
    }


    fun faveMeme(memeId: String, userId: String, faved: (Boolean) -> Unit) {
        val memeReference = db.document(memeId)

        firestoreDatabase.runTransaction {
            val meme =  it[memeReference].toObject(Meme::class.java)
            val faves = meme!!.faves

            if (faves.containsKey(userId)) {
                faves.remove(userId)

                firestoreDatabase.collection(Constants.FAVORITES)
                        .document(userId)
                        .collection(Constants.USER_FAVES)
                        .document(memeId)
                        .delete()

            } else  {
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
            faved(task.isSuccessful)
        }
    }

    fun reportMeme(report: Report, reported: (Boolean) -> Unit) {
        val reportsDb = firebaseDatabase.child(Constants.REPORTS)

        val id = reportsDb.push().key
        report.apply { this.id = id }

        reportsDb.child(id!!).setValue(report)
                .addOnCompleteListener { task ->
                    reported(task.isSuccessful)
                }
    }

}