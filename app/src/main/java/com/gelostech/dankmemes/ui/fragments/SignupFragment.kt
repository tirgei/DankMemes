package com.gelostech.dankmemes.ui.fragments


import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.Status
import com.gelostech.dankmemes.data.models.User
import com.gelostech.dankmemes.ui.base.BaseFragment
import com.gelostech.dankmemes.ui.viewmodels.UsersViewModel
import com.gelostech.dankmemes.utils.*
import com.gelostech.dankmemes.utils.AppUtils.setDrawable
import com.google.firebase.iid.FirebaseInstanceId
import com.mikepenz.ionicons_typeface_library.Ionicons
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_signup.*
import org.jetbrains.anko.toast
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SignupFragment : BaseFragment() {
    private var imageUri: Uri? = null
    private var imageSelected = false
    private var isSigningUp = false
    private val usersViewModel: UsersViewModel by viewModel()

    companion object {
        private const val AVATAR_REQUEST = 102
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        signupUsername.setDrawable(setDrawable(activity!!, Ionicons.Icon.ion_person, R.color.color_text_primary, 18))
        signupEmail.setDrawable(setDrawable(activity!!, Ionicons.Icon.ion_ios_email, R.color.color_text_primary, 18))
        signupPassword.setDrawable(setDrawable(activity!!, Ionicons.Icon.ion_android_lock, R.color.color_text_primary, 18))
        signupConfirmPassword.setDrawable(setDrawable(activity!!, Ionicons.Icon.ion_android_lock, R.color.color_text_primary, 18))

        initRegisterObserver()
        initUserObserver()

        signupAvatar.setOnClickListener {
            if (!isSigningUp) {
                AppUtils.requestStoragePermission(activity!!) { granted ->
                    if (granted) {
                        val galleryIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(galleryIntent, AVATAR_REQUEST)
                    } else longToast("Storage permission is required to select Avatar")
                }
            }
        }

        signupLogin.setOnClickListener {
            if (!isSigningUp) {
                if (activity!!.supportFragmentManager.backStackEntryCount > 0)
                    activity!!.supportFragmentManager.popBackStackImmediate()
                else
                    (activity as AppCompatActivity).replaceFragment(LoginFragment(), R.id.loginHolder)
            } else activity!!.toast("Please wait...")
        }

        signupTerms.setOnClickListener {
           if (!isSigningUp) {
               val i = Intent(Intent.ACTION_VIEW)
               i.data = Uri.parse("https://sites.google.com/view/dankmemesapp/terms-and-conditions")
               startActivity(i)
           } else activity!!.toast("Please wait...")
        }

        signupButton.setOnClickListener { register() }
    }

    /**
     * Function to register new User
     */
    private fun register() {
        // Check if all fields are filled
        if (!AppUtils.validated(signupUsername, signupEmail, signupPassword, signupConfirmPassword)) return

        val name = signupUsername.text.toString().trim()
        val email = signupEmail.text.toString().trim()
        val password = signupPassword.text.toString().trim()
        val confirmPw = signupConfirmPassword.text.toString().trim()

        // Check if password and confirm password match
        if (password != confirmPw) {
            signupConfirmPassword.error = "Does not match password"
            return
        }

        // Check if user has agreed to terms and conditions
        if (!signupCheckBox.isChecked) {
            activity?.toast("Please check the terms and conditions")
            return
        }

        // Check if user has selected avatar
        if (!imageSelected) {
            activity?.toast("Please select a profile picture")
            return
        }

        // Check username
        if (!AppUtils.isValidUsername(name)) {
            signupUsername.error = "Invalid name"
            return
        }

        // Create new user
        isSigningUp = true
        signupButton.startAnimation()
        usersViewModel.registerUser(email, password)
    }

    /**
     * Initialize function to observe register LiveData
     */
    private fun initRegisterObserver() {
        usersViewModel.authLiveData.observe(this, Observer {
            when(it.status) {
                Status.LOADING -> {
                    isSigningUp = true
                    signupButton.startAnimation()
                }

                Status.SUCCESS -> {
                    val user = it.user!!
                    val newUser = User()
                    newUser.userName = signupUsername.text.toString().trim()
                    newUser.userEmail = user.email
                    newUser.dateCreated = TimeFormatter().getNormalYear(System.currentTimeMillis())
                    newUser.userToken = FirebaseInstanceId.getInstance().token
                    newUser.userId = user.uid
                    newUser.userBio = activity?.getString(R.string.label_new_user)
                    newUser.admin = 0

                    usersViewModel.createUserAccount(newUser, imageUri!!)
                    user.sendEmailVerification()
                }

                Status.ERROR -> {
                    errorSigningUp(it.error!!)
                }
            }
        })
    }

    /**
     * Initialize function to observe User LiveData
     */
    private fun initUserObserver() {
        usersViewModel.userLiveData.observe(this, Observer {
            when(it.status) {
                Status.LOADING -> {
                    Timber.e("Creating User account...")
                }

                Status.SUCCESS -> {
                    proceedToMainActivity(it.user!!, signupButton)
                }

                Status.ERROR -> {
                    errorSigningUp(it.error!!)
                }
            }
        })
    }

    /**
     * Function to handle error registering
     */
    private fun errorSigningUp(error: String) {
        isSigningUp = false
        hideLoading()
        signupButton.revertAnimation()
        toast(error)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AVATAR_REQUEST && resultCode == RESULT_OK) {
            data.let { imageUri = it!!.data }
            startCropActivity(imageUri!!)
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                imageSelected = true
                val resultUri = result.uri

                signupAvatar?.load(resultUri.toString(), R.drawable.person)
                imageUri = resultUri

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Timber.e("Error cropping avatar")
            }
        }

    }

    private fun startCropActivity(imageUri: Uri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(context!!, this)
    }

    // Check if user has initiated signing up process. If in process, disable back button
    fun backPressOkay(): Boolean = !isSigningUp

    override fun onDestroy() {
        if (signupButton != null) signupButton.dispose()
        super.onDestroy()
    }


}
