package com.gelostech.dankmemes.data.responses

import com.gelostech.dankmemes.data.Status
import com.gelostech.dankmemes.data.models.Comment

data class CommentsResponse(
        val status: Status,
        val data: List<Comment>?,
        val error: String?
) {
    companion object {
        fun loading(): CommentsResponse = CommentsResponse(Status.LOADING, null, null)

        fun success(comments: List<Comment>): CommentsResponse
                = CommentsResponse(Status.SUCCESS, comments, null)

        fun error(error: String): CommentsResponse = CommentsResponse(Status.ERROR, null, error)
    }
}