package com.gelostech.dankmemes.models

data class NotificationModel(
        var type: Int? = null,
        var id: String? = null,
        var userId: String? = null,
        var userAvatar: Int? = null,
        var username: String? = null,
        var memeId: String? = null,
        var time: Long? = null,
        var title: String? = null,
        var description: String? = null,
        var imageUrl: String? = null

)