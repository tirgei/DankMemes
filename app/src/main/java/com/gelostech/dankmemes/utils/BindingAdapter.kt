package com.gelostech.dankmemes.utils

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.models.Comment
import com.gelostech.dankmemes.data.models.Fave
import com.gelostech.dankmemes.data.models.Notification
import com.gelostech.dankmemes.ui.callbacks.CommentsCallback
import com.gelostech.dankmemes.ui.callbacks.FavesCallback
import com.makeramen.roundedimageview.RoundedImageView
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.ionicons_typeface_library.Ionicons
import org.koin.core.KoinComponent
import org.koin.core.inject

object BindingAdapter : KoinComponent {
    private val sessionManager: SessionManager by inject()
    private val myUserId = sessionManager.getUserId()

    /**
     * Bind Image to layout
     */
    @JvmStatic
    @BindingAdapter(value = ["image", "placeholder", "thumbnail"], requireAll = false)
    fun loadImage(view: ImageView, image: Any?, placeholder: Int, thumbnail: String? = null) {
        image?.let {
            if (thumbnail.isNullOrEmpty())
                view.load(it, placeholder)
            else
                view.load(it, placeholder, thumbnail)
        }
    }

    /**
     * Bind Image to layout
     */
    @JvmStatic
    @BindingAdapter(value = ["image", "placeholder", "thumbnail"], requireAll = false)
    fun loadImage(view: ImageView, image: Any?, placeholder: Drawable, thumbnail: String? = null) {
        image?.let {
            if (thumbnail.isNullOrEmpty())
                view.load(it, placeholder)
            else
                view.load(it, placeholder, thumbnail)
        }
    }

    /**
     * Set layout visibility
     */
    @JvmStatic
    @BindingAdapter("visibility")
    fun setVisibility(view: View, visible: Boolean) {
        when (visible) {
            true -> view.showView()
            false -> view.hideView()
        }
    }

    /**
     * Set likes to Meme post
     */
    @JvmStatic
    @BindingAdapter("likeStatus")
    fun setLikeStatus(view: TextView, likes: MutableMap<String, Boolean>?) {
        val context = view.context

        likes?.let {
            when (likes.containsKey(myUserId)) {
                true -> {
                    view.setDrawable(AppUtils.getDrawable(context, FontAwesome.Icon.faw_thumbs_up2, R.color.color_secondary, 20))
                    view.setTextColor(ContextCompat.getColor(context, R.color.color_secondary))
                }

                else -> {
                    view.setDrawable(AppUtils.getDrawable(context, FontAwesome.Icon.faw_thumbs_up, R.color.color_text_secondary, 20))
                    view.setTextColor(ContextCompat.getColor(context, R.color.color_text_secondary))
                }
            }
        }
    }

    /**
     * Set favorite status to Meme post
     */
    @JvmStatic
    @BindingAdapter("faveStatus")
    fun setFaveStatus(view: ImageButton, faves: MutableMap<String, Boolean>?) {
        val context = view.context

        faves?.let {
            if (!faves.containsKey(myUserId))
                view.setImageDrawable(AppUtils.getDrawable(context, Ionicons.Icon.ion_ios_heart_outline, R.color.color_text_secondary, 19))
            else
                view.setImageDrawable(AppUtils.getDrawable(context, Ionicons.Icon.ion_ios_heart, R.color.color_favorite, 19))
        }
    }

    /**
     * Handle faves click
     */
    @JvmStatic
    @BindingAdapter(value = ["fave", "callback"], requireAll = true)
    fun favesClick(view: RoundedImageView, fave: Fave, callback: FavesCallback) {
        view.setOnClickListener { callback.onFaveClick(view, fave, false) }

        view.setOnLongClickListener {
            callback.onFaveClick(view, fave, true)
            return@setOnLongClickListener true
        }
    }

    /**
     * Handle comments click
     */
    @JvmStatic
    @BindingAdapter(value = ["comment", "callback"])
    fun commentsClick(view: View, comment: Comment?, callback: CommentsCallback?) {
        comment?.let {
            view.setOnClickListener { callback?.onCommentClicked(view, comment, false) }

            view.setOnLongClickListener {
                callback?.onCommentClicked(view, comment, true)
                return@setOnLongClickListener true
            }
        }
    }

    /**
     * Handle notification click
     */
    @JvmStatic
    @BindingAdapter("notification")
    fun setNotification(view: TextView, notification: Notification) {
        val user = notification.username!!

        view.text = when (notification.type) {
            Notification.NOTIFICATION_TYPE_LIKE -> {
                val notif = "$user liked your post"
                AppUtils.highLightName(view.context, notif, 0, user.length)
            }

            Notification.NOTIFICATION_TYPE_COMMENT -> {
                val notif = "$user commented: ${notification.description}"
                AppUtils.highLightName(view.context, notif, 0, user.length)
            }

            else -> {
                val notif = "$user ${notification.description}"
                AppUtils.highLightName(view.context, notif, 0, user.length)
            }
        }
    }

    /**
     * Set verified icon
     */
    @JvmStatic
    @BindingAdapter("verified")
    fun setVerified(view: TextView, verified: Boolean) {
        if (verified) view.setRightDrawable(ContextCompat.getDrawable(view.context, R.drawable.ic_verified)!!)
    }

}