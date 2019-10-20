package com.gelostech.dankmemes.data.repositories

import com.gelostech.dankmemes.data.Result
import com.gelostech.dankmemes.data.models.Notification
import com.gelostech.dankmemes.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class NotificationsRepository constructor(private val firestoreDatabase: FirebaseFirestore) {
    private val userId: String = ""
    private val db = firestoreDatabase
            .collection(Constants.NOTIFICATIONS)
            .document(userId)
            .collection(Constants.USER_NOTIFS)

    private val initialQuery = db.orderBy(Constants.TIME, Query.Direction.DESCENDING).limit(Constants.MEMES_COUNT)
    private var nextQuery: Query? = null

    fun fetchNotifications(onResult: (Result<List<Notification>>) -> Unit) {
        if (nextQuery != null) {
            nextQuery!!.get()
                    .addOnSuccessListener { querySnapshot ->
                        val notifications = handleFetchedData(querySnapshot)
                        onResult(Result.Success(notifications))
                    }
                    .addOnFailureListener { onResult(Result.Error("Error loading notifications")) }
        } else {
            initialQuery.get()
                    .addOnSuccessListener { querySnapshot ->
                        val notifications = handleFetchedData(querySnapshot)
                        onResult(Result.Success(notifications))
                    }
                    .addOnFailureListener { onResult(Result.Error("Error loading notifications")) }
        }
    }

    private fun handleFetchedData(querySnapshot: QuerySnapshot): List<Notification> {
        val lastFetchedMeme = querySnapshot.documents[querySnapshot.size()-1]
        nextQuery = initialQuery.startAfter(lastFetchedMeme)

        return querySnapshot.map { snapshot ->
            snapshot.toObject(Notification::class.java)
        }
    }

}