package com.gelostech.dankmemes.ui.adapters

import android.view.ViewGroup
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.models.Meme
import com.gelostech.dankmemes.data.wrappers.ObservableMeme
import com.gelostech.dankmemes.databinding.ItemMemeBinding
import com.gelostech.dankmemes.ui.callbacks.MemesCallback
import com.gelostech.dankmemes.utils.AppUtils
import com.gelostech.dankmemes.utils.TimeFormatter
import com.gelostech.dankmemes.utils.inflate
import com.gelostech.dankmemes.utils.setDrawable
import com.mikepenz.ionicons_typeface_library.Ionicons
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber

class PagedMemesAdapter(private val callback: MemesCallback): PagedListAdapter<ObservableMeme, PagedMemesAdapter.MemeHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ObservableMeme>() {
            override fun areItemsTheSame(oldItem: ObservableMeme, newItem: ObservableMeme): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ObservableMeme, newItem: ObservableMeme): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCurrentListChanged(previousList: PagedList<ObservableMeme>?, currentList: PagedList<ObservableMeme>?) {
        super.onCurrentListChanged(previousList, currentList)
        Timber.e("Previous list: $previousList vs Current list: $currentList")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemeHolder {
        return MemeHolder(parent.inflate(R.layout.item_meme), callback)
    }

    override fun onBindViewHolder(holder: MemeHolder, position: Int) {
        val currentMeme = getItem(position)
        Timber.e("Binding view holder: ${currentMeme?.id}")

        currentMeme?.meme?.subscribeBy(
                onComplete = {
                    Timber.e("Subscribing")
//                    holder.bind(it)
                },
                onError = { Timber.e("Error observing meme: $it") }
        )
    }

    override fun onViewRecycled(holder: MemeHolder) {
        super.onViewRecycled(holder)
        holder.apply { disposables.clear() }
    }

    inner class MemeHolder(private val binding: ItemMemeBinding, private val callback: MemesCallback):
            RecyclerView.ViewHolder(binding.root) {
        val disposables = CompositeDisposable()

        init {
            binding.memeMore.apply {
                setImageDrawable(AppUtils.setDrawable(this.context, Ionicons.Icon.ion_android_more_vertical, R.color.secondaryText, 14))
            }

            binding.memeComment.apply {
                this.setDrawable(AppUtils.setDrawable(this.context, Ionicons.Icon.ion_ios_chatboxes_outline, R.color.secondaryText, 20))
            }
        }

        // Bind meme object to layout
        fun bind(meme: Meme) {
            Timber.e("Binding ${meme.id}")

            binding.meme = meme
            binding.callback = callback
            binding.timeFormatter = TimeFormatter()
        }
    }
}