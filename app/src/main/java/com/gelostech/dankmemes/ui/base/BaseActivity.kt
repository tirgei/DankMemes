package com.gelostech.dankmemes.ui.base

import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.utils.Constants
import com.gelostech.dankmemes.utils.SessionManager
import com.gelostech.dankmemes.utils.TimeFormatter
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.DatabaseReference
import com.google.firebase.iid.FirebaseInstanceId
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import org.koin.android.ext.android.inject
import timber.log.Timber


open class BaseActivity : AppCompatActivity() {
    private lateinit var progressDialog: ProgressDialog
    val sessionManager: SessionManager by inject()
    private val firebaseDatabase: DatabaseReference by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this))
        progressDialog = ProgressDialog(this)

        MobileAds.initialize(this) {
            Timber.e("Admob initialized: %s", it.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        updateLastActive()
    }

    // Set dark status bar
    fun setDarkStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }
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

    // Get user ID
    fun getUid(): String = sessionManager.getUserId()

    private fun updateLastActive() {
        firebaseDatabase.child(Constants.METADATA)
                .child(Constants.LAST_ACTIVE)
                .child(TimeFormatter().getFullYear(System.currentTimeMillis()))
                .child(getUid())
                .setValue(TimeFormatter().getTime(System.currentTimeMillis()))
    }
}
