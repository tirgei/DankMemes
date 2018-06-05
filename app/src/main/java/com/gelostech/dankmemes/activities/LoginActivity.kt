package com.gelostech.dankmemes.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.commoners.BaseActivity
import com.gelostech.dankmemes.fragments.LoginFragment
import com.gelostech.dankmemes.fragments.SignupFragment
import com.gelostech.dankmemes.utils.addFragment
import com.google.firebase.auth.FirebaseAuth
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.toast

class LoginActivity : BaseActivity() {
    private var doubleBackToExit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        checkIfLoggedIn()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val user = FirebaseAuth.getInstance().currentUser

        if (user != null && user.isAnonymous) {
            addFragment(SignupFragment(), loginHolder.id)
        } else {
            addFragment(LoginFragment(), loginHolder.id)
        }

    }

    private fun checkIfLoggedIn() {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(0,0)
            finish()
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        } else {
            if (doubleBackToExit) {
                super.onBackPressed()
            } else {
                toast("Tap back again to exit")

                doubleBackToExit = true

                Handler().postDelayed({doubleBackToExit = false}, 1500)
            }
        }
    }

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val frag = supportFragmentManager.findFragmentById(SignupFragment().id)
        frag.onActivityResult(requestCode, resultCode, data)

    }*/
}
