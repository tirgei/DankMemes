package com.gelostech.dankmemes.data.datasource

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import com.gelostech.dankmemes.data.repositories.MemesRepository
import com.gelostech.dankmemes.data.wrappers.ObservableMeme
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MemesDataSource constructor(private val repository: MemesRepository,
                                  private val scope: CoroutineScope): ItemKeyedDataSource<String, ObservableMeme>() {

    class Factory(private val repository: MemesRepository,
                  private val scope: CoroutineScope): DataSource.Factory<String, ObservableMeme>() {
        override fun create(): DataSource<String, ObservableMeme> {
            return MemesDataSource(repository, scope)
        }
    }

    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<ObservableMeme>) {
        Timber.e("Loading initial...")

        scope.launch {
            val memes = repository.fetchMemes()
            Timber.e("Memes fetched: ${memes.size}")

            memes.forEach {currentMeme ->
                currentMeme.meme.subscribeBy(
                        onComplete = {
                            Timber.e("Subscribing")
                        },
                        onError = { Timber.e("Error observing meme: $it") }
                )
            }

            callback.onResult(memes)
        }
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<ObservableMeme>) {
        scope.launch {
            val memes = repository.fetchMemes(loadAfter = params.key)
            Timber.e("Memes fetched: ${memes.size}")
            callback.onResult(memes)
        }
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<ObservableMeme>) {
        scope.launch {
            val memes = repository.fetchMemes(loadBefore = params.key)
            Timber.e("Memes fetched: ${memes.size}")
            callback.onResult(memes)
        }
    }

    override fun getKey(item: ObservableMeme): String = item.id
}