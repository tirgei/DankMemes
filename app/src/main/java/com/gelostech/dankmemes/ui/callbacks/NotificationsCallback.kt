package com.gelostech.dankmemes.ui.callbacks

import android.view.View
import com.gelostech.dankmemes.data.models.NotificationModel

interface NotificationsCallback {

    fun onNotificationClicked(view: View, notification: NotificationModel)

}