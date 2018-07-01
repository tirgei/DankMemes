package com.gelostech.dankmemes.activities

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.commoners.BaseActivity
import com.gelostech.dankmemes.commoners.Config
import com.gelostech.dankmemes.utils.loadUrl
import kotlinx.android.synthetic.main.activity_view_meme.*

class ViewMemeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_meme)

        val image = BitmapFactory.decodeStream(openFileInput("image"))
        viewMemeImage.setImageBitmap(image)

        val url = intent.getStringExtra(Config.PIC_URL)
        url.let { viewMemeImage.loadUrl(it) }

        val caption = intent.getStringExtra("caption")
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
