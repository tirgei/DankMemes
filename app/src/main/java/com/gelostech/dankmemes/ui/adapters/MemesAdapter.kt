package com.gelostech.dankmemes.ui.adapters

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.models.Meme
import com.gelostech.dankmemes.data.models.User
import com.gelostech.dankmemes.data.wrappers.ItemViewModel
import com.gelostech.dankmemes.data.wrappers.ObservableMeme
import com.gelostech.dankmemes.data.wrappers.ObservableUser
import com.gelostech.dankmemes.databinding.ItemMemeBinding
import com.gelostech.dankmemes.databinding.ItemProfileBinding
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

class MemesAdapter(private val callback: MemesCallback): PagedListAdapter<ObservableMeme, MemesAdapter.MemeHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ObservableMeme>() {
            override fun areItemsTheSame(oldItem: ObservableMeme, newItem: ObservableMeme): Boolean {
                return oldItem.id == newItem.id
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: ObservableMeme, newItem: ObservableMeme): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemeHolder {
        return MemeHolder(parent.inflate(R.layout.item_meme), callback)
    }

    override fun onBindViewHolder(holder: MemeHolder, position: Int) {
        val meme = getItem(position) as ObservableMeme

        meme.meme.subscribeBy(
                onNext = { holder.bind(it) },
                onError = {
                    Timber.e("Meme deleted")
                    this.currentList?.dataSource?.invalidate()
                }
        ).addTo(holder.disposables)
    }

    override fun onViewRecycled(holder: MemeHolder) {
        super.onViewRecycled(holder)
        holder.apply { disposables.clear() }
    }

    override fun getItemId(position: Int): Long {
        val id = getItem(position)!!.id.toLong()
        Timber.e("Long ID: $id")
        return id
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

            binding.meme = meme
            binding.callback = callback
            binding.timeFormatter = TimeFormatter()
        }
    }

}