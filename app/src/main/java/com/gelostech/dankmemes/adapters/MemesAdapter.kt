package com.gelostech.dankmemes.adapters

import android.content.Context
import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.commoners.AppUtils.setDrawable
import com.gelostech.dankmemes.commoners.MyBounceInterpolator
import com.gelostech.dankmemes.models.MemeModel
import com.makeramen.roundedimageview.RoundedDrawable
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.ionicons_typeface_library.Ionicons
import kotlinx.android.synthetic.main.item_meme.view.*
import java.lang.ref.WeakReference
import android.support.v4.content.ContextCompat
import com.gelostech.dankmemes.commoners.AppUtils.cacheBitmap
import com.gelostech.dankmemes.commoners.AppUtils.getBitmap
import com.gelostech.dankmemes.commoners.K
import com.gelostech.dankmemes.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class MemesAdapter(private val context: Context, private val onItemClickListener: OnItemClickListener) : RecyclerView.Adapter<MemesAdapter.MemeHolder>(){
    private val memes = mutableListOf<MemeModel>()

    fun addMeme(meme: MemeModel) {
        if (!hasBeenAdded(meme)) {
            if (isNewer(meme)) {
                memes.add(0, meme)
                notifyItemInserted(0)

            } else {
                memes.add(meme)
                notifyItemInserted(memes.size-1)
            }
        }
    }

    fun updateMeme(meme: MemeModel) {
        for ((index, memeModel) in memes.withIndex()) {
            if (meme.id == memeModel.id) {
                memes[index] = meme
                notifyItemChanged(index, meme)
            }
        }
    }

    fun removeMeme(meme: MemeModel) {
        var indexToRemove: Int = -1

        for ((index, memeModel) in memes.withIndex()) {
            if (meme.id == memeModel.id) {
                indexToRemove = index
            }
        }

        memes.removeAt(indexToRemove)
        notifyItemRemoved(indexToRemove)
    }

    private fun hasBeenAdded(meme: MemeModel):Boolean {
        var added = false

        for (m in memes) {
            if (m.id == meme.id) {
                added = true
            }
        }

        return added
    }

    private fun isNewer(meme: MemeModel): Boolean {
        var newer = false

        if (memes.size > 0) {
            val m = memes[0]

            if (m.time!! < meme.time!!) {
                newer = true
            }
        }

        return newer
    }

    fun getLastKey(): String {
        return memes[memes.size-1].id!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemeHolder {
        return when(viewType) {
            K.IMAGE -> MemeHolder(parent.inflate(R.layout.item_meme), onItemClickListener, context)

            else -> MemeHolder(parent.inflate(R.layout.item_meme), onItemClickListener, context)
        }
    }

    override fun getItemCount(): Int = memes.size

    override fun getItemViewType(position: Int): Int {
        return when(memes[position].type) {
            K.IMAGE -> K.IMAGE
            else -> return  -1
        }
    }

    override fun onBindViewHolder(holder: MemeHolder, position: Int) {
        holder.bindViews(memes[position])
    }

    interface OnItemClickListener {
        fun onItemClick(meme: MemeModel, viewID: Int, image: Bitmap?)
    }


    class MemeHolder(itemView: View, onItemClickListener: OnItemClickListener, context: Context) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val userIcon = itemView.memeIcon
        private val userName = itemView.memeUser
        private val memeTime = itemView.memeTime
        private val memeMore = itemView.memeMore
        private val memeCaption = itemView.memeCaption
        private val memeImage = itemView.memeImage
        private val memeLike = itemView.memeLike
        private val memeComment = itemView.memeComment
        private val memeFave = itemView.memeFave
        private var anim: Animation
        private var bounceInterpolator: MyBounceInterpolator
        private var weakReference: WeakReference<OnItemClickListener> = WeakReference(onItemClickListener)
        private lateinit var meme: MemeModel
        private lateinit var image: Bitmap
        private var TAG = MemeHolder::class.java.simpleName
        private val c = context

        init {
            memeMore.setImageDrawable(setDrawable(context, Ionicons.Icon.ion_android_more_vertical, R.color.secondaryText, 14))
            memeComment.setDrawable(setDrawable(context, Ionicons.Icon.ion_ios_chatboxes_outline, R.color.secondaryText, 20))

            anim = AnimationUtils.loadAnimation(context, R.anim.bounce)
            bounceInterpolator = MyBounceInterpolator(0.2, 20.0)
            anim.interpolator = bounceInterpolator

            memeMore.setOnClickListener(this)
            memeFave.setOnClickListener(this)
            memeComment.setOnClickListener(this)
            memeLike.setOnClickListener(this)
            memeImage.setOnClickListener(this)
            userIcon.setOnClickListener(this)
        }

        fun bindViews(meme: MemeModel) {
            this.meme = meme

            with(meme) {
                loadIcon(meme)
                //loadFromStorage(memePosterID!!, userIcon)

                userName.text = memePoster
                memeTime.text = TimeFormatter().getTimeStamp(time!!)
                if (caption!!.isNullOrEmpty()) {
                    memeCaption.hideView()
                } else {
                    if (!memeCaption.isShown) memeCaption.showView()
                    memeCaption.text = caption
                }

                if (thumbnail.isNullOrEmpty()) {
                    memeImage.loadUrl(imageUrl!!)
                } else {
                    memeImage.loadUrl(imageUrl!!, thumbnail!!)
                }

                memeLike.text = "$likesCount likes"
                comments(commentsCount!!)

                if (likes.containsKey(FirebaseAuth.getInstance().currentUser!!.uid))
                    liked(likesCount!!)
                else
                    notLiked(likesCount!!)

                if (!faves.containsKey(FirebaseAuth.getInstance().currentUser!!.uid))
                    memeFave.setImageDrawable(setDrawable(c, Ionicons.Icon.ion_ios_heart_outline, R.color.secondaryText, 19))
                else
                    memeFave.setImageDrawable(setDrawable(c, Ionicons.Icon.ion_ios_heart, R.color.pink, 19))
            }

        }

        private fun loadIcon(meme: MemeModel) {
            if (getBitmap(meme.memePosterID!!) == null)
                userIcon.loadUrl(R.drawable.person)
            else
                userIcon.setImageBitmap(getBitmap(meme.memePosterID!!))

            val avatarRef = FirebaseStorage.getInstance().reference.child("avatars").child(meme.memePosterID!!)
            avatarRef.downloadUrl.addOnSuccessListener {
                cacheBitmap(it.toString(), meme.memePosterID!!)
                userIcon.loadUrl(it.toString())
            }
        }

        private fun comments(comments: Int) {
            when {
                comments > 1 -> memeComment.text = "$comments comments"
                comments == 1 -> memeComment.text = "$comments comment"
                else -> memeComment.text = "comment"
            }
        }

        private fun liked(likes: Int) {
            memeLike.setDrawable(setDrawable(c, FontAwesome.Icon.faw_thumbs_up2, R.color.colorAccent, 20))
            when {
                likes > 1 -> memeLike.text = likes.toString() + " likes"
                likes == 1 -> memeLike.text = likes.toString() + " like"
                else -> memeLike.text = "like"
            }
            memeLike.setTextColor(ContextCompat.getColor(c, R.color.colorAccent))
        }

        private fun notLiked(likes: Int) {
            memeLike.setDrawable(setDrawable(c, FontAwesome.Icon.faw_thumbs_up, R.color.secondaryText, 20))
            when {
                likes > 1 -> memeLike.text = likes.toString() + " likes"
                likes == 1 -> memeLike.text = likes.toString() + " like"
                else -> memeLike.text = "like"
            }
            memeLike.setTextColor(ContextCompat.getColor(c, R.color.textGray))
        }

        override fun onClick(v: View?) {
            image = (memeImage.drawable as RoundedDrawable).sourceBitmap

            when(v!!.id) {
                memeLike.id -> {
                    weakReference.get()!!.onItemClick(meme, 0, null)
                    memeLike.startAnimation(anim)
                }

                memeMore.id -> weakReference.get()!!.onItemClick(meme, 1, image)
                memeFave.id -> weakReference.get()!!.onItemClick(meme, 2, null)
                memeComment.id -> weakReference.get()!!.onItemClick(meme, 3, null)
                memeImage.id -> weakReference.get()!!.onItemClick(meme, 4, image)
                userIcon.id -> weakReference.get()!!.onItemClick(meme, 5, null)
            }
        }


    }



}