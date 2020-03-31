package com.gelostech.dankmemes.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.gelostech.dankmemes.data.models.User
import com.gelostech.dankmemes.utils.PreferenceHelper.set
import com.gelostech.dankmemes.utils.PreferenceHelper.get
import timber.log.Timber

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
        prefs[Constants.IS_FIRST_LAUNCH] = false
        prefs[Constants.ADMIN_STATUS] = user.admin
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return prefs[Constants.LOGGED_IN, false]
    }

    /**
     * Check if is first launch
     */
    fun isFirstLaunch(): Boolean {
        return prefs[Constants.IS_FIRST_LAUNCH, true]
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
     * Get the logged in User admin status
     */
    fun getAdminStatus(): Int {
        return prefs[Constants.ADMIN_STATUS, 0]
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

    /**
     * Set Dark Mode
     */
    fun setDarkMode(mode: Int) {
        Timber.e("Setting Dark Mode: $mode")
        prefs[Constants.DARK_MODE] = mode
    }

    /**
     * Get theme status
     */
    fun isDarkMode(): Boolean {
        return themeMode() == AppCompatDelegate.MODE_NIGHT_YES
    }

    /**
     * Get dark mode
     */
    fun themeMode(): Int {
        return prefs[Constants.DARK_MODE, AppCompatDelegate.MODE_NIGHT_NO]
    }

    /**
     * Check if user has posted new content
     */
    fun hasNewContent(): Boolean {
        return prefs[Constants.HAS_NEW_CONTENT, false]
    }

    /**
     * Set if user has new content
     */
    fun hasNewContent(freshContent: Boolean) {
        prefs[Constants.HAS_NEW_CONTENT] = freshContent
    }

    /**
     * Function to clear prefs
     */
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}