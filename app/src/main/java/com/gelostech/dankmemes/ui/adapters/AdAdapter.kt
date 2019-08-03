package com.gelostech.dankmemes.ui.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.models.Item
import com.gelostech.dankmemes.utils.inflate
import com.google.android.gms.ads.formats.MediaView
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import kotlinx.android.synthetic.main.basic_layout.view.*
import timber.log.Timber


class AdAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = mutableListOf<Any>()

    companion object {
        const val MEME = 0
        const val AD = 1
    }

    fun addItems(items: List<Any>) {
        Timber.e("Adding ${items.size} items")

        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            AD -> AdHolder(parent.inflate(R.layout.item_native_ad))
            else -> ItemHolder(parent.inflate(R.layout.basic_layout))
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        if (items[position] is UnifiedNativeAd)
            return AD

        return MEME
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AdHolder -> holder.bind(items[position] as UnifiedNativeAd)
            is ItemHolder -> holder.bind(items[position] as Item)
        }
    }

    inner class ItemHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val titleTv = itemView.title
        val descTv = itemView.description

        fun bind(item: Item) {
            titleTv.text = item.title
            descTv.text = item.desc
        }
    }

    inner class AdHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private var adView: UnifiedNativeAdView = itemView.findViewById(R.id.ad_view)

        init {
            adView.mediaView = adView.findViewById(R.id.ad_media) as MediaView

            // Register the view used for each individual asset.
            adView.headlineView = adView.findViewById(R.id.ad_headline);
            adView.bodyView = adView.findViewById(R.id.ad_body);
            adView.callToActionView = adView.findViewById(R.id.ad_call_to_action);
            adView.iconView = adView.findViewById(R.id.ad_icon);
            adView.priceView = adView.findViewById(R.id.ad_price);
            adView.starRatingView = adView.findViewById(R.id.ad_stars);
            adView.storeView = adView.findViewById(R.id.ad_store);
            adView.advertiserView = adView.findViewById(R.id.ad_advertiser)
        }

        fun bind(nativeAd: UnifiedNativeAd) {
            (adView.headlineView as TextView).text = nativeAd.headline
            (adView.bodyView as TextView).text = nativeAd.body
            (adView.callToActionView as Button).text = nativeAd.callToAction

            // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
            // check before trying to display them.
            val icon = nativeAd.icon

            if (icon == null) {
                adView.iconView.visibility = View.INVISIBLE
            } else {
                (adView.iconView as ImageView).setImageDrawable(icon.drawable)
                adView.iconView.visibility = View.VISIBLE
            }

            if (nativeAd.price == null) {
                adView.priceView.visibility = View.INVISIBLE
            } else {
                adView.priceView.visibility = View.VISIBLE
                (adView.priceView as TextView).text = nativeAd.price
            }

            if (nativeAd.store == null) {
                adView.storeView.visibility = View.INVISIBLE
            } else {
                adView.storeView.visibility = View.VISIBLE
                (adView.storeView as TextView).text = nativeAd.store
            }

            if (nativeAd.starRating == null) {
                adView.starRatingView.visibility = View.INVISIBLE
            } else {
                (adView.starRatingView as RatingBar).rating = nativeAd.starRating.toFloat()
                adView.starRatingView.visibility = View.VISIBLE
            }

            if (nativeAd.advertiser == null) {
                adView.advertiserView.visibility = View.INVISIBLE
            } else {
                (adView.advertiserView as TextView).text = nativeAd.advertiser
                adView.advertiserView.visibility = View.VISIBLE
            }

            // Assign native ad object to the native view.
            adView.setNativeAd(nativeAd)
        }

    }

}