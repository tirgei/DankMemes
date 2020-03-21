package com.gelostech.dankmemes.ui.adapters

import android.view.ViewGroup
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.models.Comment
import com.gelostech.dankmemes.data.wrappers.ObservableComment
import com.gelostech.dankmemes.databinding.ItemCommentBinding
import com.gelostech.dankmemes.ui.callbacks.CommentsCallback
import com.gelostech.dankmemes.utils.TimeFormatter
import com.gelostech.dankmemes.utils.inflate
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber

class CommentAdapter(val callback: CommentsCallback): PagedListAdapter<ObservableComment, CommentAdapter.CommentHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ObservableComment>() {
            override fun areItemsTheSame(oldItem: ObservableComment, newItem: ObservableComment): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ObservableComment, newItem: ObservableComment): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCurrentListChanged(previousList: PagedList<ObservableComment>?, currentList: PagedList<ObservableComment>?) {
        super.onCurrentListChanged(previousList, currentList)
        Timber.e("Previous list: $previousList vs Current list: $currentList")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        return CommentHolder(parent.inflate(R.layout.item_comment), callback)
    }

    override fun onViewRecycled(holder: CommentHolder) {
        super.onViewRecycled(holder)
        holder.disposables.clear()
    }

    override fun onBindViewHolder(holder: CommentHolder, position: Int) {
        val comment = getItem(position)
        comment?.comment?.subscribeBy(
                onNext = { holder.bind(it) },
                onError = {
                    Timber.e("Error fetching Comment: $it")
                }
        )?.addTo(holder.disposables)
    }

    class CommentHolder(private val binding: ItemCommentBinding, private val callback: CommentsCallback):
            RecyclerView.ViewHolder(binding.root) {
        val disposables = CompositeDisposable()

        fun bind(comment: Comment) {
            binding.comment = comment
            binding.callback = callback
            binding.timeFormatter = TimeFormatter()
        }

    }

}
