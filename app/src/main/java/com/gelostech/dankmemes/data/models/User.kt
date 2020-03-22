package com.gelostech.dankmemes.data.models

import java.io.Serializable

data class User(
        var userId: String? = null,
        var userName: String? = null,
        var userToken: String? = null,
        var userAvatar: String? = null,
        var userEmail: String? = null,
        var dateCreated: String? = null,
        var dateUpdated: String? = null,
        var userBio: String? = null,
        var admin: Int? = 0,
        var posts: Int? = 0,
        var muted: Boolean = false,
        var followers: MutableMap<String, Boolean> = mutableMapOf(),
        var following: MutableMap<String, Boolean> = mutableMapOf()
): Serializable {}