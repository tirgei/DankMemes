package com.gelostech.dankmemes.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.Status
import com.gelostech.dankmemes.data.events.PostMemeEvent
import com.gelostech.dankmemes.ui.base.BaseActivity
import com.gelostech.dankmemes.data.models.Meme
import com.gelostech.dankmemes.ui.viewmodels.MemesViewModel
import com.gelostech.dankmemes.utils.*
import com.mikepenz.ionicons_typeface_library.Ionicons
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_post.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.alert
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class PostMemeActivity : BaseActivity() {
    private var imageUri: Uri? = null
    private var imageSelected = false
    private var uploadMeme: MenuItem? = null
    private val memesViewModel: MemesViewModel by viewModel()

    companion object {
        private const val GALLERY_REQUEST = 125
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        initViews()
        checkIfShareAction()

        initMemesObserver()
    }

    private fun initViews() {
        setSupportActionBar(postToolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = null
        }

        postAddImage.setOnClickListener {
            AppUtils.requestMediaPermission(this) { granted ->
                if (granted) {
                    val galleryIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY_REQUEST)
                } else longToast("Storage permission is required to select Avatar")
            }
        }
    }

    /**
     *  Check if activity was started by share intent from another app & if intent has image
     */
    private fun checkIfShareAction() {
        val intent = intent
        val action = intent.action
        val type = intent.type

        if (Intent.ACTION_SEND == action && type != null) {
            imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM) as? Uri
            imageUri?.let { startCropActivity(it) }
        }
    }

    /**
     * Initialize observer for Meme LiveData
     */
    private fun initMemesObserver() {
        memesViewModel.genericResponseLiveData.observe(this, Observer {
            when (it.status) {
                Status.LOADING -> {
                    showLoading("Posting meme...")
                }

                Status.SUCCESS -> {
                    hideLoading()
                    showSelectImage()
                    sessionManager.hasNewContent(true)
                    toast("Meme posted \uD83E\uDD2A\uD83E\uDD2A")
                }

                Status.ERROR -> {
                    hideLoading()
                    toast(it.error!!)
                }
            }
        })
    }

    /**
     * Function to post new Meme
     */
    private fun postMeme() {
        if (!Connectivity.isConnected(this)) {
            toast("Please turn on your internet connection")
            return
        }

        if (!sessionManager.isLoggedIn() || sessionManager.getUserId().isEmpty()) {
            toast("Please login first")
            return
        }

        if (imageUri == null || !imageSelected) {
            toast("Please select a meme")
            return
        }

        // Create new meme object
        val meme = Meme()
        meme.caption = postCaption.text.toString().trim()
        meme.likesCount = 0
        meme.commentsCount = 0
        meme.memePoster = sessionManager.getUsername()
        meme.memePosterAvatar = sessionManager.getUserAvatar()
        meme.memePosterID = sessionManager.getUserId()
        meme.time = System.currentTimeMillis()

        memesViewModel.postMeme(imageUri!!, meme)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_post_meme, menu)

        uploadMeme = menu?.findItem(R.id.menu_post)
        uploadMeme?.icon = AppUtils.getDrawable(this, Ionicons.Icon.ion_android_send, R.color.color_secondary, 20)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.menu_post -> postMeme()
        }

        return true
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
                Timber.e("Error cropping meme: ${result.error.localizedMessage}")
            }
        }
    }

    /**
     * Function to launch Image crop activity
     * @param imageUri - Selected image Uri
     */
    private fun startCropActivity(imageUri: Uri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this)
    }

    /**
     *  Image has been selected, show the image in ImageView and hide the select image button
     */
    private fun showSelectedImage() {
        Timber.e("Hiding button to select image")
        postAddImage.visibility = View.GONE
        postSelectImage.showView()
    }

    /**
     *  No image selected, hide ImageView and show the select image button
     */
    private fun showSelectImage() {
        imageSelected = false
        imageUri = null

        postSelectImage.hideView()
        showViews(postAddImage)
        postCaption.setText("")
    }

    override fun onBackPressed() {
        if (imageSelected) {
            alert("Remove selected image?") {
                positiveButton("Remove") {
                    showSelectImage()
                }
                negativeButton("Cancel") {}
            }.show()
        } else {
            super.onBackPressed()
            AppUtils.slideLeft(this)
        }
    }

}
