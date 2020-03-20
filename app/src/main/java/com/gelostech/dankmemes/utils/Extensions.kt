package com.gelostech.dankmemes.utils;

import android.graphics.drawable.Drawable
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

/**
 * Bind layout using legacy inflate i.e. not using DataBinding
 */
fun ViewGroup.legacyInflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

/**
 * Bind layout to respective ViewDataBinding implementation
 */
inline fun <reified T : ViewDataBinding> ViewGroup.inflate(@LayoutRes layoutRes: Int): T {
    return DataBindingUtil.inflate(LayoutInflater.from(context), layoutRes, this, false)
}

/**
 * Load image to ImageView
 * @param url - Url of the image, can be Int, drawable or String
 * @param placeholder - Placeholder to show when loading image
 */
fun ImageView.load(url: Any, placeholder: Int) {
    Glide.with(context)
            .setDefaultRequestOptions(RequestOptions().placeholder(placeholder))
            .load(url)
            .thumbnail(0.05f)
            .error(placeholder)
            .into(this)
}

/**
 * Load image to ImageView
 * @param url - Url of the image, can be Int, drawable or String
 * @param placeholder - Placeholder to show when loading image
 */
fun ImageView.load(url: Any, placeholder: Drawable) {
    Glide.with(context)
            .setDefaultRequestOptions(RequestOptions().placeholder(placeholder))
            .load(url)
            .thumbnail(0.05f)
            .error(placeholder)
            .into(this)
}

/**
 * Load image to ImageView
 * @param url - Url of the image, can be Int, drawable or String
 * @param placeholder - Placeholder to show when loading image
 * @param thumbnail - Image thumbnail url
 */
fun ImageView.load(url: Any, placeholder: Int, thumbnail: String) {
    Glide.with(context)
            .setDefaultRequestOptions(RequestOptions()
                    .placeholder(placeholder))
            .load(url)
            .thumbnail(Glide.with(context).asDrawable().load(thumbnail).thumbnail(0.1f))
            .into(this)
}

/**
 * Load image to ImageView
 * @param url - Url of the image, can be Int, drawable or String
 * @param placeholder - Placeholder to show when loading image
 * @param thumbnail - Image thumbnail url
 */
fun ImageView.load(url: Any, placeholder: Drawable, thumbnail: String) {
    Glide.with(context)
            .setDefaultRequestOptions(RequestOptions()
                    .placeholder(placeholder))
            .load(url)
            .thumbnail(Glide.with(context).asDrawable().load(thumbnail).thumbnail(0.1f))
            .into(this)
}

/**
 * Set drawable to the left of TextView
 * @param - Drawable to set
 */
fun TextView.setDrawable(icon: Drawable) {
    this.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
}

/**
 * Set drawable to the left of TextView
 * @param - Drawable to set
 */
fun TextView.setRightDrawable(icon: Drawable) {
    this.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null)
}

/**
 * Show hidden view
 */
fun View.showView() {
    if (!this.isShown) this.visibility = View.VISIBLE
}

/**
 * Hide View
 */
fun View.hideView() {
    if (this.isShown) this.visibility = View.GONE
}

/**
 * Show multiple views
 */
fun showViews(vararg views: View) {
    views.forEach { view -> view.showView() }
}

/**
 * Hide multiple views
 */
fun hideViews(vararg views: View) {
    views.forEach { view -> view.hideView() }
}

/**
 * Set Drawable to the left of EditText
 * @param icon - Drawable to set
 */
fun EditText.setDrawable(icon: Drawable) {
    this.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
}

/**
 * Function to handle SupportFragmentManager transactions
 */
inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().func().commit()
}

/**
 * Function to add Fragment instance to layout
 * @param fragment - Instance of fragment to add
 * @param frameId - Id of the layout resource to add the Fragment
 */
fun AppCompatActivity.addFragment(fragment: Fragment, frameId: Int){
    supportFragmentManager.inTransaction { add(frameId, fragment) }
}

/**
 * Function to replace Fragment instance in layout
 * @param fragment - Instance of fragment to add
 * @param frameId - Id of the layout resource to replace the Fragment
 */
fun AppCompatActivity.replaceFragment(fragment: Fragment, frameId: Int) {
    supportFragmentManager.inTransaction{ replace(frameId, fragment) }
}

/**
 * Function to run a delayed function
 * @param millis - Time to delay
 * @param function - Function to execute
 */
fun runDelayed(millis: Long, function: () -> Unit) {
    Handler().postDelayed(function, millis)
}

/**
 * Function to wrap Firebase Database to Task
 */
fun DatabaseReference.get(): Task<DataSnapshot> {
    val taskSource = TaskCompletionSource<DataSnapshot>()

    this.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
            taskSource.setException(p0.toException())
        }

        override fun onDataChange(p0: DataSnapshot) {
            if (p0.exists())
                taskSource.setResult(p0)
            else
                taskSource.setException(Exception("Not found"))
        }
    })

    return taskSource.task
}
