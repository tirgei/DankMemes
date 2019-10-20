package com.gelostech.dankmemes.data.models

data class Report(
        var id: String? = null,
        var memeId: String? = null,
        var memeUrl: String? = null,
        var reporterId: String? = null,
        var memePosterId: String? = null,
        var reason: String? = null,
        var time: Long? = null
)