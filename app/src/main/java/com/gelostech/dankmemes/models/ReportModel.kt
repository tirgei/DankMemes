package com.gelostech.dankmemes.models

data class ReportModel(
        var id: String? = null,
        var memeId: String? = null,
        var memeUrl: String? = null,
        var reporterId: String? = null,
        var memePosterId: String? = null,
        var reason: String? = null,
        var time: Long? = null
)