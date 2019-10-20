package com.gelostech.dankmemes.data.models

import java.io.Serializable

data class Meme(
        var id: String? = null,
        var caption: String? = null,
        var imageUrl: String? = null,
        var likesCount: Int = 0,
        var commentsCount: Int = 0,
        var time: Long? = null,
        var memePoster: String? = null,
        var memePosterAvatar: String? = null,
        var memePosterID: String? = null,
        var thumbnail: String? = null,
        var likes: MutableMap<String, Boolean> = mutableMapOf(),
        var faves: MutableMap<String, Boolean> = mutableMapOf()
): Serializable {
    fun equals(meme: Meme): Boolean {
        return this.id == meme.id
    }
}