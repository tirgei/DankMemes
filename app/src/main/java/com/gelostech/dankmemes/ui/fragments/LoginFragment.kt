package com.gelostech.dankmemes.ui.fragments


import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.Status
import com.gelostech.dankmemes.data.models.User
import com.gelostech.dankmemes.ui.activities.MainActivity
import com.gelostech.dankmemes.ui.base.BaseFragment
import com.gelostech.dankmemes.ui.viewmodels.UsersViewModel
import com.gelostech.dankmemes.utils.*
import com.gelostech.dankmemes.utils.AppUtils.drawableToBitmap
import com.gelostech.dankmemes.utils.AppUtils.setDrawable
import com.gelostech.dankmemes.utils.PreferenceHelper.set
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.mikepenz.ionicons_typeface_library.Ionicons
import kotlinx.android.synthetic.main.fragment_login.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import org.koin.android.ext.android.inject
import timber.log.Timber


class LoginFragment : BaseFragment() {
    private lateinit var signupSuccessful: Bitmap
    private var isLoggingIn = false
    private lateinit var prefs: SharedPreferences
    private val usersViewModel: UsersViewModel by inject()
    private val sessionManager: SessionManager by inject()
    private val googleSignInOptions: GoogleSignInOptions by inject()

    companion object {
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

        initLoginObserver()
        initUserObserver()
        initGoogleLoginObserver()

        loginButton.setOnClickListener { login() }
        googleLogin.setOnClickListener { loginWithGoogle() }
        loginForgotPassword.setOnClickListener { if (!isLoggingIn) forgotPassword() else activity!!.toast("Please wait...")}
    }

    /**
     * Login with provided email and password
     */
    private fun login() {
        if (!AppUtils.validated(loginEmail, loginPassword)) return

        val email = loginEmail.text.toString().trim()
        val password = loginPassword.text.toString().trim()

        usersViewModel.loginUserWithEmailAndPassword(email, password)
    }

    /**
     * Function to login with Google
     */
    private fun loginWithGoogle() {
        val mGoogleSignInClient = GoogleSignIn.getClient(activity!!, googleSignInOptions)

        showLoading("Logging in...")
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
    }

    /**
     * Initialize function to observer logging in with Email & Password LiveData
     */
    private fun initLoginObserver() {
        usersViewModel.loginLiveData.observe(this, Observer {
            when (it.status) {
                Status.LOADING -> {
                    isLoggingIn = true
                    loginButton.startAnimation()
                }

                Status.SUCCESS -> {
                    usersViewModel.fetchUser(it.user!!.uid)
                }

                Status.ERROR -> {
                    errorLoggingIn(it.error!!)
                }
            }
        })
    }

    /**
     * Initialize function to observer Google login LiveData
     */
    private fun initGoogleLoginObserver() {
        usersViewModel.loginWithGoogleLiveData.observe(this, Observer {
            when (it.status) {
                Status.LOADING -> {
                    Timber.e("Logging in with Google Account...")
                }

                Status.SUCCESS -> {
                    if (it.isNewUser!!) {
                        val googleUser = it.user!!
                        val newUser = User()

                        newUser.userName = googleUser.displayName
                        newUser.userEmail = googleUser.email
                        newUser.dateCreated = TimeFormatter().getNormalYear(System.currentTimeMillis())
                        newUser.userToken = FirebaseInstanceId.getInstance().token
                        newUser.userId = googleUser.uid
                        newUser.userBio = activity?.getString(R.string.new_user_bio)
                        newUser.userAvatar = googleUser.photoUrl?.toString()

                        usersViewModel.createGoogleUserAccount(newUser)
                        googleUser.sendEmailVerification()
                    } else
                        usersViewModel.fetchUser(it.user!!.uid)
                }

                Status.ERROR -> {
                    errorLoggingIn(it.error!!)
                }
            }

        })
    }

    /**
     * Initialize function to observe User LiveData
     */
    private fun initUserObserver() {
        usersViewModel.userLiveData.observe(this, Observer {
            when (it.status) {
                Status.LOADING -> {
                    Timber.e("Fetching User details")
                }

                Status.SUCCESS -> {
                    val user = it.user!!
                    sessionManager.saveUser(user)
                    proceedToMainActivity(user.userName!!)
                }

                Status.ERROR -> {
                    errorLoggingIn(it.error!!)
                }
            }
        })
    }

    /**
     * Successful login proceed to MainActivity
     * @param username - Logged in User username
     */
    private fun proceedToMainActivity(username: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.TOPIC_GLOBAL)
        loginButton.doneLoadingAnimation(AppUtils.getColor(activity!!, R.color.pink), signupSuccessful)
        hideLoading()

        runDelayed(400) {
            isLoggingIn = false

            longToast("Welcome $username \uD83D\uDE03")

            startActivity(Intent(activity!!, MainActivity::class.java))
            activity!!.overridePendingTransition(R.anim.enter_b, R.anim.exit_a)
            activity!!.finish()
        }
    }

    /**
     * Handle error when login in
     * @param message - Message to display to User
     */
    private fun errorLoggingIn(message: String) {
        isLoggingIn = false
        hideLoading()
        loginButton.revertAnimation()
        toast(message)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GOOGLE_SIGN_IN) {
            val  task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                usersViewModel.loginWithGoogle(account)
            } catch (e: ApiException) {
                Timber.e("Google sign in failed")
                errorLoggingIn("Error signing in. Try again")
            }
        }

    }

    // Check if user has initiated logging in process. If in process, disable back button
    fun backPressOkay(): Boolean = !isLoggingIn

    override fun onDestroy() {
        if (loginButton != null) loginButton.dispose()
        super.onDestroy()
    }

}
