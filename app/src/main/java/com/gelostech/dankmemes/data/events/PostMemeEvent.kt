package com.gelostech.dankmemes.data.events

data class PostMemeEvent(
        val type: TYPE
) {
    enum class TYPE {
        NEW_POST,
        FAVORITE
    }
}