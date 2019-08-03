package com.gelostech.dankmemes.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.core.content.ContextCompat
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.commoners.AppUtils
import com.gelostech.dankmemes.commoners.BaseActivity
import com.gelostech.dankmemes.commoners.Config
import com.gelostech.dankmemes.models.MemeModel
import com.gelostech.dankmemes.utils.*
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.ionicons_typeface_library.Ionicons
import kotlinx.android.synthetic.main.activity_meme.*
import timber.log.Timber

class MemeActivity : BaseActivity() {
    private lateinit var memeId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meme)

        memeId = intent.getStringExtra(Config.MEME_ID)

        initViews()
        load()
    }

    private fun initViews() {
        setSupportActionBar(memeToolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Meme"
    }

    private fun load() {
        val memeRef = getFirestore().collection(Config.MEMES).document(memeId)
        memeRef.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null) {
                Timber.e("Error fetching meme: $firebaseFirestoreException")
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                val meme = documentSnapshot.toObject(MemeModel::class.java)!!

                with(meme) {
                    memeIcon.loadUrl(memePosterAvatar!!)
                    memeUser.text = memePoster
                    memeTime.text = TimeFormatter().getTimeStamp(time!!)

                    if (caption.isNullOrEmpty()) {
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

                    comments(commentsCount)

                    if (meme.likes.containsKey(getUid()))
                        liked(likesCount)
                    else
                        notLiked(likesCount)

                    if (!faves.containsKey(getUid()))
                        memeFave.setImageDrawable(AppUtils.setDrawable(this@MemeActivity, Ionicons.Icon.ion_ios_heart_outline, R.color.secondaryText, 19))
                    else
                        memeFave.setImageDrawable(AppUtils.setDrawable(this@MemeActivity, Ionicons.Icon.ion_ios_heart, R.color.pink, 19))

                }


            }
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
        memeLike.setDrawable(AppUtils.setDrawable(this, FontAwesome.Icon.faw_thumbs_up2, R.color.colorAccent, 20))
        when {
            likes > 1 -> memeLike.text = likes.toString() + " likes"
            likes == 1 -> memeLike.text = likes.toString() + " like"
            else -> memeLike.text = "like"
        }
        memeLike.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
    }

    private fun notLiked(likes: Int) {
        memeLike.setDrawable(AppUtils.setDrawable(this, FontAwesome.Icon.faw_thumbs_up, R.color.secondaryText, 20))
        when {
            likes > 1 -> memeLike.text = likes.toString() + " likes"
            likes == 1 -> memeLike.text = likes.toString() + " like"
            else -> memeLike.text = "like"
        }
        memeLike.setTextColor(ContextCompat.getColor(this, R.color.textGray))
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> onBackPressed()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        AppUtils.animateEnterLeft(this)
    }
}
