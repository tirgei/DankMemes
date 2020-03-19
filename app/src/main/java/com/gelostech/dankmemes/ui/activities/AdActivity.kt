package com.gelostech.dankmemes.ui.activities

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.models.Item
import com.gelostech.dankmemes.ui.adapters.AdAdapter
import com.gelostech.dankmemes.ui.base.BaseActivity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.formats.UnifiedNativeAd
import kotlinx.android.synthetic.main.activity_ad.*
import org.koin.android.ext.android.inject
import timber.log.Timber

class AdActivity : BaseActivity() {
    private lateinit var itemAdapter: AdAdapter
    private val adBuilder: AdLoader.Builder by inject()
    private lateinit var adLoader: AdLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ad)

        initViews()
        loadStuff()
    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Sample ads feed"

        itemAdapter = AdAdapter()
        rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@AdActivity)
            adapter = itemAdapter
        }
    }

    private fun loadStuff() {
        val items = mutableListOf<Item>()
        for (i in 0..25) {
            val item = Item("Title $i", "Description: $i - $i")
            items.add(item)
        }

        loadAds(items)
    }

    private fun loadAds(items: List<Any>) {
        val ads = mutableListOf<UnifiedNativeAd>()

        adLoader = adBuilder.forUnifiedNativeAd { unifiedNativeAd ->
            // A native ad loaded successfully, check if the ad loader has finished loading
            // and if so, insert the ads into the list.
            ads.add(unifiedNativeAd)
            Timber.e("Ad loaded")
            if (!adLoader.isLoading) {
                insertAdsInMenuItems(items.toMutableList(), ads)
            }
        }.withAdListener(
                object : AdListener() {
                    override fun onAdFailedToLoad(errorCode: Int) {
                        // A native ad failed to load, check if the ad loader has finished loading
                        // and if so, insert the ads into the list.
                        if (!adLoader.isLoading) {
                            insertAdsInMenuItems(items.toMutableList(), ads)
                        }
                        Timber.e("Error loading ad: %s", errorCode)
                    }
                }).build()


        // Load the Native Express ad.
        adLoader.loadAds(AdRequest.Builder().build(), 5)
    }

    private fun insertAdsInMenuItems(items: MutableList<Any>, mNativeAds: List<UnifiedNativeAd>) {
        if (mNativeAds.isEmpty()) {
            return
        }

        Timber.e("Inserting ${mNativeAds.size} ads...")

        val offset = items.size / mNativeAds.size + 1
        var index = 5
        for (ad in mNativeAds) {
            items.add(index, ad)
            index += offset
        }

        itemAdapter.addItems(items)
    }
}
