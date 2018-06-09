package com.gelostech.dankmemes.models

import java.io.Serializable

data class MemeModel(
        var id: String? = null,
        var caption: String? = null,
        var imageUrl: String? = null,
        var image: Int? = null,
        var likesCount: Int? = null,
        var commentsCount: Int? = null,
        var time: Long? = null,
        var memePoster: String? = null,
        var memePosterID: String? = null
): Serializable