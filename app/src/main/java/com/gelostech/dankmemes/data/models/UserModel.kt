package com.gelostech.dankmemes.data.models

import java.io.Serializable

data class UserModel(
        var userId: String? = null,
        var userName: String? = null,
        var userToken: String? = null,
        var userAvatar: String? = null,
        var userEmail: String? = null,
        var dateCreated: String? = null,
        var userBio: String? = null
): Serializable