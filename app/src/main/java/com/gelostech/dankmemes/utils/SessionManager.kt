package com.gelostech.dankmemes.utils

import android.content.Context
import android.content.SharedPreferences
import com.gelostech.dankmemes.data.models.User
import com.gelostech.dankmemes.utils.PreferenceHelper.set

class SessionManager (context: Context) {
    private val prefs: SharedPreferences = PreferenceHelper.defaultPrefs(context)

    fun saveUser(user: User) {
        prefs[Constants.USERNAME] = user.userName
        prefs[Constants.EMAIL] = user.userEmail
        prefs[Constants.AVATAR] = user.userAvatar
        prefs[Constants.LOGGED_IN] = true
    }

}