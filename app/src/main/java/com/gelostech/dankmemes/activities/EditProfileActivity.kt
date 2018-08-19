package com.gelostech.dankmemes.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.commoners.BaseActivity
import com.gelostech.dankmemes.commoners.AppUtils
import com.gelostech.dankmemes.commoners.AppUtils.drawableToBitmap
import com.gelostech.dankmemes.commoners.AppUtils.setDrawable
import com.gelostech.dankmemes.models.UserModel
import com.gelostech.dankmemes.utils.PreferenceHelper
import com.gelostech.dankmemes.utils.PreferenceHelper.set
import com.gelostech.dankmemes.utils.loadUrl
import com.mikepenz.ionicons_typeface_library.Ionicons
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_edit_profile.*
import org.jetbrains.anko.toast

class EditProfileActivity : BaseActivity() {
    private var imageUri: Uri? = null
    private var imageSelected = false
    private var changedAvatar = false
    private var isUpdating = false
    private lateinit var updateSuccessful: Bitmap
    private lateinit var prefs: SharedPreferences

    companion object {
        private const val AVATAR_REQUEST = 1
        private var TAG = EditProfileActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        initViews()
        loadProfile()

        prefs = PreferenceHelper.defaultPrefs(this)
    }

    private fun initViews() {
        setSupportActionBar(editProfileToolbar)
        supportActionBar?.title = getString(R.string.edit_profile)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        editProfilePickImage.setImageDrawable(AppUtils.setDrawable(this, Ionicons.Icon.ion_camera, R.color.white, 18))

        editProfilePickImage.setOnClickListener {
            if (storagePermissionGranted()) {
                val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent, AVATAR_REQUEST)

            } else {
                requestStoragePermission()
            }
        }

        editProfileButton.setOnClickListener {
            if (changedAvatar) {
                updateAvatar()
            } else {
                updateDetails()
            }
        }

        val successfullyUpdatedIcon = setDrawable(this, Ionicons.Icon.ion_checkmark_round, R.color.white, 25)
        updateSuccessful = drawableToBitmap(successfullyUpdatedIcon)
    }

    private fun loadProfile() {
        val user = intent.getSerializableExtra("user") as UserModel

        editProfileImage.loadUrl(user.userAvatar!!)
        editProfileName.setText(user.userName)
        editProfileBio.setText(user.userBio)
    }

    // User has changed profile picture
    private fun updateAvatar() {
        if (!AppUtils.validated(editProfileName, editProfileBio)) return

        editProfileButton.startAnimation()
        isUpdating = true

        val ref = getStorageReference().child("avatars").child(getUid())
        val uploadTask = ref.putFile(imageUri!!)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception!!
            }
            Log.d(TAG, "Image uploaded")

            // Continue with the task to get the download URL
            ref.downloadUrl
        }.addOnCompleteListener({ task ->
            if (task.isSuccessful) {
                val name = editProfileName.text.toString().trim()
                val bio = editProfileBio.text.toString().trim()

                val userRef = getDatabaseReference().child("users").child(getUid())
                userRef.child("userName").setValue(name)
                userRef.child("userBio").setValue(bio)
                userRef.child("userAvatar").setValue(task.result.toString())

                prefs["username"] = name

                Handler().postDelayed({
                    editProfileButton.revertAnimation()
                    isUpdating = false

                    toast("Profile updated!")
                }, 500)

            } else {
                editProfileButton.revertAnimation()
                toast("Error updating profile. Please try again.")
                Log.d(TAG, "Error updating profile: ${task.exception}")
            }
        })
    }

    // User has updated details only, profile picture still same
    private fun updateDetails() {
        if (!AppUtils.validated(editProfileName, editProfileBio)) return

        editProfileButton.startAnimation()
        isUpdating = true

        val name = editProfileName.text.toString().trim()
        val bio = editProfileBio.text.toString().trim()

        val userRef = getDatabaseReference().child("users").child(getUid())
        userRef.child("userName").setValue(name)
        userRef.child("userBio").setValue(bio)

        prefs["username"] = name

        Handler().postDelayed({
            editProfileButton.revertAnimation()
            isUpdating = false

            toast("Profile updated!")
        }, 500)
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
                imageSelected = true
                val resultUri = result.uri

                editProfileImage.setImageURI(resultUri)
                changedAvatar = true
                imageUri = resultUri

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Log.d(TAG, "Cropping error: ${result.error.message}")
            }
        }

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
            overridePendingTransition(R.anim.enter_a, R.anim.exit_b)
        }
    }
}
