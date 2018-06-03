package com.gelostech.dankmemes.activities

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.MenuItem
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.adapters.CommentAdapter
import com.gelostech.dankmemes.commoners.BaseActivity
import com.gelostech.dankmemes.models.CommentModel
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.activity_comment.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast

class CommentActivity : BaseActivity(), CommentAdapter.OnItemClickListener {
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var memeId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        memeId = intent.getStringExtra("memeId")

        initViews()
        loadSample()
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
        commentAdapter = CommentAdapter(this, this)
        commentsRv.adapter = commentAdapter

        sendComment.setOnClickListener { addComment() }
    }

    private fun addComment() {
        if (TextUtils.isEmpty(commentET.text)) {
            toast("Please type a comment..")
            return
        }

        val word = commentET.text.toString().trim()

        val comment = CommentModel("2", "dfdf", System.currentTimeMillis(), word)
        commentAdapter.addComment(comment)
    }

    private fun loadSample() {
        val comment1 = CommentModel("1", "dsgdsgdfg", System.currentTimeMillis(), "hahaha.. this is so funny")
        commentAdapter.addComment(comment1)

        val comment2 = CommentModel("1", "dsgdsgdfg", System.currentTimeMillis(), "Oh no you didnt")
        commentAdapter.addComment(comment2)

        val comment3 = CommentModel("1", "dsgdsgdfg", System.currentTimeMillis(), "Wait.. whaaat ")
        commentAdapter.addComment(comment3)
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
                val i = Intent(this, ProfileActivity::class.java)
                i.putExtra("userId", comment.authorId)
                startActivity(i)
                overridePendingTransition(R.anim.enter_b, R.anim.exit_a)
            }

            1 -> {
                alert {
                    message = "Remove this comment?"
                    positiveButton("Delete") {deleteComment(comment.id!!)}
                    negativeButton("Cancel"){}
                }.show()

            }
        }
    }

    /**
     * This method deletes the comment from the database
     * @param id ID of the comment
     */
    private fun deleteComment(id: String) {

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.enter_a, R.anim.exit_b)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}
