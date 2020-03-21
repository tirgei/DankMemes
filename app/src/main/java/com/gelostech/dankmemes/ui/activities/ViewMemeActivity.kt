package com.gelostech.dankmemes.ui.activities

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.ui.base.BaseActivity
import com.gelostech.dankmemes.utils.Constants
import com.gelostech.dankmemes.utils.load
import kotlinx.android.synthetic.main.activity_view_meme.*
import android.view.WindowManager
import android.os.Build
import androidx.core.content.ContextCompat


class ViewMemeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setDarkStatusBar()
        setContentView(R.layout.activity_view_meme)

        val image = BitmapFactory.decodeStream(openFileInput("image"))
        viewMemeImage.setImageBitmap(image)

        val url = intent.getStringExtra(Constants.PIC_URL)
        url?.let { viewMemeImage.load(it, R.drawable.loading) }

        val caption = intent.getStringExtra(Constants.CAPTION)
        if (!caption.isNullOrEmpty()) {
            memeCaptionText.text = caption

            viewMemeImage.setOnClickListener {
                if (memeCaption.isShown)
                    memeCaption.visibility = View.GONE
                else
                    memeCaption.visibility = View.VISIBLE
            }
        } else {
            memeCaption.visibility = View.GONE
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
