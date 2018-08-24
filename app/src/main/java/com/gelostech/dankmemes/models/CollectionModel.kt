package com.gelostech.dankmemes.models

data class CollectionModel(
        var id: String? = null,
        var name: String? = null,
        var owner: String? = null,
        var memesCount: Int? = null,
        var thumbnailUrl: String? = null
) {
}