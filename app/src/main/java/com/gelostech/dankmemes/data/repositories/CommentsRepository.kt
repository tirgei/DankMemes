package com.gelostech.dankmemes.data.repositories

import com.gelostech.dankmemes.data.Result
import com.gelostech.dankmemes.data.models.Comment
import com.gelostech.dankmemes.data.models.Meme
import com.gelostech.dankmemes.data.wrappers.ObservableComment
import com.gelostech.dankmemes.utils.Constants
import com.gelostech.dankmemes.utils.get
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.reactivex.Observable
import kotlinx.coroutines.tasks.await
import timber.log.Timber


class CommentsRepository constructor(private val firestoreDatabase: FirebaseFirestore) {

    private val db = firestoreDatabase.collection(Constants.COMMENTS)
    private val memesDb = firestoreDatabase.collection(Constants.MEMES)

    /**
     * Function to post new comment
     * @param comment - The comment model
     */
    suspend fun postComment(comment: Comment): Result<Boolean> {
        val id = db.document().id
        val memeId = comment.memeId!!

        comment.apply {
            this.commentId = id
        }

        return try {
            db.document(memeId).collection(Constants.MEME_COMMENTS).document(id).set(comment).await()
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
            db.document(memeId).collection(Constants.MEME_COMMENTS).document(commentId).delete().await()
            updateCommentsCount(memeId, false)
            Result.Success(true)

        } catch (e: java.lang.Exception) {
            Timber.e("Error deleting comment: ${e.localizedMessage}")
            Result.Error("Error deleting comment")
        }
    }

    /**
     * Function to fetch all comments for a Meme
     * @param memeId - ID of the meme
     */
    suspend fun fetchComments(memeId: String): List<ObservableComment> {
        val query = db.document(memeId).collection(Constants.MEME_COMMENTS)
                .orderBy(Constants.TIME, Query.Direction.ASCENDING)

        return query.get().await().map { ObservableComment(it.id, getObservableComment(memeId, it.id)) }
    }

    /**
     * Create an observable comment object
     * @param commentId - ID of the comment
     */
    private fun getObservableComment(memeId: String, commentId: String): Observable<Comment> = Observable.create<Comment> { emitter ->
        db.document(memeId).collection(Constants.MEME_COMMENTS).document(commentId)
                .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null)
                        emitter.onError(firebaseFirestoreException)
                    else if (documentSnapshot != null && documentSnapshot.exists())
                        emitter.onNext(documentSnapshot.toObject(Comment::class.java)!!)
                    else
                        emitter.onError(Throwable("Comment not found"))
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