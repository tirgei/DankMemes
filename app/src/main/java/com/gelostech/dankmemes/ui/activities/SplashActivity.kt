package com.gelostech.dankmemes.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.utils.SessionManager
import org.koin.android.ext.android.inject

class SplashActivity : AppCompatActivity() {
    private val sessionManager: SessionManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (sessionManager.isLoggedIn()) {
            true -> launch(MainActivity::class.java)
            else -> launch(LoginActivity::class.java)
        }
    }

    private fun launch(activity: Class<*>)  {
        startActivity(Intent(this, activity))
        overridePendingTransition(R.anim.enter_b, R.anim.exit_a)
        finish()
    }
}
