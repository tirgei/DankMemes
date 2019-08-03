package com.gelostech.dankmemes.commoners

import android.Manifest
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.gelostech.dankmemes.utils.TimeFormatter
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import timber.log.Timber


open class BaseActivity : AppCompatActivity() {
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this))
        progressDialog = ProgressDialog(this)

        MobileAds.initialize(this) {
            Timber.e("Admob initialized: %s", it.toString())
        }
    }

    // User hasn't requested storage permission; request them to allow
    fun requestStoragePermission() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {

                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {

                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {
                        token.continuePermissionRequest()
                    }
                }).check()
    }

    // Show progress dialog
    fun showLoading(message: String) {
        progressDialog.setMessage(message)
        progressDialog.setCancelable(false)
        progressDialog.show()
    }

    fun hideLoading() {
        progressDialog.dismiss()
    }

    // Check if user has granted storage permission
    fun storagePermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    // Get root database reference
    fun getDatabaseReference(): DatabaseReference = FirebaseDatabase.getInstance().reference

    // Get root firestore reference
    fun getFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    // Get Firebase Storage reference
    fun getStorageReference(): StorageReference = FirebaseStorage.getInstance().reference

    // Get FirebaseAuth instance
    fun getFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    // Get user ID
    fun getUid(): String {
        val user = FirebaseAuth.getInstance().currentUser
        return user!!.uid
    }

    fun refreshToken() {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
            if (it.isSuccessful) {
                val token = it.result?.token
                getDatabaseReference().child("users").child(getUid()).child("userToken").setValue(token)
            }
        }
    }

    fun updateLastActive() {
        getDatabaseReference()
                .child(Config.METADATA)
                .child(Config.LAST_ACTIVE)
                .child(TimeFormatter().getFullYear(System.currentTimeMillis()))
                .child(getUid())
                .setValue(TimeFormatter().getTime(System.currentTimeMillis()))
    }
}
