package com.gelostech.dankmemes.ui.callbacks

import com.gelostech.dankmemes.data.models.Fave
import com.makeramen.roundedimageview.RoundedImageView

interface FavesCallback {
    fun onFaveClick(view: RoundedImageView, meme: Fave, longClick: Boolean)
}