package com.gelostech.dankmemes.data.wrappers

import com.gelostech.dankmemes.data.models.Comment
import io.reactivex.Observable

data class ObservableComment(
        val id: String,
        val comment: Observable<Comment>
) {}