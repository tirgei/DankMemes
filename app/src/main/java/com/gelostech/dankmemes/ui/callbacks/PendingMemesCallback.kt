package com.gelostech.dankmemes.ui.callbacks

import android.view.View
import com.gelostech.dankmemes.data.models.PendingMeme

interface PendingMemesCallback {
    fun onPendingMemeClicked(view: View, meme: PendingMeme)
}