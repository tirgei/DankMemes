package com.gelostech.dankmemes.utils.services

import android.content.Context
import android.content.Intent
import android.util.Log
import com.gelostech.dankmemes.utils.Constants
import com.gelostech.dankmemes.utils.NotificationUtils
import com.gelostech.dankmemes.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject
import timber.log.Timber


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private var notificationUtils: NotificationUtils? = null

    companion object {
        private val TAG = MyFirebaseMessagingService::class.java.simpleName
    }

    override fun onNewToken(p0: String) {
        Timber.e("Token refreshed: %s", p0)

        p0.let {
            // save in prefs
            val sessionManager = SessionManager(this)
            sessionManager.updateUser(Constants.USER_TOKEN, it)

            // sending reg id to your server
            val userID = FirebaseAuth.getInstance().currentUser?.uid
            if (userID != null) {
                val dbRef = FirebaseFirestore.getInstance()
                dbRef.collection(Constants.USERS).document(userID).update(Constants.USER_TOKEN, it)
            }
        }

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            Log.e(TAG, "Notification: " + remoteMessage.notification!!.toString())
            handleNotification(remoteMessage.notification)
        }

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.e(TAG, "Data Payload: " + remoteMessage.data.toString())

            try {
                val json = JSONObject(remoteMessage.data.toString())
                //handleDataMessage(json)
            } catch (e: Exception) {
                Log.e(TAG, "Exception: " + e.message)
            }

        }
    }

    private fun handleNotification(notification: RemoteMessage.Notification?) {
        if (!NotificationUtils(this).isAppIsInBackground(applicationContext)) {
            // app is in foreground, broadcast the push message
            val pushNotification = Intent(Constants.PUSH_NOTIFICATION)
            pushNotification.putExtra("title", notification!!.title)
            pushNotification.putExtra("message", notification.body)

            // play notification sound
            val notificationUtils = NotificationUtils(applicationContext)
            notificationUtils.playNotificationSound()
        } else {
            // If the app is in background, firebase itself handles the notification
        }
    }

    /*private fun handleDataMessage(json: JSONObject) {
        Log.e(TAG, "push json: " + json.toString())

        try {

            if (json.getString("type") == "topup"){
                val pushNotification = Intent(Constants.PUSH_NOTIFICATION)
                pushNotification.putExtra("type", "topup")

                return

            } else if (json.getString("type") == "welcome") {

                val title = json.getString("title")
                val message = json.getString("message")
                val imageUrl = json.getString("image")
                val timestamp = json.getString("timestamp")

                val resultIntent = Intent(applicationContext, MainActivity::class.java)
                    resultIntent.putExtra("message", message)

                // check for image attachment
                if (TextUtils.isEmpty(imageUrl)) {
                    showNotificationMessage(applicationContext, title, message, timestamp, resultIntent)
                } else {
                    // image is present, show notification with image
                    showNotificationMessageWithBigImage(applicationContext, title, message, timestamp, resultIntent, imageUrl)
                }
            } else if (json.getString("type") == "balance_update") {

                val title = json.getString("title")
                val message = json.getString("message")
                val imageUrl = json.getString("image")
                val timestamp = json.getString("timestamp")

                val resultIntent = Intent(applicationContext, MainActivity::class.java)
                resultIntent.putExtra("message", message)

                // check for image attachment
                if (TextUtils.isEmpty(imageUrl)) {
                    showNotificationMessage(applicationContext, title, message, timestamp, resultIntent)
                } else {
                    // image is present, show notification with image
                    showNotificationMessageWithBigImage(applicationContext, title, message, timestamp, resultIntent, imageUrl)
                }
            }

        } catch (e: JSONException) {
            Log.e(TAG, "Json Exception: " + e.message)
        } catch (e: Exception) {
            Log.e(TAG, "Exception: " + e.message)
        }

    }*/

    /**
     * Showing notification with text only
     */
    private fun showNotificationMessage(context: Context, title: String, message: String, timeStamp: String, intent: Intent) {
        notificationUtils = NotificationUtils(context)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        notificationUtils!!.showNotificationMessage(title, message, timeStamp, intent)
    }

    /**
     * Showing notification with text and image
     */
    private fun showNotificationMessageWithBigImage(context: Context, title: String, message: String, timeStamp: String, intent: Intent, imageUrl: String) {
        notificationUtils = NotificationUtils(context)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        notificationUtils!!.showNotificationMessage(title, message, timeStamp, intent, imageUrl)
    }


}