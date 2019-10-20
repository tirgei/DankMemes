package com.gelostech.dankmemes.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.models.Meme
import com.gelostech.dankmemes.databinding.ItemMemeBinding
import com.gelostech.dankmemes.ui.callbacks.MemesCallback
import com.gelostech.dankmemes.utils.AppUtils
import com.gelostech.dankmemes.utils.TimeFormatter
import com.gelostech.dankmemes.utils.inflate
import com.gelostech.dankmemes.utils.setDrawable
import com.mikepenz.ionicons_typeface_library.Ionicons
import timber.log.Timber

class MemesAdapter(private val callback: MemesCallback) : RecyclerView.Adapter<MemesAdapter.MemeHolder>(){
    private val memes = mutableListOf<Meme>()

    fun addMeme(meme: Meme) {
        if (!hasBeenAdded(meme)) {
            if (isNewer(meme)) {
                memes.add(0, meme)
                notifyItemInserted(0)

            } else {
                memes.add(meme)
                notifyItemInserted(memes.size-1)
            }
        }
    }

    fun updateMeme(meme: Meme) {
        Timber.e("Updating meme: ${meme.id}")

        for ((index, memeModel) in memes.withIndex()) {
            if (memeModel.equals(meme)) {
                memes[index] = meme
                notifyItemChanged(index, meme)
            }
        }
    }

    fun removeMeme(meme: Meme) {
        var indexToRemove: Int = -1

        for ((index, memeModel) in memes.withIndex()) {
            if (memeModel.equals(meme)) {
                indexToRemove = index
            }
        }

        memes.removeAt(indexToRemove)
        notifyItemRemoved(indexToRemove)
    }

    private fun hasBeenAdded(meme: Meme):Boolean {
        var added = false

        for (m in memes) {
            if (m.equals(meme)) {
                added = true
            }
        }

        return added
    }

    private fun isNewer(meme: Meme): Boolean {
        var newer = false

        if (memes.size > 0) {
            val m = memes[0]

            if (m.time!! < meme.time!!) {
                newer = true
            }
        }

        return newer
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemeHolder {
        return MemeHolder(parent.inflate(R.layout.item_meme), callback)
    }

    override fun getItemCount(): Int = memes.size

    override fun onBindViewHolder(holder: MemeHolder, position: Int) {
        holder.bind(memes[position])
    }

    class MemeHolder(private val binding: ItemMemeBinding, private val callback: MemesCallback):
            RecyclerView.ViewHolder(binding.root) {

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
            binding.meme = meme
            binding.callback = callback
            binding.timeFormatter = TimeFormatter()
        }
    }


}