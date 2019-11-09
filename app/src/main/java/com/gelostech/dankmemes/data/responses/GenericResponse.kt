package com.gelostech.dankmemes.data.responses

import com.gelostech.dankmemes.data.Status

data class GenericResponse (
        val status: Status,
        val success: Boolean? = null,
        val error: String? = null,
        val item: ITEM_RESPONSE? = null,
        val id: String? = null
) {
    companion object {
        fun loading(): GenericResponse = GenericResponse(Status.LOADING)

        fun success(success: Boolean, id: String? = null): GenericResponse
                = GenericResponse(Status.SUCCESS, success, id =  id)

        fun error(error: String, item: ITEM_RESPONSE): GenericResponse = GenericResponse(Status.ERROR, error =  error, item = item)
    }

    enum class ITEM_RESPONSE {
        POST_MEME,
        LIKE_MEME,
        FAVE_MEME,
        DELETE_MEME,
        RESET_PASSWORD
    }
}