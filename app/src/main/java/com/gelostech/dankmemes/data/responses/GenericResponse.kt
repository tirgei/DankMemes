package com.gelostech.dankmemes.data.responses

import com.gelostech.dankmemes.data.Status

data class GenericResponse (
        val status: Status,
        val success: Boolean?,
        val error: String?,
        val item: ITEM_RESPONSE?
) {
    companion object {
        fun loading(): GenericResponse = GenericResponse(Status.LOADING, null, null, null)

        fun success(success: Boolean): GenericResponse
                = GenericResponse(Status.SUCCESS, success, null, null)

        fun error(error: String, item: ITEM_RESPONSE): GenericResponse = GenericResponse(Status.ERROR, null, error, item)
    }

    enum class ITEM_RESPONSE {
        POST_MEME,
        LIKE_MEME,
        FAVE_MEME,
        DELETE_MEME,
        RESET_PASSWORD
    }
}