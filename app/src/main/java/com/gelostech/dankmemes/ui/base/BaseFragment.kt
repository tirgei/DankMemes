package com.gelostech.dankmemes.ui.base


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import org.jetbrains.anko.toast
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.models.User
import com.gelostech.dankmemes.ui.activities.MainActivity
import com.gelostech.dankmemes.utils.*
import com.google.firebase.messaging.FirebaseMessaging
import com.mikepenz.ionicons_typeface_library.Ionicons
import org.jetbrains.anko.longToast
import org.koin.android.ext.android.inject


open class BaseFragment : Fragment() {
    lateinit var progressDialog: ProgressDialog
    private val sessionManager: SessionManager by inject()
    fun isConnected(): Boolean = Connectivity.isConnected(activity!!)

    // Toast a message
    fun toast(message: String) {
        activity?.toast(message)
    }

    // Toast a message
    fun longToast(message: String) {
        activity?.longToast(message)
    }

    // User hasn't requested storage permission; request them to allow
    fun requestStoragePermission() {
        Dexter.withActivity(activity)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        activity?.toast("Storage permission is required!")
                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {
                        token.continuePermissionRequest()
                    }
                }).check()
    }

    // Check if user has granted storage permission
    fun storagePermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    // Get root database reference
    fun getDatabaseReference(): DatabaseReference = FirebaseDatabase.getInstance().reference

    // Get root firestore reference
    fun getFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    // Get FirebaseAuth instance
    fun getFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    // Get Firebase Storage reference
    fun getStorageReference(): StorageReference = FirebaseStorage.getInstance().reference

    // Get user ID
    fun getUid(): String {
        val user = FirebaseAuth.getInstance().currentUser

        return user!!.uid
    }


    fun showLoading(message: String) {
        progressDialog = ProgressDialog(activity)
        progressDialog.setCancelable(false)
        progressDialog.setMessage(message)
        progressDialog.show()
    }

    fun hideLoading() {
        if (::progressDialog.isInitialized && progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    fun proceedToMainActivity(user: User, button: CircularProgressButton) {
        // Set progress icon status
        val successIcon = AppUtils.setDrawable(activity!!, Ionicons.Icon.ion_checkmark_round, R.color.white, 25)
        val successfulButtonIcon: Bitmap = AppUtils.drawableToBitmap(successIcon)
        button.doneLoadingAnimation(AppUtils.getColor(activity!!, R.color.pink), successfulButtonIcon)

        hideLoading()
        sessionManager.saveUser(user)
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.TOPIC_GLOBAL)

        runDelayed(400) {
            longToast("Welcome ${user.userName} \uD83D\uDE03")

            startActivity(Intent(activity!!, MainActivity::class.java))
            activity!!.overridePendingTransition(R.anim.enter_b, R.anim.exit_a)
            activity!!.finish()
        }
    }

    fun animateView(view: View) {
        val anim = AnimationUtils.loadAnimation(context, R.anim.bounce)
        val bounceInterpolator = MyBounceInterpolator(0.2, 20.0)
        anim.interpolator = bounceInterpolator

        view.startAnimation(anim)
    }

}
