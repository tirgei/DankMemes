package com.gelostech.dankmemes.data.datasource

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import com.gelostech.dankmemes.data.repositories.MemesRepository
import com.gelostech.dankmemes.data.wrappers.ItemViewModel
import com.gelostech.dankmemes.data.wrappers.ObservableMeme
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MemesDataSource constructor(private val repository: MemesRepository,
                                  private val scope: CoroutineScope): ItemKeyedDataSource<String, ItemViewModel>() {

    class Factory(private val repository: MemesRepository,
                  private val scope: CoroutineScope): DataSource.Factory<String, ItemViewModel>() {
        override fun create(): DataSource<String, ItemViewModel> {
            return MemesDataSource(repository, scope)
        }
    }

    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<ItemViewModel>) {
        Timber.e("Loading initial...")

        scope.launch {
            val memes = repository.fetchMemes()
            Timber.e("Memes fetched: ${memes.size}")
            callback.onResult(memes)
        }
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<ItemViewModel>) {
        scope.launch {
            val memes = repository.fetchMemes(loadAfter = params.key)
            Timber.e("Memes fetched: ${memes.size}")
            callback.onResult(memes)
        }
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<ItemViewModel>) {
        scope.launch {
            val memes = repository.fetchMemes(loadBefore = params.key)
            Timber.e("Memes fetched: ${memes.size}")
            callback.onResult(memes)
        }
    }

    override fun getKey(item: ItemViewModel): String = item.id
}