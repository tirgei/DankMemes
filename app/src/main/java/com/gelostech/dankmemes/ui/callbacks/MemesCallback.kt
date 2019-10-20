package com.gelostech.dankmemes.ui.callbacks

import android.view.View
import com.gelostech.dankmemes.data.models.Meme

interface MemesCallback {
    fun onMemeClicked(view: View, meme: Meme)
}