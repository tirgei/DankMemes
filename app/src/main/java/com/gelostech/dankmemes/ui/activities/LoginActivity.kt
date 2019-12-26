package com.gelostech.dankmemes.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.ui.base.BaseActivity
import com.gelostech.dankmemes.ui.fragments.LoginFragment
import com.gelostech.dankmemes.ui.fragments.SignupFragment
import com.gelostech.dankmemes.utils.addFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.toast

class LoginActivity : BaseActivity() {
    private var doubleBackToExit = false
    private lateinit var signUpFragment: SignupFragment
    private lateinit var loginFragment: LoginFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        checkIfLoggedIn()

        signUpFragment = SignupFragment()
        loginFragment = LoginFragment()

        addFragment(loginFragment, loginHolder.id)

        requestStoragePermission()
    }

    private fun checkIfLoggedIn() {
        signUpFragment = SignupFragment()
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null && !user.isAnonymous) {
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(0,0)
            finish()

        } else if (user!= null && user.isAnonymous) {
            addFragment(signUpFragment, loginHolder.id)
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
