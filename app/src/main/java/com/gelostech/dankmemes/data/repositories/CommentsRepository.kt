package com.gelostech.dankmemes.data.repositories

import com.gelostech.dankmemes.data.Result
import com.gelostech.dankmemes.data.models.Comment
import com.gelostech.dankmemes.data.models.Meme
import com.gelostech.dankmemes.utils.Constants
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber


class CommentsRepository constructor(private val firebaseDatabase: DatabaseReference,
                                     private val firestoreDatabase: FirebaseFirestore) {

    private val db = firebaseDatabase.child(Constants.COMMENTS)
    private val memesDb = firestoreDatabase.collection(Constants.MEMES)

    /**
     * Funtion to post new comment
     * @param comment - The comment model
     */
    suspend fun postComment(comment: Comment): Result<Boolean> {
        val id = db.push().key
        val memeId = comment.picKey!!

        comment.apply {
            this.commentKey = id
        }

        return try {
            db.child(memeId).child(id!!).setValue(comment).await()
            updateCommentsCount(memeId, true)
            Result.Success(true)

        } catch (e: Exception) {
            Timber.e("Error posting comment: ${e.localizedMessage}")
            Result.Error("Error posting comment")
        }
    }

    /**
     * Function to delete comment
     * @param memeId - ID of the meme
     * @param commentId - ID of the comment
     */
    suspend fun deleteComment(memeId: String, commentId: String): Result<Boolean> {
        return try {
            db.child(memeId).child(commentId).removeValue().await()
            updateCommentsCount(memeId, false)
            Result.Success(true)

        } catch (e: java.lang.Exception) {
            Timber.e("Error deleting comment: ${e.localizedMessage}")
            Result.Error("Error deleting comment")
        }
    }

    /**
     * Function to update comments count on meme model
     * @param memeId - ID of the meme
     * @param add - Value to check whether add/minus operation
     */
    private suspend fun updateCommentsCount(memeId: String, add: Boolean) {
        val memeRef = memesDb.document(memeId)

        firestoreDatabase.runTransaction {
            val meme =  it[memeRef].toObject(Meme::class.java)
            var comments = meme!!.commentsCount

            if (add) {
                comments += 1
            } else {
                comments -= 1
            }

            it.update(memeRef, Constants.COMMENTS_COUNT, comments)
        }.await()
    }

}