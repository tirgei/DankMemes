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

        fun success(success: Boolean, item: ITEM_RESPONSE): GenericResponse
                = GenericResponse(Status.SUCCESS, success, null, item)

        fun error(error: String): GenericResponse = GenericResponse(Status.ERROR, null, error, null)
    }

    enum class ITEM_RESPONSE {
        POST_MEME,
        LIKE_MEME,
        FAVE_MEME,
        DELETE_MEME,
        RESET_PASSWORD
    }
}