package com.gelostech.dankmemes.ui.adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.ui.callbacks.NotificationsCallback
import com.gelostech.dankmemes.utils.AppUtils
import com.gelostech.dankmemes.data.models.Notification
import com.gelostech.dankmemes.databinding.ItemNotificationBinding
import com.gelostech.dankmemes.utils.TimeFormatter
import com.gelostech.dankmemes.utils.inflate
import com.gelostech.dankmemes.utils.load
import kotlinx.android.synthetic.main.item_notification.view.*

class NotificationsAdapter(private val callback:NotificationsCallback) : RecyclerView.Adapter<NotificationsAdapter.NotificationHolder>() {
    private val notifications = mutableListOf<Notification>()

    fun addNotification(notification: Notification) {
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

    fun updateNotif(notification: Notification) {
        for ((index, n) in notifications.withIndex()) {
            if (n.equals(notification)) {
                notifications[index] = notification
                notifyItemChanged(index, notification)
            }
        }
    }

    fun removeNotif(removedNotif: Notification) {
        var indexToRemove = -1

        for ((index, notif) in notifications.withIndex()) {
            if (notif.equals(removedNotif)) {
                indexToRemove = index
            }
        }

        notifications.removeAt(indexToRemove)
        notifyItemRemoved(indexToRemove)
    }

    private fun hasBeenAdded(notification: Notification): Boolean {
        var added = false

        for (n in notifications) {
            if (n.equals(notification)) {
                added = true
            }
        }

        return added
    }

    private fun isNewer(notification: Notification): Boolean {
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

    class NotificationHolder(private val binding: ItemNotificationBinding, private val callback: NotificationsCallback):
            RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: Notification) {
            binding.notification = notification
            binding.callback = callback
            binding.timeFormatter = TimeFormatter()
        }
    }

}