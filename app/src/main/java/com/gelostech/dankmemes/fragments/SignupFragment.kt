package com.gelostech.dankmemes.fragments


import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
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
import com.gelostech.dankmemes.commoners.DankMemesUtil.getColor
import com.gelostech.dankmemes.commoners.DankMemesUtil.setDrawable
import com.gelostech.dankmemes.models.UserModel
import com.gelostech.dankmemes.utils.*
import com.google.firebase.auth.*
import com.google.firebase.iid.FirebaseInstanceId
import com.mikepenz.ionicons_typeface_library.Ionicons
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_signup.*
import org.jetbrains.anko.toast
import com.gelostech.dankmemes.utils.PreferenceHelper.set
import com.google.firebase.messaging.FirebaseMessaging

class SignupFragment : BaseFragment() {
    private lateinit var signupSuccessful: Bitmap
    private var imageUri: Uri? = null
    private var imageSelected = false
    private var isCreatingAccount = false
    private lateinit var prefs: SharedPreferences

    companion object {
        private val TAG = SignupFragment::class.java.simpleName
        private const val AVATAR_REQUEST = 1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        val successfulIcon = setDrawable(activity!!, Ionicons.Icon.ion_checkmark_round, R.color.white, 25)
        signupSuccessful = drawableToBitmap(successfulIcon)
        prefs = PreferenceHelper.defaultPrefs(activity!!)

        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        signupUsername.setDrawable(setDrawable(activity!!, Ionicons.Icon.ion_person, R.color.secondaryText, 18))
        signupEmail.setDrawable(setDrawable(activity!!, Ionicons.Icon.ion_ios_email, R.color.secondaryText, 18))
        signupPassword.setDrawable(setDrawable(activity!!, Ionicons.Icon.ion_android_lock, R.color.secondaryText, 18))
        signupConfirmPassword.setDrawable(setDrawable(activity!!, Ionicons.Icon.ion_android_lock, R.color.secondaryText, 18))

        signupAvatar.setOnClickListener {
            if (!isCreatingAccount) {
                if (storagePermissionGranted()) {
                    val galleryIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, AVATAR_REQUEST)

                    //pickImageFromGallery()
                } else {
                    requestStoragePermission()
                }
            }
        }

        signupLogin.setOnClickListener {
            if (!isCreatingAccount) {
                if (activity!!.supportFragmentManager.backStackEntryCount > 0)
                    activity!!.supportFragmentManager.popBackStackImmediate()
                else
                    (activity as AppCompatActivity).replaceFragment(LoginFragment(), R.id.loginHolder)
            } else activity!!.toast("Please wait...")
        }

        signupTerms.setOnClickListener {
           if (!isCreatingAccount) {
               val i = Intent(Intent.ACTION_VIEW)
               i.data = Uri.parse("https://sites.google.com/view/dankmemesapp/terms-and-conditions")
               startActivity(i)
           } else activity!!.toast("Please wait...")
        }

