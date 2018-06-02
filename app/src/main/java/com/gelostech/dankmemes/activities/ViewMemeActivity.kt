package com.gelostech.dankmemes.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.models.FaveModel
import com.gelostech.dankmemes.models.MemeModel
import com.gelostech.dankmemes.utils.loadUrl
import kotlinx.android.synthetic.main.activity_view_meme.*

class ViewMemeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_meme)

        val isFave = intent.getBooleanExtra("isFave", false)

        if (isFave) {
            val meme = intent.getSerializableExtra("meme") as FaveModel
            viewMemeImage.loadUrl(meme.image!!)
        } else {
            val meme = intent.getSerializableExtra("meme") as MemeModel
            viewMemeImage.loadUrl(meme.image!!)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
