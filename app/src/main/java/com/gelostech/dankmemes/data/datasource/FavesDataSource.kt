package com.gelostech.dankmemes.data.datasource

import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import com.gelostech.dankmemes.data.models.Fave
import com.gelostech.dankmemes.data.repositories.MemesRepository
import com.gelostech.dankmemes.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class FavesDataSource constructor(private val repository: MemesRepository,
                                  private val scope: CoroutineScope,
                                  private val onEmptyAction: (Boolean) -> Unit): ItemKeyedDataSource<String, Fave>(), KoinComponent {

    private val sessionManager: SessionManager by inject()
    private val userId = sessionManager.getUserId()

    class Factory(private val repository: MemesRepository,
                  private val scope: CoroutineScope,
                  private val onEmptyAction: (Boolean) -> Unit): DataSource.Factory<String, Fave>() {
        override fun create(): DataSource<String, Fave> {
            return FavesDataSource(repository, scope, onEmptyAction)
        }
    }

    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<Fave>) {
        Timber.e("Loading initial...")

        scope.launch {
            val memes = repository.fetchFaves(userId)

            onEmptyAction(memes.isEmpty())
            callback.onResult(memes)
        }
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<Fave>) {
        scope.launch {
            val memes = repository.fetchFaves(userId = userId, loadAfter = params.key)
            Timber.e("Faves fetched: ${memes.size}")
            callback.onResult(memes)
        }
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<Fave>) {
        scope.launch {
            val memes = repository.fetchFaves(userId = userId, loadBefore = params.key)
            Timber.e("Faves fetched: ${memes.size}")
            callback.onResult(memes)
        }
    }

    override fun getKey(item: Fave): String = item.id!!
}