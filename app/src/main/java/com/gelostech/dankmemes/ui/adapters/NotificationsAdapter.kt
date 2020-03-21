package com.gelostech.dankmemes.ui.adapters

import android.view.ViewGroup
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.models.Notification
import com.gelostech.dankmemes.databinding.ItemNotificationBinding
import com.gelostech.dankmemes.ui.callbacks.NotificationsCallback
import com.gelostech.dankmemes.utils.TimeFormatter
import com.gelostech.dankmemes.utils.inflate
import timber.log.Timber


class NotificationsAdapter(private val callback: NotificationsCallback): PagedListAdapter<Notification, 
        NotificationsAdapter.NotificationHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Notification>() {
            override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCurrentListChanged(previousList: PagedList<Notification>?, currentList: PagedList<Notification>?) {
        super.onCurrentListChanged(previousList, currentList)
        Timber.e("Previous list: $previousList vs Current list: $currentList")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationHolder {
        return NotificationHolder(parent.inflate(R.layout.item_notification), callback)
    }

    override fun onBindViewHolder(holder: NotificationHolder, position: Int) {
        val notification = getItem(position)
        holder.bind(notification!!)
    }

    inner class NotificationHolder(private val binding: ItemNotificationBinding, private val callback: NotificationsCallback):
            RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: Notification) {
            binding.notification = notification
            binding.callback = callback
            binding.timeFormatter = TimeFormatter()
        }
    }
}
