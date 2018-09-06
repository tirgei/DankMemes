package com.gelostech.dankmemes.callbacks

import android.view.View
import com.gelostech.dankmemes.models.NotificationModel

interface NotificationsCallback {

    fun onNotificationClicked(view: View, notification: NotificationModel)

}