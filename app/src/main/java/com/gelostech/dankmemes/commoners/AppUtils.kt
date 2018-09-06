package com.gelostech.dankmemes.commoners

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StrictMode
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.gelostech.dankmemes.R
import com.google.firebase.storage.FirebaseStorage
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import de.hdodenhof.circleimageview.CircleImageView
import org.jetbrains.anko.toast
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


object AppUtils {
    private var TAG = AppUtils::class.java.simpleName

    /**
     * This function returns a FontAwesome drawable
     *
     * @param context Activity context
     * @param icon FontAwesome icon
     * @param color Color to set to icon
     * @param size Size of icon
     */
    fun setDrawable(context: Context, icon: IIcon, color: Int, size: Int): Drawable {
        return IconicsDrawable(context).icon(icon).color(ContextCompat.getColor(context, color)).sizeDp(size)
    }

    fun getColor(context: Context, color: Int): Int {
        return ContextCompat.getColor(context, color)
    }

    fun getBytes(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    fun getImage(image: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(image, 0, image.size)
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

    fun downloadPic(context: Context, url: String) {
        Toast.makeText(context, "Downloading...", Toast.LENGTH_SHORT).show()
        val directory = File("${Environment.getExternalStorageDirectory()}/DankMemes")

        if (!directory.exists()) {
            directory.mkdirs()
        }

        val mgr = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadLink = Uri.parse(url)
        val request = DownloadManager.Request(downloadLink)

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setDescription("Downloading meme...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setTitle(context.resources.getString(R.string.app_name))
                .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_PICTURES, "DankMeme-${System.currentTimeMillis()}.jpg" )
                .allowScanningByMediaScanner()

        mgr.enqueue(request)

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

    fun loadFromStorage(id: String, image: CircleImageView) {
        image.setImageResource(R.drawable.person)
        val bitmap = getBitmap(id)
        if (bitmap != null) {
            image.setImageBitmap(bitmap)
        }

        val avatarRef = FirebaseStorage.getInstance().reference.child("avatars").child(id)
        avatarRef.downloadUrl.addOnSuccessListener {
            ImageLoader.getInstance().loadImage(it.toString(), object : SimpleImageLoadingListener() {
                override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
                    super.onLoadingComplete(imageUri, view, loadedImage)
                    if (loadedImage != null) {
                        cacheBitmap(loadedImage, id)
                        image.setImageBitmap(loadedImage)
                    }
                }
            })
        }
    }

    fun cacheBitmap(bitmap: Bitmap?, name: String?) {
        val loader = ImageLoader.getInstance()
        if (bitmap == null || name == null) return
        try {
            loader.diskCache.remove(name)
        } catch (e: Exception) {
        }

        try {
            loader.diskCache.save(name, bitmap)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun cacheBitmap(url: String, name: String) {
        val loader = ImageLoader.getInstance()
        try {
            loader.diskCache.remove(name)
        } catch (e: Exception) {
        }

        try {
            loader.loadImage(url, object : SimpleImageLoadingListener(){
                override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
                    super.onLoadingComplete(imageUri, view, loadedImage)
                    if (loadedImage!=null)
                        loader.diskCache.save(name, loadedImage)
                }
            })
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun getBitmap(name: String): Bitmap? {
        val imageLoader = ImageLoader.getInstance()
        val f = imageLoader.diskCache.get(name)

        return if (f != null && f.exists()) {
            decodedBitmap(f.absolutePath, 500, 500, 85)
        } else {
            null
        }
    }

    private fun decodedBitmap(filePath: String, reqWidth: Int, reqHeight: Int, quality: Int): Bitmap? {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true //checks the dimensions
        try {
            BitmapFactory.decodeFile(filePath, options) //decodes without loading in memory
        } catch (e: Exception) {
        }

        options.inSampleSize = calculateSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        val bitmap = BitmapFactory.decodeFile(filePath, options)
        val bytearrayoutputstream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, quality, bytearrayoutputstream) ?: return null

        val BYTE = bytearrayoutputstream.toByteArray()

        return BitmapFactory.decodeByteArray(BYTE, 0, BYTE.size)
    }

    private fun calculateSampleSize(options: BitmapFactory.Options, rw: Int, rh: Int): Int {
        val width = options.outWidth
        val height = options.outHeight
        var inSampleSize = 1
        if (width > rw || height > rh) {
            val halfH = height / 2
            val halfW = width / 2
            while (halfH / inSampleSize > rh && halfW / inSampleSize > rw) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun animateEnterRight(activity: Activity) {
        activity.overridePendingTransition(R.anim.enter_b, R.anim.exit_a)
    }

    fun animateEnterLeft(activity: Activity) {
        activity.overridePendingTransition(R.anim.enter_a, R.anim.exit_b)
    }

    fun fadeIn(activity: Activity) {
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    fun highLightName(context: Context, title: String, start: Int, length: Int): SpannableString {
        val newName = SpannableString(title)
        newName.setSpan(StyleSpan(Typeface.BOLD), start, length, 0)
        newName.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.black)), start, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        return newName
    }


}