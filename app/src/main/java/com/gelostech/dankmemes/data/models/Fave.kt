package com.gelostech.dankmemes.data.models

import java.io.Serializable

data class Fave(
        var id: String? = null,
        var imageUrl: String? = null,
        var time: Long? = null
): Serializable {
    fun equals(fave: Fave): Boolean = this.id == fave.id
}