package com.gelostech.dankmemes.models

import java.io.Serializable

data class FaveModel(
        var id: String? = null,
        var imageURL: String? = null,
        var image: Int? = null
): Serializable