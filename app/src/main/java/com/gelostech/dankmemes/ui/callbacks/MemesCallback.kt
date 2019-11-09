package com.gelostech.dankmemes.ui.callbacks

import android.view.View
import com.gelostech.dankmemes.data.models.Meme
import com.gelostech.dankmemes.data.models.User

interface MemesCallback {
    fun onMemeClicked(view: View, meme: Meme)
    fun onProfileClicked(user: User)
}