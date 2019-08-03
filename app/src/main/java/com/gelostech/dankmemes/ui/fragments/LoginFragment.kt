package com.gelostech.dankmemes.ui.fragments


import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.ui.activities.MainActivity
import com.gelostech.dankmemes.utils.AppUtils
import com.gelostech.dankmemes.utils.AppUtils.drawableToBitmap
import com.gelostech.dankmemes.utils.AppUtils.setDrawable
import com.gelostech.dankmemes.ui.base.BaseFragment
import com.gelostech.dankmemes.utils.Constants
import com.gelostech.dankmemes.data.models.UserModel
import com.gelostech.dankmemes.utils.PreferenceHelper
import com.gelostech.dankmemes.utils.PreferenceHelper.set
import com.gelostech.dankmemes.utils.TimeFormatter
import com.gelostech.dankmemes.utils.replaceFragment
import com.gelostech.dankmemes.utils.setDrawable
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.mikepenz.ionicons_typeface_library.Ionicons
import kotlinx.android.synthetic.main.fragment_login.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import timber.log.Timber


class LoginFragment : BaseFragment() {
    private lateinit var signupSuccessful: Bitmap
    private var isLoggingIn = false
    private lateinit var prefs: SharedPreferences

    companion object {
        private val TAG = LoginFragment::class.java.simpleName
        private const val GOOGLE_SIGN_IN = 123
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
        googleLogin.setSize(SignInButton.SIZE_WIDE)

        loginRegister.setOnClickListener {
            if (!isLoggingIn)
                (activity as AppCompatActivity).replaceFragment(SignupFragment(), R.id.loginHolder)
            else
                activity!!.toast("Please wait...")
        }

        googleLogin.setOnClickListener { googleLogin() }
        loginButton.setOnClickListener { signIn() }
        loginForgotPassword.setOnClickListener { if (!isLoggingIn) forgotPassword() else activity!!.toast("Please wait...")}
    }

    private fun signIn() {
        if (!AppUtils.validated(loginEmail, loginPassword)) return

        val email = loginEmail.text.toString().trim()
        val pw = loginPassword.text.toString().trim()

        isLoggingIn = true
        loginButton.startAnimation()
        getFirebaseAuth().signInWithEmailAndPassword(email, pw)
                .addOnCompleteListener(activity!!) { task ->
                    if (task.isSuccessful) {
                        Timber.e("signingIn: Success!")

                        // update UI with the signed-in user's information
                        val user = task.result?.user!!
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
                            Timber.e( "signingIn: Failure - ${e.localizedMessage}" )
                            activity?.toast("Error signing in. Please try again.")
                        }
                    }
                }

    }

    private fun googleLogin() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_key))
                .requestEmail()
                .build()

        val mGoogleSignInClient = GoogleSignIn.getClient(activity!!, gso)

        showLoading("Signing in...")
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
    }

    private fun forgotPassword() {
        if (!AppUtils.validated(loginEmail)) return

        val email = loginEmail.text.toString().trim()

        activity?.alert("Instructions to reset your password will be sent to $email") {
            title = "Forgot password"

            positiveButton("SEND EMAIL") {

                getFirebaseAuth().sendPasswordResetEmail(email)
                        .addOnCompleteListener(activity!!) { task ->
                            if (task.isSuccessful) {
                                Timber.e("sendResetPassword: Success!")
                                activity?.toast("Email sent")

                            } else {
                                try {
                                    throw task.exception!!
                                } catch (malformedEmail: FirebaseAuthInvalidCredentialsException) {
                                    loginEmail.error = "Incorrect email format"
                                    activity?.toast("Email not sent. Please try again.")

                                } catch (e: Exception) {
                                    Timber.e("sendResetEmail: Failure - $e" )
                                    activity?.toast("Email not sent. Please try again.")
                                }
                            }
                        }

            }

            negativeButton("CANCEL") {}
        }!!.show()
    }

    private fun updateUI(user: FirebaseUser) {
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.TOPIC_GLOBAL)
        val dbRef = getDatabaseReference().child("users").child(user.uid)
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.e(TAG, "Error fetching user: ${p0.message}")
            }

            override fun onDataChange(p0: DataSnapshot) {
                loginButton.doneLoadingAnimation(AppUtils.getColor(activity!!, R.color.pink), signupSuccessful)
                val userObject = p0.getValue(UserModel::class.java)

                prefs[Constants.USERNAME] = userObject!!.userName
                prefs[Constants.EMAIL] = userObject.userEmail
                prefs[Constants.AVATAR] = userObject.userAvatar
                prefs[Constants.LOGGED_IN] = true

                hideLoading()

                Handler().postDelayed({
                    activity!!.toast("Welcome back ${userObject.userName}")

                    startActivity(Intent(activity!!, MainActivity::class.java))
                    activity!!.overridePendingTransition(R.anim.enter_b, R.anim.exit_a)
                    activity!!.finish()
                }, 400)
            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == GOOGLE_SIGN_IN) {
            hideLoading()
            val  task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Timber.e("Google sign in failed")

                activity!!.toast("Error signing in. Try again")
            }
        }

    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        showLoading("Checking account...")
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)

        getFirebaseAuth().signInWithCredential(credential)
                .addOnCompleteListener(activity!!) { p0 ->
                    if (p0.isSuccessful) {
                        val isNew = p0.result?.additionalUserInfo?.isNewUser!!

                        if (isNew) {
                            val user = getFirebaseAuth().currentUser!!
                            createUser(user)

                        } else {
                            val user = getFirebaseAuth().currentUser!!
                            updateUI(user)
                        }

                    } else {
                        activity!!.toast("Error signing in. Try again")
                    }
                }

    }

    private fun createUser(user: FirebaseUser) {
        val newUser = UserModel()
        newUser.userName = user.displayName
        newUser.userEmail = user.email
        newUser.dateCreated = TimeFormatter().getNormalYear(System.currentTimeMillis())
        newUser.userToken = FirebaseInstanceId.getInstance().token
        newUser.userId = user.uid
        newUser.userBio = activity?.getString(R.string.new_user_bio)
        newUser.userAvatar = user.photoUrl?.toString()

        user.sendEmailVerification()

        FirebaseMessaging.getInstance().subscribeToTopic(Constants.TOPIC_GLOBAL)
        getDatabaseReference().child("users").child(user.uid).setValue(newUser).addOnCompleteListener {

            prefs[Constants.USERNAME] = newUser.userName
            prefs[Constants.EMAIL] = newUser.userEmail
            prefs[Constants.AVATAR] = newUser.userAvatar

            hideLoading()

            activity!!.toast("Welcome ${user.displayName}")
            startActivity(Intent(activity!!, MainActivity::class.java))
            activity!!.overridePendingTransition(R.anim.enter_b, R.anim.exit_a)
            activity!!.finish()
        }
    }

    // Check if user has initiated logging in process. If in process, disable back button
    fun backPressOkay(): Boolean = !isLoggingIn

    override fun onDestroy() {
        if (loginButton != null) loginButton.dispose()
        super.onDestroy()
    }

}
