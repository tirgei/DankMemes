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

        fun success(success: Boolean, item: ITEM_RESPONSE? = null, id: String? = null): GenericResponse
                = GenericResponse(Status.SUCCESS, success, item = item, id =  id)

        fun error(error: String): GenericResponse = GenericResponse(Status.ERROR, error =  error)
    }

    enum class ITEM_RESPONSE {
        DELETE_MEME,
        REPORT_MEME,
        DELETE_FAVE,
        POST_COMMENT,
        DELETE_COMMENT
    }
}