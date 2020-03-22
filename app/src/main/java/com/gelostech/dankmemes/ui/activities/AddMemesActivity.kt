package com.gelostech.dankmemes.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.ui.base.BaseActivity
import com.gelostech.dankmemes.utils.AppUtils
import kotlinx.android.synthetic.main.activity_add_memes.*

class AddMemesActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_memes)

        initViews()
    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null
    }

    override fun onBackPressed() {
        super.onBackPressed()
        AppUtils.slideLeft(this)
    }
}
