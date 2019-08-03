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
import timber.log.Timber

class AdActivity : BaseActivity() {
    private lateinit var itemAdapter: AdAdapter
    private val items = mutableListOf<Any>()
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
        for (i in 1..100) {
            val item = Item("Title $i", "Description: $i - $i")
            items.add(item)
        }

        loadAds()
    }

    private fun loadAds() {
        val ads = mutableListOf<UnifiedNativeAd>()

        val builder = AdLoader.Builder(this, getString(R.string.admob_native_test_ad))
        adLoader = builder.forUnifiedNativeAd { unifiedNativeAd ->
            // A native ad loaded successfully, check if the ad loader has finished loading
            // and if so, insert the ads into the list.
            ads.add(unifiedNativeAd)
            Timber.e("Ad loaded")
            if (!adLoader.isLoading) {
                insertAdsInMenuItems(ads)
            }
        }.withAdListener(
                object : AdListener() {
                    override fun onAdFailedToLoad(errorCode: Int) {
                        // A native ad failed to load, check if the ad loader has finished loading
                        // and if so, insert the ads into the list.
                        if (!adLoader.isLoading) {
                            insertAdsInMenuItems(ads)
                        }
                        Timber.e("Error loading ad: %s", errorCode)
                    }
                }).build()


        // Load the Native Express ad.
        adLoader.loadAds(AdRequest.Builder().build(), 20)
    }

    private fun insertAdsInMenuItems(mNativeAds: List<UnifiedNativeAd>) {
        if (mNativeAds.isEmpty()) {
            return
        }

        Timber.e("Inserting ${mNativeAds.size} ads...")

        val offset = items.size / mNativeAds.size + 1
        var index = 0
        for (ad in mNativeAds) {
            items.add(index, ad)
            index += offset
        }

        itemAdapter.addItems(items)
    }
}
