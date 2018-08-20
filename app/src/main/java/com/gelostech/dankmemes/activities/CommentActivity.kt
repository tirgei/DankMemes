package com.gelostech.dankmemes.activities

import android.content.ContentResolver
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.adapters.CommentAdapter
import com.gelostech.dankmemes.commoners.BaseActivity
import com.gelostech.dankmemes.commoners.Config
import com.gelostech.dankmemes.models.CommentModel
import com.gelostech.dankmemes.models.MemeModel
import com.gelostech.dankmemes.utils.PreferenceHelper
import com.google.firebase.database.*
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.activity_comment.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import com.gelostech.dankmemes.utils.PreferenceHelper.get

class CommentActivity : BaseActivity(), CommentAdapter.OnItemClickListener {
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var memeId: String
    private lateinit var commentsQuery: Query
    private lateinit var prefs: SharedPreferences

    companion object {
        private var TAG = CommentActivity::class.java.simpleName
    }

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
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Comments"

        sendComment.setImageDrawable(IconicsDrawable(this).icon(FontAwesome.Icon.faw_paper_plane).color(ContextCompat.getColor(this, R.color.colorAccent)).sizeDp(22))

        commentsRv.setHasFixedSize(true)
        commentsRv.layoutManager = LinearLayoutManager(this)
        commentsRv.itemAnimator = DefaultItemAnimator()
        commentAdapter = CommentAdapter( this)
        commentsRv.adapter = commentAdapter

        sendComment.setOnClickListener { addComment() }
    }

    private val commentsValueListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
            Log.e(TAG, "Error loading comments: ${p0.message}")
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
            Log.e(TAG, "Error loading comments: ${p0.message}")
        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            Log.e(TAG, "Comment moved: ${p0.key}")

        }

        override fun onChildChanged(p0: DataSnapshot, p1: String?) {

        }

        override fun onChildAdded(p0: DataSnapshot, p1: String?) {
            val comment = p0.getValue(CommentModel::class.java)
            commentAdapter.addComment(comment!!)
        }

        override fun onChildRemoved(p0: DataSnapshot) {
            val comment = p0.getValue(CommentModel::class.java)
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

        val commentObject = CommentModel()
        commentObject.commentKey = id
        commentObject.authorId = getUid()
        commentObject.userName = prefs[Config.USERNAME]
        commentObject.userAvatar = prefs[Config.AVATAR]
        commentObject.comment = comment
        commentObject.hates = 0
        commentObject.likes = 0
        commentObject.timeStamp = System.currentTimeMillis()
        commentObject.picKey = memeId

        getDatabaseReference().child("comments").child(memeId).child(id!!).setValue(commentObject).addOnSuccessListener {
            commentET.setText("")
            playNotificationSound()

            getDatabaseReference().child("dank-memes").child(memeId).runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    val meme = mutableData.getValue<MemeModel>(MemeModel::class.java)

                    meme!!.commentsCount = meme.commentsCount + 1

                    mutableData.value = meme
                    return Transaction.success(mutableData)
                }

                override fun onComplete(databaseError: DatabaseError?, b: Boolean, dataSnapshot: DataSnapshot?) {

                    Log.d(javaClass.simpleName, "postTransaction:onComplete: $databaseError")
                }
            })
        }

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> onBackPressed()
        }

        return true
    }

    override fun onItemClick(comment: CommentModel, viewId: Int) {
        when(viewId) {
            0 -> {
                if (comment.authorId != getUid()) {
                    val i = Intent(this, ProfileActivity::class.java)
                    i.putExtra("userId", comment.authorId)
                    startActivity(i)
                    overridePendingTransition(R.anim.enter_b, R.anim.exit_a)
                }
            }
        }
    }

    override fun onLongItemClick(comment: CommentModel) {
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

            getDatabaseReference().child("dank-memes").child(memeId).runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    val meme = mutableData.getValue<MemeModel>(MemeModel::class.java)

                    meme!!.commentsCount = meme.commentsCount!! - 1

                    mutableData.value = meme
                    return Transaction.success(mutableData)
                }

                override fun onComplete(databaseError: DatabaseError?, b: Boolean, dataSnapshot: DataSnapshot?) {

                    Log.d(javaClass.simpleName, "postTransaction:onComplete: $databaseError")
                }
            })
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
