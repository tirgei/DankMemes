package com.gelostech.dankmemes.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.commoners.BaseActivity
import com.gelostech.dankmemes.commoners.AppUtils
import com.gelostech.dankmemes.models.MemeModel
import com.gelostech.dankmemes.utils.PreferenceHelper
import com.mikepenz.ionicons_typeface_library.Ionicons
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_post.*
import org.jetbrains.anko.toast
import com.gelostech.dankmemes.utils.PreferenceHelper.get

class PostActivity : BaseActivity(), View.OnClickListener {
    private var imageUri: Uri? = null
    private var imageSelected = false
    private var uploadMeme: MenuItem? = null
    private lateinit var prefs: SharedPreferences

    companion object {
        private const val GALLERY_REQUEST = 1
        private var TAG = PostActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        initViews()
        checkIfShareAction()

        prefs = PreferenceHelper.defaultPrefs(this)
    }

    private fun initViews() {
        setSupportActionBar(postToolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Post new meme"

        postAddImage.setImageDrawable(AppUtils.setDrawable(this, Ionicons.Icon.ion_image, R.color.secondaryText, 65))
        postAddImage.setOnClickListener(this)
    }

    /**
     *  Check if activity was started by share intent from another app & if intent has image
     */
    private fun checkIfShareAction() {
        val intent = intent
        val action = intent.action
        val type = intent.type

        if (Intent.ACTION_SEND == action && type != null) {
            imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM) as Uri
            startCropActivity(imageUri!!)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_post_meme, menu)

        uploadMeme = menu?.findItem(R.id.menu_post)
        uploadMeme?.icon = AppUtils.setDrawable(this, Ionicons.Icon.ion_android_send, R.color.colorAccent, 20)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.menu_post -> postMeme()
        }

        return true
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            postAddImage.id -> {
                if (storagePermissionGranted()) {
                    val galleryIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY_REQUEST)

                } else {
                    requestStoragePermission()
                }
            }
        }
    }

    private fun startCropActivity(imageUri: Uri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_REQUEST && resultCode == Activity.RESULT_OK) {
            data.let { imageUri = it!!.data }

            startCropActivity(imageUri!!)
        }


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                imageSelected = true
                val resultUri = result.uri

                postSelectImage.setImageURI(resultUri)
                imageUri = resultUri
                showSelectedImage()

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Log.d(TAG, "Cropping error: ${result.error.message}")
            }
        }

    }

    private fun postMeme() {
        if (imageUri == null) {
            toast("Please select a meme...")
            return
        }

        if (!imageSelected) return

        showLoading("Posting meme...")
        val id = getDatabaseReference().child("dank-memes").push().key

        // Create new meme object
        val meme = MemeModel()
        meme.id = id
        meme.caption = postCaption.text.toString().trim()
        meme.likesCount = 0
        meme.commentsCount = 0
        meme.memePoster = prefs["username"]
        meme.memePosterID = getUid()
        meme.time = System.currentTimeMillis()

        val ref = getStorageReference().child("memes").child(getUid()).child(id!!)
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
                meme.imageUrl = task.result.toString()

                getDatabaseReference().child("dank-memes").child(id).setValue(meme).addOnCompleteListener {
                    hideLoading()
                    toast("Meme posted!")
                    showSelectImage()
                    postCaption.setText("")
                }

            } else {
                toast("Error updating profile. Please try again.")
                Log.d(TAG, "Error updating profile: ${task.exception}")
            }
        })

    }

    /**
     *  Image has been selected, show the image in ImageView and hide the select image button
     */
    private fun showSelectedImage() {
        postAddImage.visibility = View.GONE
        postSelectImage.visibility = View.VISIBLE
    }

    /**
     *  No image selected, hide ImageView and show the select image button
     */
    private fun showSelectImage() {
        imageSelected = false
        imageUri = null

        postSelectImage.visibility = View.GONE
        postAddImage.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        if (imageSelected) {
            showSelectImage()
        } else {
            super.onBackPressed()
            overridePendingTransition(R.anim.enter_a, R.anim.exit_b)
        }
    }

}
