package com.gelostech.dankmemes.models

data class CommentModel(
        var commentKey: String? = null,
        var authorId: String? = null,
        var timeStamp: Long? = null,
        var comment: String? = null,
        var hates: Int? = null,
        var likes: Int? = null,
        var userName: String? = null,
        var picKey: String? = null
)