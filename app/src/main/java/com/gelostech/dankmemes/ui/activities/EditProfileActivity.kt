package com.gelostech.dankmemes.ui.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import androidx.lifecycle.Observer
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.Status
import com.gelostech.dankmemes.data.responses.GenericResponse
import com.gelostech.dankmemes.ui.base.BaseActivity
import com.gelostech.dankmemes.ui.viewmodels.UsersViewModel
import com.gelostech.dankmemes.utils.AppUtils
import com.gelostech.dankmemes.utils.Constants
import com.gelostech.dankmemes.utils.TimeFormatter
import com.gelostech.dankmemes.utils.load
import com.mikepenz.ionicons_typeface_library.Ionicons
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_edit_profile.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class EditProfileActivity : BaseActivity() {
    private var imageUri: Uri? = null
    private var changedAvatar = false
    private var isUpdating = false
    private val usersViewModel: UsersViewModel by viewModel()

    companion object {
        private const val AVATAR_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        initViews()
        initResponseObserver()

        loadProfile()
    }

    private fun initViews() {
        setSupportActionBar(editProfileToolbar)
        supportActionBar?.apply {
            title = null
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        editProfilePickImage.setImageDrawable(AppUtils.getDrawable(this, Ionicons.Icon.ion_camera, R.color.white, 18))

        editProfilePickImage.setOnClickListener {
            AppUtils.requestStoragePermission(this) {granted ->
                if (granted) {
                    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, AVATAR_REQUEST)
                } else longToast("Storage permission is required to select Avatar")
            }
        }

        editProfileButton.setOnClickListener {
            if (changedAvatar) {
                usersViewModel.updateUserAvatar(sessionManager.getUserId(), imageUri!!)
            } else if (hasUpdatedDetails()) {
                updateDetails()
            }
        }
    }

    /**
     * Initialize function to observer Response LiveData
     */
    private fun initResponseObserver() {
        usersViewModel.genericResponseLiveData.observe(this, Observer {
            when (it.status) {
                Status.LOADING -> {
                    Timber.e("Loading...")
                    isUpdating = true
                    if (!editProfileButton.isActivated) editProfileButton.startAnimation()
                }

                Status.SUCCESS -> {
                    when (it.item) {
                        GenericResponse.ITEM_RESPONSE.UPDATE_AVATAR -> {
                            toast("Avatar updated")

                            val avatar = it.value!!
                            sessionManager.updateUser(Constants.AVATAR, avatar)
                            updateDetails(avatar)
                        }

                        else -> {
                            stopUpdating()
                            toast("Profile updated \uD83D\uDD7A \uD83D\uDC83")

                            sessionManager.updateUser(Constants.USERNAME, editProfileName.text.toString())
                            sessionManager.updateUser(Constants.USER_BIO, editProfileBio.text.toString())
                            loadProfile()
                        }
                    }
                }

                Status.ERROR -> {
                    stopUpdating()
                    toast("${it.error}. Please try again")
                }
            }
        })
    }

    private fun loadProfile() {
        val user = sessionManager.getUser()

        editProfileImage.load(user.userAvatar!!, R.drawable.person)
        editProfileName.setText(user.userName)
        editProfileBio.setText(user.userBio)
    }

    // User has updated details only, profile picture still same
    private fun updateDetails(avatar: String? = null) {
        if (!AppUtils.validated(editProfileName, editProfileBio)) return

        val name = editProfileName.text.toString().trim()
        val bio = editProfileBio.text.toString().trim()

        // Check username
        if (!AppUtils.isValidUsername(name)) {
            editProfileName.error = "Invalid name"
            return
        }

        usersViewModel.updateUserDetails(sessionManager.getUserId(), name, bio, avatar)
    }

    private fun startCropActivity(imageUri: Uri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AVATAR_REQUEST && resultCode == Activity.RESULT_OK) {
            data.let { imageUri = it!!.data }

            startCropActivity(imageUri!!)
        }


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri = result.uri

                editProfileImage.setImageURI(resultUri)
                changedAvatar = true
                imageUri = resultUri

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Timber.e("Cropping error: ${result.error.message}")
            }
        }

    }

    /**
     * Check if User has updated their name or bio
     */
    private fun hasUpdatedDetails(): Boolean {
        val name = editProfileName.text.toString().trim()
        val bio = editProfileBio.text.toString().trim()

        return name != sessionManager.getUsername() || bio != sessionManager.getBio()
    }

    /**
     * Stop loading animations
     */
    private fun stopUpdating() {
        hideLoading()
        isUpdating = false
        editProfileButton.revertAnimation()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> onBackPressed()
        }

        return true
    }

    override fun onBackPressed() {
        if (isUpdating) {
            toast("Please wait...")
        } else {
            super.onBackPressed()
            AppUtils.slideLeft(this)
        }
    }
}
