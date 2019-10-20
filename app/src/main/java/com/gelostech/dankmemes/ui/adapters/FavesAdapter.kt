package com.gelostech.dankmemes.ui.adapters

import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.models.Fave
import com.gelostech.dankmemes.databinding.ItemFaveBinding
import com.gelostech.dankmemes.ui.callbacks.FavesCallback
import com.gelostech.dankmemes.utils.Constants
import com.gelostech.dankmemes.utils.inflate
import com.gelostech.dankmemes.utils.load
import com.makeramen.roundedimageview.RoundedDrawable
import kotlinx.android.synthetic.main.item_fave.view.*
import java.lang.ref.WeakReference

class FavesAdapter(private val callback: FavesCallback) : RecyclerView.Adapter<FavesAdapter.FaveHolder>() {
    private val faves = mutableListOf<Fave>()

    fun addFave(fave: Fave) {
        if (!hasBeenAdded(fave)) {
            faves.add(fave)
            notifyItemInserted(faves.size-1)
        }
    }

    fun updateFave(fave: Fave) {
        for ((index, memeModel) in faves.withIndex()) {
            if (memeModel.equals(fave)) {
                faves[index] = fave
                notifyItemChanged(index, fave)
            }
        }
    }

    fun removeFave(fave: Fave) {
        var indexToRemove: Int = -1

        for ((index, memeModel) in faves.withIndex()) {
            if (memeModel.equals(fave)) {
                indexToRemove = index
            }
        }

        faves.removeAt(indexToRemove)
        notifyItemRemoved(indexToRemove)
    }

    private fun hasBeenAdded(fave: Fave):Boolean {
        var added = false

        for (f in faves) {
            if (f.equals(fave)) {
                added = true
            }
        }

        return added
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaveHolder {
        return FaveHolder(parent.inflate(R.layout.item_fave), callback)
    }

    override fun getItemCount(): Int = faves.size

    override fun onBindViewHolder(holder: FaveHolder, position: Int) {
        holder.bind(faves[position])
    }

    class FaveHolder(private val binding: ItemFaveBinding, private val callback: FavesCallback):
            RecyclerView.ViewHolder(binding.root) {

        fun bind(fave: Fave) {
            binding.meme = fave
            binding.callback = callback
        }

    }

}