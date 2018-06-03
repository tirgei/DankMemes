package com.gelostech.dankmemes.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.models.CommentModel
import com.gelostech.dankmemes.utils.TimeFormatter
import com.gelostech.dankmemes.utils.inflate
import kotlinx.android.synthetic.main.item_comment.view.*
import java.lang.ref.WeakReference

class CommentAdapter(context: Context, onItemClickListener: OnItemClickListener): RecyclerView.Adapter<CommentAdapter.CommentHolder>() {
    private val comments = mutableListOf<CommentModel>()
    private val c: Context = context
    private val onItemClickListener = onItemClickListener;

    fun addComment(comment: CommentModel) {
        comments.add(comment)
        notifyItemInserted(comments.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        return CommentHolder(parent.inflate(R.layout.item_comment), onItemClickListener)
    }

    override fun getItemCount(): Int = comments.size

    override fun onBindViewHolder(holder: CommentHolder, position: Int) {
        holder.bindView(comments[position], c)
    }

    class CommentHolder(itemView: View, onItemClickListener: OnItemClickListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        private val commentRoot = itemView.commentRoot
        private val commentIcon = itemView.commentIcon
        private val commentUser = itemView.commentUser
        private val commentText = itemView.commentText
        private val commentTime = itemView.commentTime
        private val weakReference = WeakReference<OnItemClickListener>(onItemClickListener)
        private lateinit var comment: CommentModel

        init {
            commentIcon.setOnClickListener(this)
            commentRoot.setOnLongClickListener(this)
        }

        fun bindView(comment: CommentModel, context: Context) {
            this.comment = comment

            with(comment) {
                Glide.with(context).load(R.drawable.person).thumbnail(0.1f).into(commentIcon)
                commentUser.text = "Vincent Tirgei"
                commentText.text = commentContent
                commentTime.text = TimeFormatter().getTimeStamp(time!!)
            }
        }

        override fun onClick(v: View?) {
            when(v?.id) {
                commentIcon.id -> weakReference.get()!!.onItemClick(comment, 0)
            }
        }

        override fun onLongClick(v: View?): Boolean {
            when(v?.id) {
                commentRoot.id -> weakReference.get()!!.onItemClick(comment, 1)
            }

            return true
        }
    }

    interface OnItemClickListener{
        fun onItemClick(comment: CommentModel, viewId: Int)
    }

}