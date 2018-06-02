package com.gelostech.dankmemes.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.models.CollectionModel
import com.gelostech.dankmemes.utils.inflate
import com.gelostech.dankmemes.utils.loadUrl
import kotlinx.android.synthetic.main.item_collection.view.*

class CollectionsAdapter(context: Context) : RecyclerView.Adapter<CollectionsAdapter.CollectionsHolder>() {
    private val collections = mutableListOf<CollectionModel>()

    fun addCollection(collection: CollectionModel) {
        collections.add(collection)
        notifyItemInserted(collections.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionsHolder {
        return CollectionsHolder(parent.inflate(R.layout.item_collection))
    }

    override fun getItemCount(): Int = collections.size

    override fun onBindViewHolder(holder: CollectionsHolder, position: Int) {
        holder.bindViews(collections[position])
    }

    class CollectionsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val collectionImage = itemView.collectionImage
        private val collectionName = itemView.collectionName

        fun bindViews(collection: CollectionModel) {
            with(collection) {
                collectionImage.loadUrl(R.drawable.games)
                collectionName.text = name
            }
        }

    }

}