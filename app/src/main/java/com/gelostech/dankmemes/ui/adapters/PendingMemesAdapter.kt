package com.gelostech.dankmemes.ui.adapters

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.models.PendingMeme
import com.gelostech.dankmemes.databinding.ItemPendingMemeBinding
import com.gelostech.dankmemes.ui.callbacks.PendingMemesCallback
import com.gelostech.dankmemes.utils.AppUtils
import com.gelostech.dankmemes.utils.inflate
import com.gelostech.dankmemes.utils.setDrawable
import com.mikepenz.ionicons_typeface_library.Ionicons

class PendingMemesAdapter(private val callback: PendingMemesCallback): PagedListAdapter<PendingMeme,
        PendingMemesAdapter.MemeHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PendingMeme>() {
            override fun areItemsTheSame(oldItem: PendingMeme, newItem: PendingMeme): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: PendingMeme, newItem: PendingMeme): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemeHolder {
        return MemeHolder(parent.inflate(R.layout.item_pending_meme), callback)
    }

    override fun onBindViewHolder(holder: MemeHolder, position: Int) {
        val meme = getItem(position)
        holder.bind(meme!!)
    }

    inner class MemeHolder(private val binding: ItemPendingMemeBinding, private val callback: PendingMemesCallback):
            RecyclerView.ViewHolder(binding.root) {

        fun bind(meme: PendingMeme) {
            binding.meme = meme
            binding.callback = callback

            binding.delete.setImageDrawable(AppUtils.getDrawable(binding.root.context, Ionicons.Icon.ion_android_delete, R.color.color_text_secondary, 22))
        }
    }
}