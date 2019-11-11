package com.gelostech.dankmemes.ui.activities

import android.content.ContentResolver
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.ui.adapters.CommentAdapter
import com.gelostech.dankmemes.ui.base.BaseActivity
import com.gelostech.dankmemes.utils.Constants
import com.gelostech.dankmemes.data.models.Comment
import com.gelostech.dankmemes.data.models.Meme
import com.gelostech.dankmemes.ui.callbacks.CommentsCallback
import com.gelostech.dankmemes.utils.PreferenceHelper
import com.gelostech.dankmemes.utils.PreferenceHelper.get
import com.google.firebase.database.*
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.activity_comment.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import timber.log.Timber

class CommentActivity : BaseActivity() {
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var memeId: String
    private lateinit var commentsQuery: Query
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        memeId = intent.getStringExtra("memeId")

        initViews()

        prefs = PreferenceHelper.defaultPrefs(this)
        commentsQuery = getDatabaseReference().child("comments").child(memeId)

        commentsQuery.addValueEventListener(commentsValueListener)
        commentsQuery.addChildEventListener(commentsChildListener)

    }

    private fun initViews() {
        setSupportActionBar(commentToolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Comments"
        }
        sendComment.setImageDrawable(IconicsDrawable(this).icon(FontAwesome.Icon.faw_paper_plane).color(ContextCompat.getColor(this, R.color.colorAccent)).sizeDp(22))

        commentAdapter = CommentAdapter(commentsCallback)

        commentsRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@CommentActivity)
            itemAnimator = DefaultItemAnimator()
            adapter = commentAdapter
        }

        sendComment.setOnClickListener { addComment() }
    }

    private val commentsValueListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
            Timber.e("Error loading comments: ${p0.message}")
        }

        override fun onDataChange(p0: DataSnapshot) {
            if (p0.exists()) {
                commentsEmptyState.visibility = View.GONE
                commentsRv.visibility = View.VISIBLE
            } else {
                commentsRv.visibility = View.GONE
                commentsEmptyState.visibility = View.VISIBLE
            }
        }
    }

    private val commentsChildListener = object : ChildEventListener {
        override fun onCancelled(p0: DatabaseError) {
            Timber.e("Error loading comments: ${p0.message}")
        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            Timber.e("Comment moved: ${p0.key}")

        }

        override fun onChildChanged(p0: DataSnapshot, p1: String?) {

        }

        override fun onChildAdded(p0: DataSnapshot, p1: String?) {
            val comment = p0.getValue(Comment::class.java)
            commentAdapter.addComment(comment!!)
        }

        override fun onChildRemoved(p0: DataSnapshot) {
            val comment = p0.getValue(Comment::class.java)
            commentAdapter.removeComment(comment!!)
        }
    }

    private fun addComment() {
        if (TextUtils.isEmpty(commentET.text)) {
            toast("Please type a comment..")
            return
        }

        val id = getDatabaseReference().child("comments").push().key
        val comment = commentET.text.toString().trim()

        val commentObject = Comment()
        commentObject.commentKey = id
        commentObject.authorId = getUid()
        commentObject.userName = prefs[Constants.USERNAME]
        commentObject.userAvatar = prefs[Constants.AVATAR]
        commentObject.comment = comment
        commentObject.hates = 0
        commentObject.likes = 0
        commentObject.timeStamp = System.currentTimeMillis()
        commentObject.picKey = memeId

        getDatabaseReference().child("comments").child(memeId).child(id!!).setValue(commentObject).addOnSuccessListener {
            commentET.setText("")
            playNotificationSound()
            updateCommentsCount(true)
        }

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
        if (comment.authorId != getUid()) {
            val i = Intent(this, ProfileActivity::class.java)
            i.putExtra(Constants.USER_ID, comment.authorId)
            startActivity(i)
            overridePendingTransition(R.anim.enter_b, R.anim.exit_a)
        }
    }

    private fun handleLongClick (comment: Comment) {
        if (comment.authorId == getUid()) {
            alert {
                message = "Remove this comment?"
                positiveButton("Delete") {deleteComment(comment.commentKey!!)}
                negativeButton("Cancel"){}
            }.show()
        }
    }

    /**
     * This function deletes the comment from the database
     * @param id ID of the comment
     */
    private fun deleteComment(id: String) {
        getDatabaseReference().child("comments").child(memeId).child(id).removeValue().addOnCompleteListener {
            toast("Comment deleted")
            updateCommentsCount(false)
        }
    }

    private fun updateCommentsCount(add: Boolean) {
        val docRef = getFirestore().collection(Constants.MEMES).document(memeId)

        getFirestore().runTransaction {

            val meme =  it[docRef].toObject(Meme::class.java)
            var comments = meme!!.commentsCount

            if (add) {
                comments += 1
            } else {
                comments -= 1
            }

            it.update(docRef, Constants.COMMENTS_COUNT, comments)

            return@runTransaction null
        }.addOnSuccessListener {
            Timber.e("Comments count updated")
        }.addOnFailureListener {
            Timber.e("Error updating comments count")
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

    override fun onDestroy() {
        commentsQuery.removeEventListener(commentsValueListener)
        commentsQuery.removeEventListener(commentsChildListener)
        super.onDestroy()
    }

}
