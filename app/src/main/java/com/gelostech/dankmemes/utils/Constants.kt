package com.gelostech.dankmemes.utils

object Constants {

    // Global topic to receive app wide push notifications
    const val TOPIC_GLOBAL = "memes"
    const val TOPIC_ADMIN = "admin"

    // Broadcast receiver intent filters
    const val REGISTRATION_COMPLETE = "registrationComplete"
    const val PUSH_NOTIFICATION = "pushNotification"

    // Id to handle the notification in the notification tray
    const val NOTIFICATION_ID = 100
    const val NOTIFICATION_ID_BIG_IMAGE = 101

    // Prefs constants
    const val EMAIL = "email"
    const val USER_ID = "user_id"
    const val USERNAME = "username"
    const val AVATAR = "avatar"
    const val LOGGED_IN = "logged_in"
    const val ADMIN_STATUS = "admin_status"
    const val DARK_MODE = "dark_mode"
    const val HAS_NEW_CONTENT = "has_new_content"
    const val IS_FIRST_LAUNCH = "is_first_launch"

    const val PIC_URL = "pic_url"
    const val CAPTION = "caption"
    const val MEME_ID = "meme_id"

    // Firebase
    const val MEMES_COUNT = 25L
    const val AD_COUNT = 3
    const val MEMES = "memes"
    const val FAVORITES = "favorites"
    const val USER_FAVES = "user-favorites"
    const val NOTIFICATIONS = "notifications"
    const val USER_NOTIFS = "user-notifications"
    const val PENDING_MEMES = "pending-memes"
    const val TIME = "time"
    const val LIKES_COUNT = "likesCount"
    const val COMMENTS_COUNT = "commentsCount"
    const val LIKES = "likes"
    const val FAVES = "faves"
    const val POSTER_ID = "memePosterID"
    const val METADATA = "metadata"
    const val LAST_ACTIVE = "last-active"
    const val REPORTS = "reports"
    const val USERS = "users"
    const val AVATARS = "avatars"
    const val USER_NAME = "userName"
    const val USER_BIO = "userBio"
    const val USER_AVATAR = "userAvatar"
    const val USER_TOKEN = "userToken"
    const val COMMENTS = "comments"
    const val MEME_COMMENTS = "meme-comments"
    const val DATE_UPDATED = "dateUpdated"
    const val REVIEWED = "reviewed"
    const val MUTED = "muted"

    // Admin type
    const val ADMIN = 1
    const val SUPER_ADMIN = 2
}