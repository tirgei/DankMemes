package com.gelostech.dankmemes.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.models.Comment
import com.gelostech.dankmemes.databinding.ItemCommentBinding
import com.gelostech.dankmemes.ui.callbacks.CommentsCallback
import com.gelostech.dankmemes.utils.TimeFormatter
import com.gelostech.dankmemes.utils.inflate

class CommentAdapter(val callback: CommentsCallback): RecyclerView.Adapter<CommentAdapter.CommentHolder>() {
    private val comments = mutableListOf<Comment>()

    fun addComments(comments: List<Comment>) {
        this.comments.addAll(comments)
        notifyDataSetChanged()
    }

    fun clear() {
        this.comments.clear()
        notifyDataSetChanged()
    }

    fun removeComment(id: String) {
        var indexToRemove: Int = -1

        for ((index, commentModel) in comments.withIndex()) {
            if (commentModel.commentKey!! == id) {
                indexToRemove = index
            }
        }

        comments.removeAt(indexToRemove)
        notifyItemRemoved(indexToRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        return CommentHolder(parent.inflate(R.layout.item_comment), callback)
    }

    override fun getItemCount(): Int = comments.size

    override fun onBindViewHolder(holder: CommentHolder, position: Int) {
        holder.bind(comments[position])
    }

    class CommentHolder(private val binding: ItemCommentBinding, private val callback: CommentsCallback):
            RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            binding.comment = comment
            binding.callback = callback
            binding.timeFormatter = TimeFormatter()
        }

    }


}