        signupButton.setOnClickListener {
            val user = getFirebaseAuth().currentUser

            if (user != null && user.isAnonymous) {
                linkToId()
            } else {
                signUp()
            }
        }
    }

    private fun signUp() {
        // Check if all fields are filled
        if (!DankMemesUtil.validated(signupUsername, signupEmail, signupPassword, signupConfirmPassword)) return

        val name = signupUsername.text.toString().trim()
        val email = signupEmail.text.toString().trim()
        val pw = signupPassword.text.toString().trim()
        val confirmPw = signupConfirmPassword.text.toString().trim()

        // Check if password and confirm password match
        if (pw != confirmPw) {
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

        // Create new user
        isCreatingAccount = true
        signupButton.startAnimation()
        getFirebaseAuth().createUserWithEmailAndPassword(email, pw)
                .addOnCompleteListener(activity!!, {task ->
                    if (task.isSuccessful) {
                        signupButton.doneLoadingAnimation(getColor(activity!!, R.color.pink), signupSuccessful)
                        Log.e(TAG, "signingIn: Success!")

                        // update UI with the signed-in user's information
                        val user = task.result.user
                        updateUI(user)

                        prefs["username"] = name
                        prefs["email"] = email

                    } else {
                        try {
                            throw task.exception!!
                        } catch (weakPassword: FirebaseAuthWeakPasswordException){
                            isCreatingAccount = false
                            signupButton.revertAnimation()
                            signupPassword.error = "Please enter a stronger password"

                        } catch (userExists: FirebaseAuthUserCollisionException) {
                            isCreatingAccount = false
                            signupButton.revertAnimation()
                            activity?.toast("Account already exists. Please log in.")

                        } catch (malformedEmail: FirebaseAuthInvalidCredentialsException) {
                            isCreatingAccount = false
                            signupButton.revertAnimation()
                            signupEmail.error = "Incorrect email format"

                        } catch (e: Exception) {
                            isCreatingAccount = false
                            signupButton.revertAnimation()
                            Log.e(TAG, "signingIn: Failure - $e}" )
                            activity?.toast("Error signing up. Please try again.")
                        }
                    }
                })


    }

    // User was signed in anonymously, sign them up
    private fun linkToId() {
        // Check if all fields are filled
        if (!DankMemesUtil.validated(signupUsername, signupEmail, signupPassword, signupConfirmPassword)) return

        val name = signupUsername.text.toString().trim()
        val email = signupEmail.text.toString().trim()
        val pw = signupPassword.text.toString().trim()
        val confirmPw = signupConfirmPassword.text.toString().trim()

        // Check if password and confirm password match
        if (pw != confirmPw) {
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

        isCreatingAccount = true
        signupButton.startAnimation()
        val credential =  EmailAuthProvider.getCredential(email, pw)

        getFirebaseAuth().currentUser!!.linkWithCredential(credential)
                .addOnCompleteListener(activity!!, {task ->
                    if (task.isSuccessful) {
                        Log.e(TAG, "signingIn: Success!")

                        // update UI with the signed-in user's information
                        val user = task.result.user
                        updateUI(user)

                        prefs["username"] = name
                        prefs["email"] = email

                    } else {
                        try {
                            throw task.exception!!
                        } catch (weakPassword: FirebaseAuthWeakPasswordException){
                            isCreatingAccount = false
                            signupButton.revertAnimation()
                            signupPassword.error = "Please enter a stronger password"

                        } catch (userExists: FirebaseAuthUserCollisionException) {
                            isCreatingAccount = false
                            signupButton.revertAnimation()
                            activity?.toast("Account already exists. Please log in.")

                        } catch (malformedEmail: FirebaseAuthInvalidCredentialsException) {
                            isCreatingAccount = false
                            signupButton.revertAnimation()
                            signupEmail.error = "Incorrect email format"

                        } catch (e: Exception) {
                            isCreatingAccount = false
                            signupButton.revertAnimation()
                            Log.e(TAG, "signingIn: Failure - $e}" )
                            activity?.toast("Error signing up. Please try again.")
                        }
                    }
                })

    }

    private fun updateUI(user: FirebaseUser) {
        val token = FirebaseInstanceId.getInstance().token
        val id = user.uid

        val newUser = UserModel()
        newUser.userName = signupUsername.text.toString().trim()
        newUser.userEmail = user.email
        newUser.dateCreated = TimeFormatter().getNormalYear(System.currentTimeMillis())
        newUser.userToken = token
        newUser.userId = id
        newUser.userBio = activity?.getString(R.string.new_user_bio)

        val ref = getStorageReference().child("avatars").child(id)
        val uploadTask = ref.putFile(imageUri!!)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception!!
            }
            Log.d(TAG, "Image uploaded")

            // Continue with the task to getBitmap the download URL
            ref.downloadUrl
        }.addOnCompleteListener({ task ->
            if (task.isSuccessful) {
                newUser.userAvatar =  task.result.toString()

                user.sendEmailVerification()
                FirebaseMessaging.getInstance().subscribeToTopic("memes")
                getDatabaseReference().child("users").child(id).setValue(newUser).addOnCompleteListener {
                    signupButton.doneLoadingAnimation(getColor(activity!!, R.color.pink), signupSuccessful)

                    activity!!.toast("Welcome ${signupUsername.text.toString().trim()}")
                    startActivity(Intent(activity!!, MainActivity::class.java))
                    activity!!.overridePendingTransition(R.anim.enter_b, R.anim.exit_a)
                    activity!!.finish()
                }
            } else {
                signupButton.revertAnimation()
                activity?.toast("Error signing up. Please try again.")
                Log.d(TAG, "Error signing up: ${task.exception}")
            }
        })
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AVATAR_REQUEST && resultCode == RESULT_OK) {
            data.let { imageUri = it!!.data }

            startCropActivity(imageUri!!)
            Log.e(TAG, "Avatar uri: ${data.toString()}")
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                imageSelected = true
                val resultUri = result.uri
                Log.e(TAG, "Avatar: $resultUri")

                signupAvatar?.loadUrl(resultUri.toString())
                imageUri = resultUri

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Log.d(TAG, "Cropping error: ${result.error.message}")
            }
        }

    }

    private fun startCropActivity(imageUri: Uri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(context!!, this)
    }

    // Check if user has initiated signing up process. If in process, disable back button
    fun backPressOkay(): Boolean = !isCreatingAccount

    override fun onDestroy() {
        if (signupButton != null) signupButton.dispose()
        super.onDestroy()
    }


}
