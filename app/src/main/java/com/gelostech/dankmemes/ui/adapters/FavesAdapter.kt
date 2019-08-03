package com.gelostech.dankmemes.ui.adapters

import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.models.FaveModel
import com.gelostech.dankmemes.utils.Constants
import com.gelostech.dankmemes.utils.inflate
import com.gelostech.dankmemes.utils.loadUrl
import com.makeramen.roundedimageview.RoundedDrawable
import kotlinx.android.synthetic.main.item_fave.view.*
import java.lang.ref.WeakReference

class FavesAdapter(private val onItemClickListener: OnItemClickListener) : RecyclerView.Adapter<FavesAdapter.FaveHolder>() {
    private val faves = mutableListOf<FaveModel>()

    fun addFave(fave: FaveModel) {
        if (!hasBeenAdded(fave)) {
            faves.add(fave)
            notifyItemInserted(faves.size-1)
        }
    }

    fun updateFave(fave: FaveModel) {
        for ((index, memeModel) in faves.withIndex()) {
            if (fave.id == memeModel.id) {
                faves[index] = fave
                notifyItemChanged(index, fave)
            }
        }
    }

    fun removeFave(fave: FaveModel) {
        var indexToRemove: Int = -1

        for ((index, memeModel) in faves.withIndex()) {
            if (fave.id == memeModel.id) {
                indexToRemove = index
            }
        }

        faves.removeAt(indexToRemove)
        notifyItemRemoved(indexToRemove)
    }

    private fun hasBeenAdded(fave: FaveModel):Boolean {
        var added = false

        for (f in faves) {
            if (f.id == fave.id) {
                added = true
            }
        }

        return added
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaveHolder {
        return FaveHolder(parent.inflate(R.layout.item_fave), onItemClickListener)
    }

    override fun getItemCount(): Int = faves.size

    override fun getItemViewType(position: Int): Int {
        return when(faves[position].type) {
            Constants.FEED_TYPE_MEME_IMAGE -> Constants.FEED_TYPE_MEME_IMAGE

            else -> -1
        }
    }

    override fun onBindViewHolder(holder: FaveHolder, position: Int) {
        holder.bindViews(faves[position])
    }

    class FaveHolder(itemView: View, onItemClickListener: OnItemClickListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        private val memeView = itemView.faveImage
        private var weakReference: WeakReference<OnItemClickListener> = WeakReference(onItemClickListener)
        private lateinit var fave: FaveModel
        private lateinit var image: Bitmap

        init {
            memeView.setOnClickListener(this)
            memeView.setOnLongClickListener(this)
        }

        fun bindViews(fave: FaveModel) {
            this.fave = fave

            with(fave) {
                memeView.loadUrl(imageUrl!!)
            }
        }

        override fun onClick(v: View?) {
            image = (memeView.drawable as RoundedDrawable).sourceBitmap

            when(v!!.id) {
                memeView.id -> weakReference.get()!!.onItemClick(fave, image)
            }
        }

        override fun onLongClick(v: View?): Boolean {
            when(v?.id) {
                memeView.id -> weakReference.get()!!.onLongItemClick(fave)
            }
            return true
        }
    }

    interface OnItemClickListener{
        fun onItemClick(fave: FaveModel, image: Bitmap)

        fun onLongItemClick(fave: FaveModel)

    }
}