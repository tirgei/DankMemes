package com.gelostech.dankmemes.utils;

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.gelostech.dankmemes.R
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.snackbar.Snackbar
import com.makeramen.roundedimageview.RoundedImageView
import de.hdodenhof.circleimageview.CircleImageView

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun ImageView.loadUrl(url: Int) {
    Glide.with(context)
            .setDefaultRequestOptions(RequestOptions()
                    .placeholder(R.drawable.loading))
            .load(url)
            .thumbnail(0.05f)
            .into(this)
}

fun ImageView.loadUrl(url: String) {
    Glide.with(context)
            .setDefaultRequestOptions(RequestOptions()
                    .placeholder(R.drawable.loading))
            .load(url)
            .thumbnail(0.05f)
            .into(this)
}

fun RoundedImageView.loadUrl(url: String) {
    Glide.with(context)
            .setDefaultRequestOptions(RequestOptions()
                    .placeholder(R.drawable.loading))
            .load(url)
            .thumbnail(0.05f)
            .into(this)
}

fun RoundedImageView.loadUrl(url: String, placeholer: String) {
    Glide.with(context)
            .setDefaultRequestOptions(RequestOptions()
                    .placeholder(R.drawable.loading))
            .load(url)
            .thumbnail(Glide.with(context).asDrawable().load(placeholer).thumbnail(0.1f))
            .into(this)
}

fun CircleImageView.loadUrl(url: Int) {
    Glide.with(context)
            .setDefaultRequestOptions(RequestOptions()
                    .placeholder(R.drawable.loading))
            .load(url)
            .thumbnail(0.05f)
            .into(this)
}

fun CircleImageView.loadUrl(url: String) {
    Glide.with(context.applicationContext)
            .setDefaultRequestOptions(RequestOptions()
                    .placeholder(R.drawable.person))
            .load(url)
            .thumbnail(0.05f)
            .into(this)
}

fun PhotoView.loadUrl(url: String) {
    Glide.with(context)
            .load(url)
            .thumbnail(0.05f)
            .into(this)
}

fun TextView.setFont(font: String) {
    this.typeface = Typeface.createFromAsset(context.assets, font)
}

fun TextView.setDrawable(icon: Drawable) {
    this.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
}

fun View.showView() {
    this.visibility = View.VISIBLE
}

fun View.hideView() {
    this.visibility = View.GONE
}

fun EditText.setDrawable(icon: Drawable) {
    this.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
}

inline fun View.snack(message: String, length: Int = Snackbar.LENGTH_LONG, f: Snackbar.() -> Unit) {
    val snack = Snackbar.make(this, message, length)
    snack.f()
    snack.show()
}

fun Snackbar.action(action: String, color: Int? = null, listener: (View) -> Unit) {
    setAction(action, listener)
    color?.let { setActionTextColor(ContextCompat.getColor(context, color)) }
}

inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().func().commit()
}

fun AppCompatActivity.addFragment(fragment: Fragment, frameId: Int){
    supportFragmentManager.inTransaction { add(frameId, fragment) }
}

fun AppCompatActivity.replaceFragment(fragment: Fragment, frameId: Int) {
    supportFragmentManager.inTransaction{ replace(frameId, fragment) }
}

class SemiSquareLayout : RelativeLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec - 80)
    }

}

class SquareLayout : RelativeLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

}