package com.gelostech.dankmemes.utils

import android.content.Context
import android.content.SharedPreferences
import com.gelostech.dankmemes.data.models.User
import com.gelostech.dankmemes.utils.PreferenceHelper.set
import com.gelostech.dankmemes.utils.PreferenceHelper.get

class SessionManager (context: Context) {
    private val prefs: SharedPreferences = PreferenceHelper.defaultPrefs(context)

    /**
     * Save User details on login
     * @param user - User model
     */
    fun saveUser(user: User) {
        prefs[Constants.USERNAME] = user.userName
        prefs[Constants.EMAIL] = user.userEmail
        prefs[Constants.AVATAR] = user.userAvatar
        prefs[Constants.USER_ID] = user.userId
        prefs[Constants.USER_BIO] = user.userBio
        prefs[Constants.LOGGED_IN] = true
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return prefs[Constants.LOGGED_IN, false]
    }

    /**
     * Get the logged in User ID
     */
    fun getUserId(): String {
        return prefs[Constants.USER_ID, ""]
    }

    /**
     * Get the logged in User username
     */
    fun getUsername(): String {
        return prefs[Constants.USERNAME, ""]
    }

    /**
     * Get the logged in User email
     */
    fun getEmail(): String {
        return prefs[Constants.EMAIL, ""]
    }

    /**
     * Get the logged in user bio
     */
    fun getBio(): String {
        return prefs[Constants.USER_BIO, ""]
    }

    /**
     * Get the logged in User avatar
     */
    fun getUserAvatar(): String {
        return prefs[Constants.AVATAR, ""]
    }

    /**
     * Get the logged in User
     */
    fun getUser(): User {
        val user = User()
        user.userId = prefs[Constants.USER_ID, ""]
        user.userName = prefs[Constants.USERNAME, ""]
        user.userAvatar = prefs[Constants.AVATAR, ""]
        user.userEmail = prefs[Constants.EMAIL, ""]
        user.userBio = prefs[Constants.USER_BIO, ""]
        return user
    }

    /**
     * Update User details
     */
    fun updateUser(key: String, value: String) {
        prefs[key] = value
    }

}