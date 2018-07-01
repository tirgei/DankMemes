package com.gelostech.dankmemes.fragments


import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.activities.MainActivity
import com.gelostech.dankmemes.commoners.BaseFragment
import com.gelostech.dankmemes.commoners.DankMemesUtil
import com.gelostech.dankmemes.commoners.DankMemesUtil.drawableToBitmap
import com.gelostech.dankmemes.commoners.DankMemesUtil.setDrawable
import com.gelostech.dankmemes.models.UserModel
import com.gelostech.dankmemes.utils.PreferenceHelper
import com.gelostech.dankmemes.utils.PreferenceHelper.set
import com.gelostech.dankmemes.utils.replaceFragment
import com.gelostech.dankmemes.utils.setDrawable
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.mikepenz.ionicons_typeface_library.Ionicons
import kotlinx.android.synthetic.main.fragment_login.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast

class LoginFragment : BaseFragment() {
    private lateinit var signupSuccessful: Bitmap
    private var isLoggingIn = false
    private lateinit var prefs: SharedPreferences

    companion object {
        private val TAG = LoginFragment::class.java.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        val successfulIcon = setDrawable(activity!!, Ionicons.Icon.ion_checkmark_round, R.color.white, 25)
        signupSuccessful = drawableToBitmap(successfulIcon)
        prefs = PreferenceHelper.defaultPrefs(activity!!)

        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginEmail.setDrawable(setDrawable(activity!!, Ionicons.Icon.ion_ios_email, R.color.secondaryText, 18))
        loginPassword.setDrawable(setDrawable(activity!!, Ionicons.Icon.ion_android_lock, R.color.secondaryText, 18))

        loginRegister.setOnClickListener {
            if (!isLoggingIn)
                (activity as AppCompatActivity).replaceFragment(SignupFragment(), R.id.loginHolder)
            else
                activity!!.toast("Please wait...")
        }

        loginButton.setOnClickListener { signIn() }
        loginForgotPassword.setOnClickListener { if (!isLoggingIn) forgotPassword() else activity!!.toast("Please wait...")}
    }

    private fun signIn() {
        if (!DankMemesUtil.validated(loginEmail, loginPassword)) return

        val email = loginEmail.text.toString().trim()
        val pw = loginPassword.text.toString().trim()

        isLoggingIn = true
        loginButton.startAnimation()
        getFirebaseAuth().signInWithEmailAndPassword(email, pw)
                .addOnCompleteListener(activity!!, { task ->
                    if (task.isSuccessful) {
                        Log.e(TAG, "signingIn: Success!")

                        // update UI with the signed-in user's information
                        val user = task.result.user
                        updateUI(user!!)
                    } else {
                        try {
                            throw task.exception!!
                        } catch (wrongPassword: FirebaseAuthInvalidCredentialsException) {
                            isLoggingIn = false
                            loginButton.revertAnimation()
                            loginPassword.error = "Password incorrect"

                        } catch (userNull: FirebaseAuthInvalidUserException) {
                            isLoggingIn = false
                            loginButton.revertAnimation()
                            activity?.toast("Account not found. Have you signed up?")

                        } catch (e: Exception) {
                            isLoggingIn = false
                            loginButton.revertAnimation()
                            Log.e(TAG, "signingIn: Failure - ${e.localizedMessage}" )
                            activity?.toast("Error signing in. Please try again.")
                        }
                    }
                })

    }

    private fun forgotPassword() {
        if (!DankMemesUtil.validated(loginEmail)) return

        val email = loginEmail.text.toString().trim()

        activity?.alert("Instructions to reset your password will be sent to $email") {
            title = "Forgot password"

            positiveButton("SEND EMAIL") {

                getFirebaseAuth().sendPasswordResetEmail(email)
                        .addOnCompleteListener(activity!!, { task ->
                            if (task.isSuccessful) {
                                Log.e(TAG, "sendResetPassword: Success!")

                                activity?.toast("Email sent")
                            } else {
                                try {
                                    throw task.exception!!
                                } catch (malformedEmail: FirebaseAuthInvalidCredentialsException) {
                                    loginEmail.error = "Incorrect email format"
                                    activity?.toast("Email not sent. Please try again.")

                                } catch (e: Exception) {
                                    Log.e(TAG, "sendResetEmail: Failure - $e" )
                                    activity?.toast("Email not sent. Please try again.")
                                }
                            }
                        })

            }

            negativeButton("CANCEL") {}
        }!!.show()
    }

    private fun updateUI(user: FirebaseUser) {
        FirebaseMessaging.getInstance().subscribeToTopic("memes")
        val dbRef = getDatabaseReference().child("users").child(user.uid)
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.e(TAG, "Error fetching user: ${p0.message}")
            }

            override fun onDataChange(p0: DataSnapshot) {
                loginButton.doneLoadingAnimation(DankMemesUtil.getColor(activity!!, R.color.pink), signupSuccessful)
                val userObject = p0.getValue(UserModel::class.java)

                prefs["username"] = userObject!!.userName
                prefs["email"] = userObject.userEmail

                Handler().postDelayed({
                    activity!!.toast("Welcome back ${userObject.userName}")

                    startActivity(Intent(activity!!, MainActivity::class.java))
                    activity!!.overridePendingTransition(R.anim.enter_b, R.anim.exit_a)
                    activity!!.finish()
                }, 400)
            }
        })

    }

    // Check if user has initiated logging in process. If in process, disable back button
    fun backPressOkay(): Boolean = !isLoggingIn

    override fun onDestroy() {
        if (loginButton != null) loginButton.dispose()
        super.onDestroy()
    }

}
