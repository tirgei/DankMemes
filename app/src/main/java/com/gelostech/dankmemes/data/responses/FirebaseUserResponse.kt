package com.gelostech.dankmemes.data.responses

import com.gelostech.dankmemes.data.Status
import com.google.firebase.auth.FirebaseUser

data class FirebaseUserResponse (
        val status: Status,
        val user: FirebaseUser?,
        val isNew: Boolean?,
        val error: String?
) {
    companion object {
        fun loading(): FirebaseUserResponse = FirebaseUserResponse(Status.LOADING, null, null, null)

        fun success(user: FirebaseUser, isNew: Boolean = false): FirebaseUserResponse
                = FirebaseUserResponse(Status.SUCCESS, user, isNew, null)

        fun error(error: String): FirebaseUserResponse = FirebaseUserResponse(Status.ERROR, null, null, error)
    }
}