package com.gelostech.dankmemes.data.responses

import com.gelostech.dankmemes.data.Status
import com.google.firebase.auth.FirebaseUser

data class GoogleLoginResponse (
        val status: Status,
        val isNewUser: Boolean?,
        val user: FirebaseUser?,
        val error: String?
) {
    companion object {
        fun loading(): GoogleLoginResponse = GoogleLoginResponse(Status.LOADING, null,null, null)

        fun success(isNewUser: Boolean, user: FirebaseUser): GoogleLoginResponse
                = GoogleLoginResponse(Status.SUCCESS, isNewUser, user, null)

        fun error(error: String): GoogleLoginResponse = GoogleLoginResponse(Status.ERROR, null, null, error)
    }
}