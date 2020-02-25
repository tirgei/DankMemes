package com.gelostech.dankmemes.ui.callbacks

import android.view.View
import com.gelostech.dankmemes.data.models.Meme
import com.gelostech.dankmemes.data.models.User

interface ProfileMemesCallback : MemesCallback {
    fun onMemeLongClicked(meme: Meme)

    override fun onMemeClicked(view: View, meme: Meme)

    override fun onProfileClicked(view: View, user: User)
}