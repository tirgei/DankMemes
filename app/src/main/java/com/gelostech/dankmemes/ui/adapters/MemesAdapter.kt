package com.gelostech.dankmemes.ui.adapters

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
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
import com.google.android.gms.ads.formats.MediaView
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
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
        return when (viewType) {
            AD -> AdHolder(parent.legacyInflate(R.layout.item_native_ad))
            else -> MemeHolder(parent.inflate(R.layout.item_meme), callback)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AdHolder -> {
                val adWrapper = getItem(position) as NativeAdWrapper
                holder.bind(adWrapper.ad)
            }

            is MemeHolder -> {
                val meme = getItem(position) as ObservableMeme

                meme.meme.subscribeBy(
                        onNext = { holder.bind(it) },
                        onError = {
                            Timber.e("Meme deleted")
                            this.currentList?.dataSource?.invalidate()
                        }
                ).addTo(holder.disposables)
            }
        }
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