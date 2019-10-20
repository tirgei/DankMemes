package com.gelostech.dankmemes.utils

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
import com.google.firebase.auth.FirebaseAuth
import com.makeramen.roundedimageview.RoundedImageView
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.ionicons_typeface_library.Ionicons
import de.hdodenhof.circleimageview.CircleImageView

object BindingAdapter {
    /**
     * Bind Image to layout
     */
    @JvmStatic
    @BindingAdapter(value = ["bind:image", "bind:placeholder", "bind:thumbnail"], requireAll = false)
    fun loadImage(view: ImageView, image: Any, placeholder: Int, thumbnail: String? = null) {
        if (thumbnail.isNullOrEmpty())
            view.load(image, placeholder)
        else
            view.load(image, placeholder, thumbnail)
    }

    /**
     * Set layout visibility
     */
    @JvmStatic
    @BindingAdapter("bind:visibility")
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
    @BindingAdapter("bind:likeStatus")
    fun setLikeStatus(view: TextView, likes: MutableMap<String, Boolean>) {
        val context = view.context

        when (likes.containsKey(FirebaseAuth.getInstance().currentUser!!.uid)) {
            true -> {
                view.setDrawable(AppUtils.setDrawable(context, FontAwesome.Icon.faw_thumbs_up2, R.color.colorAccent, 20))
                view.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
            }

            else -> {
                view.setDrawable(AppUtils.setDrawable(context, FontAwesome.Icon.faw_thumbs_up, R.color.secondaryText, 20))
                view.setTextColor(ContextCompat.getColor(context, R.color.textGray))
            }
        }
    }

    /**
     * Set favorite status to Meme post
     */
    @JvmStatic
    @BindingAdapter("bind:faveStatus")
    fun setFaveStatus(view: ImageButton, faves: MutableMap<String, Boolean>) {
        val context = view.context

        if (!faves.containsKey(FirebaseAuth.getInstance().currentUser!!.uid))
            view.setImageDrawable(AppUtils.setDrawable(context, Ionicons.Icon.ion_ios_heart_outline, R.color.secondaryText, 19))
        else
            view.setImageDrawable(AppUtils.setDrawable(context, Ionicons.Icon.ion_ios_heart, R.color.pink, 19))
    }

    /**
     * Handle faves click
     */
    @JvmStatic
    @BindingAdapter(value = ["bind:fave", "bind:callback"], requireAll = true)
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
    @BindingAdapter(value = ["bind:comment", "bind:callback"], requireAll = true)
    fun commentsClick(view: View, comment: Comment, callback: CommentsCallback) {
        view.setOnClickListener { callback.onCommentClicked(view, comment, false) }

        view.setOnLongClickListener {
            callback.onCommentClicked(view, comment, true)
            return@setOnLongClickListener true
        }
    }

    /**
     * Handle notification click
     */
    @JvmStatic
    @BindingAdapter("bind:notification")
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
}