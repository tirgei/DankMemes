package com.gelostech.dankmemes.ui.activities

import android.content.ContentResolver
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.Status
import com.gelostech.dankmemes.ui.adapters.CommentAdapter
import com.gelostech.dankmemes.ui.base.BaseActivity
import com.gelostech.dankmemes.data.models.Comment
import com.gelostech.dankmemes.data.responses.GenericResponse
import com.gelostech.dankmemes.databinding.ActivityCommentBinding
import com.gelostech.dankmemes.ui.callbacks.CommentsCallback
import com.gelostech.dankmemes.ui.callbacks.EditTextCallback
import com.gelostech.dankmemes.ui.callbacks.EditTextListener
import com.gelostech.dankmemes.ui.viewmodels.CommentsViewModel
import com.gelostech.dankmemes.utils.*
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.activity_comment.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class CommentActivity : BaseActivity() {
    private lateinit var commentsAdapter: CommentAdapter
    private lateinit var memeId: String
    private lateinit var binding: ActivityCommentBinding
    private val commentsViewModel: CommentsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_comment)
        binding.lifecycleOwner = this

        memeId = intent.getStringExtra(Constants.MEME_ID)!!

        initViews()
        initResponseObserver()
        initEmptyStateObserver()
        initCommentsObserver()

        commentsViewModel.fetchComments(memeId)
    }

    private fun initViews() {
        setSupportActionBar(commentToolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = null
        }

        val disabledSendIcon = AppUtils.getDrawable(this, FontAwesome.Icon.faw_paper_plane, R.color.color_secondary_variant, 22)
        val enabledSendIcon = AppUtils.getDrawable(this, FontAwesome.Icon.faw_paper_plane, R.color.color_secondary, 22)

        sendComment.setImageDrawable(disabledSendIcon)
        commentET.addTextChangedListener(EditTextListener(object : EditTextCallback {
            override fun onTextChanged(text: String) {
                if (text.isEmpty())
                    sendComment.setImageDrawable(disabledSendIcon)
                else
                    sendComment.setImageDrawable(enabledSendIcon)
            }
        }))

        commentsAdapter = CommentAdapter(commentsCallback)

        commentsRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@CommentActivity)
            itemAnimator = DefaultItemAnimator()
            adapter = commentsAdapter
        }

        commentsRefresh.setOnRefreshListener {
            commentsAdapter.currentList?.dataSource?.invalidate()
            runDelayed(2500) { commentsRefresh.isRefreshing = false }
        }

        sendComment.setOnClickListener { postComment() }
    }

    /**
     * Initialize function to fetch comments
     */
    private fun initCommentsObserver() {
        commentsViewModel.fetchComments(memeId).observe(this, Observer {
            commentsAdapter.submitList(it)
        })
    }

    /**
     * Initialize function to observer Empty State LiveData
     */
    private fun initEmptyStateObserver() {
        commentsViewModel.showEmptyStateLiveData.observe(this, Observer {
            when (it) {
                true -> {
                    commentsRv.hideView()
                    emptyState.showView()
                }
                else -> {
                    emptyState.hideView()
                    commentsRv.showView()
                }
            }
        })
    }

    /**
     * Initialize observer for GenericResponse LiveData
     */
    private fun initResponseObserver() {
        commentsViewModel.genericResponseLiveData.observe(this, Observer {
            when (it.status) {
                Status.LOADING -> Timber.e("Loading...")

                Status.SUCCESS -> {
                    hideLoading()

                    when (it.item) {
                        GenericResponse.ITEM_RESPONSE.DELETE_COMMENT -> {
                            toast("Comment deleted \uD83D\uDEAE")
                            commentsAdapter.currentList?.dataSource?.invalidate()
                        }

                        GenericResponse.ITEM_RESPONSE.POST_COMMENT -> {
                            commentET.setText("")
                            playNotificationSound()
                            commentsAdapter.currentList?.dataSource?.invalidate()
                        }

                        else -> Timber.e("Success")
                    }
                }

                Status.ERROR -> {
                    hideLoading()
                    toast("${it.error}. Please try again")
                }
            }
        })
    }


    private fun postComment() {
        if (TextUtils.isEmpty(commentET.text)) {
            toast("Please type a comment..")
            return
        }

        showLoading("Posting comment...")

        val comment = Comment()
        comment.userId = getUid()
        comment.userName = sessionManager.getUsername()
        comment.userAvatar = sessionManager.getUserAvatar()
        comment.comment = commentET.text.toString().trim()
        comment.hates = 0
        comment.likes = 0
        comment.time = System.currentTimeMillis()
        comment.memeId = memeId

        commentsViewModel.postComment(comment)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> onBackPressed()
        }

        return true
    }

    private val commentsCallback = object : CommentsCallback {
        override fun onCommentClicked(view: View, comment: Comment, longClick: Boolean) {
            if (longClick) handleLongClick(comment)
            else {
                if (view.id == R.id.commentIcon) handleClick(comment)
            }
        }
    }

    private fun handleClick (comment: Comment) {
        if (comment.userId != getUid()) {
            val i = Intent(this, ProfileActivity::class.java)
            i.putExtra(Constants.USER_ID, comment.userId)
            startActivity(i)
            overridePendingTransition(R.anim.enter_b, R.anim.exit_a)
        }
    }

    private fun handleLongClick (comment: Comment) {
        if (comment.userId == getUid()) {
            alert ("Delete this comment?") {
                title = "Delete Comment"

                positiveButton("Delete") {
                    showLoading("Deleting comment...")
                    commentsViewModel.deleteComment(memeId, comment.commentId!!)
                }
                negativeButton("Cancel"){}
            }.show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.enter_a, R.anim.exit_b)
    }

    private fun playNotificationSound() {
        try {
            val alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + this.packageName + "/raw/new_comment")
            val r = RingtoneManager.getRingtone(this, alarmSound)
            r.play()

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

}
