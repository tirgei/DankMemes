package com.gelostech.dankmemes.data.models

import com.gelostech.dankmemes.data.wrappers.ItemViewModel
import java.io.Serializable

data class User(
        var userId: String? = null,
        var userName: String? = null,
        var userToken: String? = null,
        var userAvatar: String? = null,
        var userEmail: String? = null,
        var dateCreated: String? = null,
        var userBio: String? = null
): ItemViewModel, Serializable {
    override val id: String
        get() = userId!!
}