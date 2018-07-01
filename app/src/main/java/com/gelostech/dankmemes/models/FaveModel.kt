package com.gelostech.dankmemes.models

import java.io.Serializable

data class FaveModel(
        var faveKey: String? = null,
        var picUrl: String? = null,
        var name: String? = null,
        var commentId: String? = null,
        var uploadDay: String? = null,
        var image: Int? = null
): Serializable