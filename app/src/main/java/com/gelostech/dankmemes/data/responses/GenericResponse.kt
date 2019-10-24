package com.gelostech.dankmemes.data.responses

import com.gelostech.dankmemes.data.Status

data class GenericResponse (
        val status: Status,
        val success: Boolean?,
        val error: String?
) {
    companion object {
        fun loading(): GenericResponse = GenericResponse(Status.LOADING, null, null)

        fun success(success: Boolean): GenericResponse
                = GenericResponse(Status.SUCCESS, success, null)

        fun error(error: String): GenericResponse = GenericResponse(Status.ERROR, null, error)
    }
}