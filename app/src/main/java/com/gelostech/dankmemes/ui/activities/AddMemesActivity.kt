package com.gelostech.dankmemes.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.ui.base.BaseActivity

class AddMemesActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_memes)
    }
}
