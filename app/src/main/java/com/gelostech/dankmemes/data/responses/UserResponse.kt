package com.gelostech.dankmemes.data.responses

import com.gelostech.dankmemes.data.Status
import com.gelostech.dankmemes.data.models.User

data class UserResponse (
        val status: Status,
        val user: User?,
        val error: String?
) {
    companion object {
        fun loading(): UserResponse = UserResponse(Status.LOADING, null, null)

        fun success(user: User): UserResponse
                = UserResponse(Status.SUCCESS, user, null)

        fun error(error: String): UserResponse = UserResponse(Status.ERROR, null, error)
    }
}