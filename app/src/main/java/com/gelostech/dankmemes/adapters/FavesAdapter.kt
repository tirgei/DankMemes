package com.gelostech.dankmemes.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.models.FaveModel
import com.gelostech.dankmemes.utils.inflate
import com.gelostech.dankmemes.utils.loadUrl
import kotlinx.android.synthetic.main.item_fave.view.*
import java.lang.ref.WeakReference

class FavesAdapter(val onItemClickListener: OnItemClickListener) : RecyclerView.Adapter<FavesAdapter.FaveHolder>() {
    private val faves = mutableListOf<FaveModel>()

    fun addFave(fave: FaveModel) {
        faves.add(fave)
        notifyItemInserted(faves.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaveHolder {
        return FaveHolder(parent.inflate(R.layout.item_fave), onItemClickListener)
    }

    override fun getItemCount(): Int = faves.size

    override fun onBindViewHolder(holder: FaveHolder, position: Int) {
        holder.bindViews(faves[position])
    }

    class FaveHolder(itemView: View, onItemClickListener: OnItemClickListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val memeView = itemView.faveImage
        private var weakReference: WeakReference<OnItemClickListener> = WeakReference(onItemClickListener)
        private lateinit var fave: FaveModel

        init {
            memeView.setOnClickListener(this)
        }

        fun bindViews(fave: FaveModel) {
            this.fave = fave

            with(fave) {
                memeView.loadUrl(image!!)
            }
        }

        override fun onClick(v: View?) {
            when(v!!.id) {
                memeView.id -> weakReference.get()!!.onItemClick(fave)
            }
        }
    }

    interface OnItemClickListener{

        fun onItemClick(fave: FaveModel)

    }
}