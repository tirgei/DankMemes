package com.gelostech.dankmemes.fragments


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.commoners.BaseFragment
import com.gelostech.dankmemes.commoners.DankMemesUtil
import com.gelostech.dankmemes.utils.setDrawable
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.mikepenz.ionicons_typeface_library.Ionicons
import kotlinx.android.synthetic.main.fragment_signup.*
import org.jetbrains.anko.toast

class SignupFragment : BaseFragment() {
    private lateinit var firebaseAuth: FirebaseAuth

    companion object {
        private val TAG = SignupFragment::class.java.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        firebaseAuth = FirebaseAuth.getInstance()
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        signupUsername.setDrawable(DankMemesUtil.setDrawable(activity!!, Ionicons.Icon.ion_person, R.color.secondaryText, 18))
        signupEmail.setDrawable(DankMemesUtil.setDrawable(activity!!, Ionicons.Icon.ion_ios_email, R.color.secondaryText, 18))
        signupPassword.setDrawable(DankMemesUtil.setDrawable(activity!!, Ionicons.Icon.ion_android_lock, R.color.secondaryText, 18))
        signupConfirmPassword.setDrawable(DankMemesUtil.setDrawable(activity!!, Ionicons.Icon.ion_android_lock, R.color.secondaryText, 18))

        signupLogin.setOnClickListener {
            if (activity!!.supportFragmentManager.backStackEntryCount > 0)
                activity!!.supportFragmentManager.popBackStackImmediate()
        }

        signupButton.setOnClickListener { signUp() }
    }

    private fun signUp() {
        // Check if all fields are filled
        if (!DankMemesUtil.validated(signupUsername, signupEmail, signupPassword, signupConfirmPassword)) return

        val email = signupEmail.text.toString().trim()
        val pw = signupPassword.text.toString().trim()
        val confirmPw = signupConfirmPassword.text.toString().trim()

        // Check if password and confirm password match
        if (pw != confirmPw) {
            signupConfirmPassword.error = "Does not match password"
            return
        }

        // Create new user
        firebaseAuth.createUserWithEmailAndPassword(email, pw)
                .addOnCompleteListener(activity!!, {task ->
                    if (task.isSuccessful) {
                        Log.e(TAG, "signingIn: Success!")

                        // update UI with the signed-in user's information
                        val user = firebaseAuth.currentUser
                        updateUI(user!!)

                    } else {
                        try {
                            throw task.exception!!
                        } catch (weakPassword: FirebaseAuthWeakPasswordException){
                            signupPassword.error = "Please enter a stronger password"

                        } catch (userExists: FirebaseAuthUserCollisionException) {
                            activity?.to("Account already exists. Please log in.")

                        } catch (e: Exception) {
                            Log.e(TAG, "signingIn: Failure - ${e.localizedMessage}" )
                            activity?.toast("Error signing up. Please try again.")
                        }
                    }
                })

    }

    private fun updateUI(user: FirebaseUser) {

    }


}
