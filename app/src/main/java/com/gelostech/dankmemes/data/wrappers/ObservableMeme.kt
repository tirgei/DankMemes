package com.gelostech.dankmemes.data.wrappers

import com.gelostech.dankmemes.data.models.Meme
import io.reactivex.Observable

data class ObservableMeme(
        override val id: String,
        val meme: Observable<Meme>
): ItemViewModel {
}