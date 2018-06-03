package com.gelostech.dankmemes.models

data class CommentModel(
        var id: String? = null,
        var authorId: String? = null,
        var time: Long? = null,
        var commentContent: String? = null
)