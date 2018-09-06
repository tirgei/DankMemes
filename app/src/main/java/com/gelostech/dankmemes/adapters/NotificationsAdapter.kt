package com.gelostech.dankmemes.adapters

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.callbacks.NotificationsCallback
import com.gelostech.dankmemes.commoners.AppUtils
import com.gelostech.dankmemes.models.NotificationModel
import com.gelostech.dankmemes.utils.TimeFormatter
import com.gelostech.dankmemes.utils.inflate
import com.gelostech.dankmemes.utils.loadUrl
import kotlinx.android.synthetic.main.item_notification.view.*

class NotificationsAdapter(private val callback:NotificationsCallback) : RecyclerView.Adapter<NotificationsAdapter.NotificationHolder>() {
    private val notifications = mutableListOf<NotificationModel>()

    fun addNotification(notification: NotificationModel) {
        if (!hasBeenAdded(notification)) {
            if (!isNewer(notification)) {
                notifications.add(notification)
                notifyItemInserted(notifications.size-1)

            } else {
                notifications.add(0, notification)
                notifyItemInserted(0)
            }
        }
    }

    private fun hasBeenAdded(notification: NotificationModel): Boolean {
        var added = false

        for (n in notifications) {
            if (n.id == notification.id) {
                added = true
            }
        }

        return added
    }

    private fun isNewer(notification: NotificationModel): Boolean {
        var newer = false

        if (notifications.size > 0) {
            val n = notifications[0]

            if (n.time!! < notification.time!!) {
                newer = true
            }
        }

        return newer
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationHolder {
        return NotificationHolder(parent.inflate(R.layout.item_notification), callback)
    }

    override fun getItemCount(): Int = notifications.size

    override fun onBindViewHolder(holder: NotificationHolder, position: Int) {
        holder.bind(notifications[position])
    }

    class NotificationHolder(val view: View, private val callback: NotificationsCallback) : RecyclerView.ViewHolder(view) {
        private val avatar = view.avatar
        private val description = view.description
        private val time = view.time
        private val meme = view.meme
        private val root = view.root

        fun bind(notification: NotificationModel) {
            avatar.loadUrl(notification.userAvatar!!)
            meme.loadUrl(notification.imageUrl!!)
            time.text = TimeFormatter().getTimeStamp(notification.time!!)

            when(notification.type!!) {
                0 -> {
                    setLikeNotif(notification.username!!)
                }

                1 -> {
                    setCommentNotif(notification.username!!, notification.description!!)
                }
            }

            avatar.setOnClickListener { callback.onNotificationClicked(avatar, notification) }
            root.setOnClickListener { callback.onNotificationClicked(root, notification) }
            meme.setOnClickListener { callback.onNotificationClicked(meme, notification) }
        }

        private fun setLikeNotif(user: String) {
            val notif = "$user liked your post"

            description.text = AppUtils.highLightName(view.context, notif, 0, user.length)
        }

        private fun setCommentNotif(user: String, comment: String) {
            val notif = "$user commented: $comment"

            description.text = AppUtils.highLightName(view.context, notif, 0, user.length)
        }

    }

}