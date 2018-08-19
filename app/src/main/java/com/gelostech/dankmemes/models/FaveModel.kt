package com.gelostech.dankmemes.models

import java.io.Serializable

data class FaveModel(
        var type: Int = 0,
        var id: String? = null,
        var imageUrl: String? = null,
        var time: Long? = null
): Serializable