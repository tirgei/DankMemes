package com.gelostech.dankmemes.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.gelostech.dankmemes.R

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.enter_a, R.anim.exit_b)
    }
}
