package com.gelostech.dankmemes.ui.callbacks

import android.view.View
import com.gelostech.dankmemes.data.models.Comment

interface CommentsCallback {
    fun onCommentClicked(view: View, comment: Comment, longClick: Boolean)
}