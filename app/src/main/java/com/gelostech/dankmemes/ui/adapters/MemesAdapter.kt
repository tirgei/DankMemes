package com.gelostech.dankmemes.ui.adapters

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.models.Meme
import com.gelostech.dankmemes.data.wrappers.ItemViewModel
import com.gelostech.dankmemes.data.wrappers.NativeAdWrapper
import com.gelostech.dankmemes.data.wrappers.ObservableMeme
import com.gelostech.dankmemes.databinding.ItemMemeBinding
import com.gelostech.dankmemes.ui.callbacks.MemesCallback
import com.gelostech.dankmemes.utils.*
import com.mikepenz.ionicons_typeface_library.Ionicons
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber

class MemesAdapter(private val callback: MemesCallback): PagedListAdapter<ItemViewModel, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ItemViewModel>() {
            override fun areItemsTheSame(oldItem: ItemViewModel, newItem: ItemViewModel): Boolean {
                return when {
                    oldItem is ObservableMeme && newItem is ObservableMeme -> oldItem.id == newItem.id
                    oldItem is NativeAdWrapper && newItem is NativeAdWrapper -> oldItem.id == newItem.id
                    else -> false
                }
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: ItemViewModel, newItem: ItemViewModel): Boolean {
                return when {
                    oldItem is ObservableMeme && newItem is ObservableMeme -> oldItem == newItem
                    oldItem is NativeAdWrapper && newItem is NativeAdWrapper -> oldItem == newItem
                    else -> false
                }
            }
        }

        const val MEME = 0
        const val AD = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MemeHolder(parent.inflate(R.layout.item_meme), callback)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val meme = getItem(position) as ObservableMeme
        holder as MemeHolder
        meme.meme.subscribeBy(
            onNext = { holder.bind(it) },
            onError = {
                Timber.e("Meme deleted")
                this.currentList?.dataSource?.invalidate()
            }
        ).addTo(holder.disposables)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is MemeHolder) {
            holder.apply { disposables.clear() }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            getItem(position) is NativeAdWrapper -> AD
            else -> MEME
        }
    }

    inner class MemeHolder(private val binding: ItemMemeBinding, private val callback: MemesCallback):
            RecyclerView.ViewHolder(binding.root) {
        val disposables = CompositeDisposable()

        init {
            binding.memeMore.apply {
                setImageDrawable(AppUtils.getDrawable(this.context, Ionicons.Icon.ion_android_more_vertical, R.color.color_text_secondary, 14))
            }

            binding.memeComment.apply {
                this.setDrawable(AppUtils.getDrawable(this.context, Ionicons.Icon.ion_ios_chatboxes_outline, R.color.color_text_secondary, 20))
            }
        }

        // Bind meme object to layout
        fun bind(meme: Meme) {
            Timber.e("Binding ${meme.id}")

            if (binding.meme != null && binding.meme!!.id == meme.id) {
                meme.imageUrl = null
            }

            binding.meme = meme
            binding.callback = callback
            binding.timeFormatter = TimeFormatter()
        }
    }

}