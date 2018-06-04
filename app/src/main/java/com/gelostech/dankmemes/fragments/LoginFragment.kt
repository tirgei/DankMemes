package com.gelostech.dankmemes.fragments


import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.commoners.BaseFragment
import com.gelostech.dankmemes.commoners.DankMemesUtil
import com.gelostech.dankmemes.utils.replaceFragment
import com.gelostech.dankmemes.utils.setDrawable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.mikepenz.ionicons_typeface_library.Ionicons
import kotlinx.android.synthetic.main.fragment_login.*
import org.jetbrains.anko.toast


class LoginFragment : BaseFragment() {
    private lateinit var firebaseAuth: FirebaseAuth

    companion object {
        private val TAG = LoginFragment::class.java.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        firebaseAuth = FirebaseAuth.getInstance()
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginEmail.setDrawable(DankMemesUtil.setDrawable(activity!!, Ionicons.Icon.ion_ios_email, R.color.secondaryText, 18))
        loginPassword.setDrawable(DankMemesUtil.setDrawable(activity!!, Ionicons.Icon.ion_android_lock, R.color.secondaryText, 18))

        loginRegister.setOnClickListener { (activity as AppCompatActivity).replaceFragment(SignupFragment(), R.id.loginHolder) }

        loginButton.setOnClickListener { signIn() }
    }

    private fun signIn() {
        if (!DankMemesUtil.validated(loginEmail, loginPassword)) return

        val email = loginEmail.text.toString().trim()
        val pw = loginPassword.text.toString().trim()

        firebaseAuth.signInWithEmailAndPassword(email, pw)
                .addOnCompleteListener(activity!!, { task ->
                    if (task.isSuccessful) {
                        Log.e(TAG, "signingIn: Success!")

                        // update UI with the signed-in user's information
                        val user = firebaseAuth.currentUser
                        updateUI(user!!)
                    } else {
                        try {
                            throw task.exception!!
                        } catch (wrongPassword: FirebaseAuthInvalidCredentialsException) {
                            loginPassword.error = "Password incorrect"
                        } catch (userNull: FirebaseAuthInvalidUserException) {
                            activity?.toast("Account not found. Have you signed up?")
                        } catch (e: Exception) {
                            Log.e(TAG, "signingIn: Failure - ${e.localizedMessage}" )
                            activity?.toast("Error signing in. Please try again.")
                        }
                    }
                })

    }

    private fun updateUI(user: FirebaseUser) {

    }


}
