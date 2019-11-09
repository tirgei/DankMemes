package com.gelostech.dankmemes.data.repositories

import com.gelostech.dankmemes.data.models.Notification
import com.gelostech.dankmemes.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class NotificationsRepository constructor(private val firestoreDatabase: FirebaseFirestore) {

    /**
     * Fetch all notifications
     */
    suspend fun fetchNotifications(userId: String, loadBefore: String? = null, loadAfter: String? = null): List<Notification> {
        Timber.e("Fetching memes...")
        val db = firestoreDatabase.collection(Constants.NOTIFICATIONS).document(userId).collection(Constants.USER_NOTIFS)
        var query = db.orderBy(Constants.TIME, Query.Direction.DESCENDING).limit(Constants.MEMES_COUNT)

        loadBefore?.let {
            val notif = db.document(it).get().await()
            query = query.endBefore(notif)
        }

        loadAfter?.let {
            val notif = db.document(it).get().await()
            query = query.startAfter(notif)
        }

        return query.get().await().map { it.toObject(Notification::class.java) }
    }

}