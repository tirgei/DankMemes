package com.gelostech.dankmemes.data.models

data class Comment(
        var commentId: String? = null,
        var userId: String? = null,
        var time: Long? = null,
        var comment: String? = null,
        var hates: Int? = null,
        var likes: Int? = null,
        var userName: String? = null,
        var userAvatar: String? = null,
        var memeId: String? = null
) {
    fun equals(comment: Comment): Boolean = this.commentId == comment.commentId
}