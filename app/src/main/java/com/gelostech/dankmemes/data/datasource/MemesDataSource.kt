package com.gelostech.dankmemes.data.datasource

import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import com.gelostech.dankmemes.data.repositories.MemesRepository
import com.gelostech.dankmemes.data.wrappers.ObservableMeme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MemesDataSource constructor(private val repository: MemesRepository,
                                  private val scope: CoroutineScope): ItemKeyedDataSource<String, ObservableMeme>() {

    class Factory(private val repository: MemesRepository,
                  private val scope: CoroutineScope): DataSource.Factory<String, ObservableMeme>() {
        override fun create(): DataSource<String, ObservableMeme> {
            return MemesDataSource(repository, scope)
        }
    }

    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<ObservableMeme>) {
        scope.launch {
            val memes = repository.fetchMemes()
            callback.onResult(memes)
        }
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<ObservableMeme>) {
        scope.launch {
            val memes = repository.fetchMemes(loadAfter = params.key)
            callback.onResult(memes)
        }
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<ObservableMeme>) {
        scope.launch {
            val memes = repository.fetchMemes(loadBefore = params.key)
            callback.onResult(memes)
        }
    }

    override fun getKey(item: ObservableMeme): String = item.id
}