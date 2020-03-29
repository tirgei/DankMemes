package com.gelostech.dankmemes.data.models

/**
 * Data class for pending memes
 */
data class PendingMeme (
        var id: String? = null,
        var link: String? = null,
        var title: String? = null,
        val time: Long? = null,
        var reviewed: Boolean = false
)