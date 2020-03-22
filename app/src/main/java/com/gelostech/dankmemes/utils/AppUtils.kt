package com.gelostech.dankmemes.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StrictMode
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.events.ScrollingEvent
import com.gelostech.dankmemes.ui.callbacks.StorageUploadListener
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.toast
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


object AppUtils {

    /**
     * This function returns a FontAwesome drawable
     *
     * @param context Activity context
     * @param icon FontAwesome icon
     * @param color Color to set to icon
     * @param size Size of icon
     */
    fun getDrawable(context: Context, icon: IIcon, color: Int, size: Int): Drawable {
        return IconicsDrawable(context).icon(icon).color(ContextCompat.getColor(context, color)).sizeDp(size)
    }

    /**k
     * Function to check storage permission is granted
     */
    fun requestStoragePermission(context: Context, granted: (Boolean) -> Unit) {
        Dexter.withActivity(context as Activity)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        granted(true)
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        granted(false)
                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {
                        token.continuePermissionRequest()
                    }
                }).check()
    }

    fun loadBitmapFromUrl(context: Context, url: String): Bitmap {
        return Glide.with(context)
                .asBitmap()
                .load(url)
                .submit()
                .get()
    }

    fun getColor(context: Context, color: Int): Int {
        return ContextCompat.getColor(context, color)
    }

    fun validated(vararg views: View): Boolean {
        var ok = true
        for (v in views) {
            if (v is EditText) {
                if (TextUtils.isEmpty(v.text.toString())) {
                    ok = false
                    v.error = "Required"
                }
            }
        }
        return ok
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return drawable.bitmap
            }
        }

        val bitmap: Bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Single color bitmap will be created of 1x1 pixel
        } else {
            Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        }

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun saveTemporaryImage(context: Context, bitmap: Bitmap) {
        val fileName: String? = "image"
        try {
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val fo = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            fo.write(bytes.toByteArray())
            fo.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveImage(context: Context, bitmap: Bitmap) {

        val file = File(Environment.getExternalStorageDirectory().toString() + File.separator + "Dank Memes")
        if(!file.exists()) file.mkdirs()

        val fileName = "Meme-" + System.currentTimeMillis() + ".jpg"

        val newImage = File(file, fileName)
        if(newImage.exists()) file.delete()
        try {
            val out = FileOutputStream(newImage)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()

            if (Build.VERSION.SDK_INT >= 19) {
                MediaScannerConnection.scanFile(context, arrayOf(newImage.absolutePath), null, null)
            } else {
                context.sendBroadcast( Intent("android.intent.action.MEDIA_MOUNTED", Uri.fromFile(newImage)))
            }
            context.toast("Meme saved")

        } catch (e: Exception){
            Log.d(javaClass.simpleName, e.localizedMessage)
        }

    }

    fun shareImage(context: Context, bitmap: Bitmap) {
        val share = Intent(Intent.ACTION_SEND)
        share.type = "image/*"

        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val f = File("${Environment.getExternalStorageDirectory()}/${File.separator}/temporary_file.jpg")

        try {
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
        } catch (e: IOException) {
                e.printStackTrace()
        }

        if(Build.VERSION.SDK_INT>=24){
           try{
              val m = StrictMode::class.java.getMethod("disableDeathOnFileUriExposure")
              m.invoke(null)
           }catch(e: Exception){
              e.printStackTrace()
           }
        }

        share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/temporary_file.jpg"))
        context.startActivity(Intent.createChooser(share, "Share Meme via..."))
    }

    fun slideRight(activity: Activity) {
        activity.overridePendingTransition(R.anim.enter_b, R.anim.exit_a)
    }

    fun slideLeft(activity: Activity) {
        activity.overridePendingTransition(R.anim.enter_a, R.anim.exit_b)
    }

    fun fadeIn(activity: Activity) {
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    /**
     * Function to highlight username
     */
    fun highLightName(context: Context, title: String, start: Int, length: Int): SpannableString {
        val newName = SpannableString(title)
        newName.setSpan(StyleSpan(Typeface.BOLD), start, length, 0)
        newName.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.color_text_primary)), start, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return newName
    }

    /**
     * Function to check provided username is valid
     */
    fun isValidUsername(name: String?): Boolean {
        if (name.isNullOrEmpty()) return false

        val username = name.toLowerCase(Locale.getDefault())
        if (username == "dank memes"
                || username.contains("dank")
                || username.contains("dank_memes")
                || username.contains("dank-memes"))
            return false

        return true
    }

    /**
     * FUnction to upload a file to FirebaseStorage
     * @param db - Firebase StorageReference
     * @param fileUri - Uri of the file
     */
    fun uploadFileToFirebaseStorage(db: StorageReference, fileUri: Uri, listener: StorageUploadListener) {
        val uploadTask = db.putFile(fileUri)
        uploadTask.continueWithTask {task ->
            if (!task.isSuccessful) {
                throw task.exception!!
            }
            db.downloadUrl
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                Timber.e("File uploaded...")
                listener.onFileUploaded(it.result.toString())
            } else {
                Timber.e("File not uploaded...")
                listener.onFileUploaded(null)
            }
        }
    }

    /**
     * Animate bounce effect
     */
    fun animateView(view: View) {
        val anim = AnimationUtils.loadAnimation(view.context, R.anim.bounce)
        val bounceInterpolator = MyBounceInterpolator(0.2, 20.0)
        anim.interpolator = bounceInterpolator

        view.startAnimation(anim)
    }

    /**
     * Generate random ID
     */
    fun randomIdGenerator(): String {
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

        return (1..16)
                .map { kotlin.random.Random.nextInt(0, charPool.size) }
                .map(charPool::get)
                .joinToString("")

    }

    /**
     * Function to show/hide home action button on scrolling
     */
    fun handleHomeScrolling(recyler: RecyclerView) {
        recyler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0) EventBus.getDefault().post(ScrollingEvent(false))
                else if (dy < 0) EventBus.getDefault().post(ScrollingEvent(true))
            }
        })
    }

}