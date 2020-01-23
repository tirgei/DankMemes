package com.gelostech.dankmemes.data.models

data class Notification(
        var type: Int? = null,
        var id: String? = null,
        var userId: String? = null,
        var userAvatar: String? = null,
        var username: String? = null,
        var memeId: String? = null,
        var time: Long? = null,
        var title: String? = null,
        var description: String? = null,
        var imageUrl: String? = null,
        var notifiedUserId: String? = null
) {
    companion object {
        const val NOTIFICATION_TYPE_LIKE = 0
        const val NOTIFICATION_TYPE_COMMENT = 1
    }

    fun equals(notification: Notification): Boolean = this.id == notification.id
}