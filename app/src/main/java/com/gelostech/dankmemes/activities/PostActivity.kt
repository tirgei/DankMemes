package com.gelostech.dankmemes.activities

import android.app.Activity
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.commoners.BaseActivity
import com.gelostech.dankmemes.commoners.DankMemesUtil
import com.mikepenz.ionicons_typeface_library.Ionicons
import kotlinx.android.synthetic.main.activity_post.*
import android.content.Intent
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import android.R.attr.data
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import org.jetbrains.anko.toast
import gun0912.tedbottompicker.TedBottomPicker




class PostActivity : BaseActivity(), View.OnClickListener {
    private var imageUri: Uri? = null
    private var imageSelected = false
    private var uploadMeme: MenuItem? = null

    companion object {
        private const val GALLERY_REQUEST = 1
        private var TAG = PostActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        initViews()
        checkIfShareAction()
    }

    private fun initViews() {
        setSupportActionBar(postToolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Post new meme"

        postAddImage.setImageDrawable(DankMemesUtil.setDrawable(this, Ionicons.Icon.ion_image, R.color.secondaryText, 65))
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
        uploadMeme?.icon = DankMemesUtil.setDrawable(this, Ionicons.Icon.ion_android_send, R.color.colorAccent, 20)

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
