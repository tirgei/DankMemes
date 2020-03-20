package com.gelostech.dankmemes.data.datasource

import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import com.gelostech.dankmemes.data.Status
import com.gelostech.dankmemes.data.repositories.MemesRepository
import com.gelostech.dankmemes.data.wrappers.ItemViewModel
import com.gelostech.dankmemes.data.wrappers.NativeAdWrapper
import com.gelostech.dankmemes.data.wrappers.ObservableUser
import com.gelostech.dankmemes.utils.AppUtils
import com.gelostech.dankmemes.utils.Constants
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.formats.UnifiedNativeAd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

class MemesDataSource constructor(private val repository: MemesRepository,
                                  private val scope: CoroutineScope,
                                  private val adBuilder: AdLoader.Builder,
                                  private val user: ObservableUser? = null,
                                  private val status: (Status) -> Unit): ItemKeyedDataSource<String, ItemViewModel>() {

    class Factory(private val repository: MemesRepository,
                  private val scope: CoroutineScope,
                  private val adBuilder: AdLoader.Builder,
                  private val user: ObservableUser? = null,
                  private val status: (Status) -> Unit): DataSource.Factory<String, ItemViewModel>() {

        override fun create(): DataSource<String, ItemViewModel> {
            return MemesDataSource(repository, scope, adBuilder, user, status)
        }
    }

    private lateinit var adLoader: AdLoader

    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<ItemViewModel>) {
        Timber.e("Loading initial...")
        status(Status.LOADING)

        scope.launch {
            if (user == null) {
                val memes = repository.fetchMemes()

                if (memes.isEmpty()) status(Status.ERROR)
                loadAds(memes, callback)
            } else {
                val memes = repository.fetchMemesByUser(user.id)
                memes.add(0, user)

                if (memes.size == 1) status(Status.ERROR) else status (Status.SUCCESS)
                callback.onResult(memes)
            }
        }
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<ItemViewModel>) {
        if (params.key != user?.id) {
            scope.launch {
                if (user == null) {
                    val memes =  repository.fetchMemes(loadAfter = params.key)
                    loadAds(memes, callback)
                } else {
                    val memes = repository.fetchMemesByUser(userId = user.id, loadAfter = params.key)
                    callback.onResult(memes)
                }
            }
        }
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<ItemViewModel>) {}

    override fun getKey(item: ItemViewModel): String = item.id

    private fun loadAds(items: List<ItemViewModel>, callback: LoadCallback<ItemViewModel>) {
        val ads = mutableListOf<UnifiedNativeAd>()

        adLoader = adBuilder.forUnifiedNativeAd { unifiedNativeAd ->
            // A native ad loaded successfully, check if the ad loader has finished loading
            // and if so, insert the ads into the list.
            ads.add(unifiedNativeAd)
            Timber.e("Ad loaded")
            if (!adLoader.isLoading) {
                insertAdsInMemeItems(items.toMutableList(), ads, callback)
            }
        }.withAdListener(
                object : AdListener() {
                    override fun onAdFailedToLoad(errorCode: Int) {
                        // A native ad failed to load, check if the ad loader has finished loading
                        // and if so, insert the ads into the list.
                        if (!adLoader.isLoading) {
                            Timber.e("Done loading ads")
                            insertAdsInMemeItems(items.toMutableList(), ads, callback)
                        } else {
                            Timber.e("Still loading ads...")
                        }
                    }
                }).build()


        // Load the Native Express ad.
        adLoader.loadAds(AdRequest.Builder().build(), Constants.AD_COUNT)
    }

    private fun insertAdsInMemeItems(items: MutableList<ItemViewModel>, mNativeAds: List<UnifiedNativeAd>, callback: LoadCallback<ItemViewModel>) {
        if (mNativeAds.isEmpty()) {
            if (items.isNotEmpty()) status(Status.SUCCESS)
            callback.onResult(items)
            return
        }

        val offset = items.size / mNativeAds.size + 1
        var index = 8
        for (ad in mNativeAds) {
            if (index <= items.lastIndex) items.add(index, NativeAdWrapper(AppUtils.randomIdGenerator(), ad))
            index += offset
        }

        if (items.isNotEmpty()) status(Status.SUCCESS)
        callback.onResult(items)
    }

}