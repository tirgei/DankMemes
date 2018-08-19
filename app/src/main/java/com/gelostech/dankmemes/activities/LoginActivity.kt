package com.gelostech.dankmemes.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.commoners.BaseActivity
import com.gelostech.dankmemes.fragments.LoginFragment
import com.gelostech.dankmemes.fragments.SignupFragment
import com.gelostech.dankmemes.utils.addFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.toast

class LoginActivity : BaseActivity() {
    private var doubleBackToExit = false
    private lateinit var signUpFragment: SignupFragment
    private lateinit var loginFragment: LoginFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        checkIfLoggedIn()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val user = FirebaseAuth.getInstance().currentUser

        signUpFragment = SignupFragment()
        loginFragment = LoginFragment()

        if (user != null && user.isAnonymous) {
            addFragment(signUpFragment, loginHolder.id)
        } else {
            addFragment(loginFragment, loginHolder.id)
        }

        requestStoragePermission()
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
        if (!signUpFragment.backPressOkay() || !loginFragment.backPressOkay()) {
            toast("Please wait...")

        } else if (supportFragmentManager.backStackEntryCount > 0) {
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

}
