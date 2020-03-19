package com.gelostech.dankmemes.data.datasource

import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import com.gelostech.dankmemes.data.Status
import com.gelostech.dankmemes.data.repositories.MemesRepository
import com.gelostech.dankmemes.data.wrappers.ItemViewModel
import com.gelostech.dankmemes.data.wrappers.ObservableUser
import com.google.android.gms.ads.AdLoader
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

    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<ItemViewModel>) {
        Timber.e("Loading initial...")
        status(Status.LOADING)

        scope.launch {
            if (user == null) {
                val memes = repository.fetchMemes()

                if (memes.isEmpty()) status(Status.ERROR) else status (Status.SUCCESS)
                callback.onResult(memes)
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
                val memes = if (user == null) {
                    repository.fetchMemes(loadAfter = params.key)
                } else {
                    repository.fetchMemesByUser(userId = user.id, loadAfter = params.key)
                }

                callback.onResult(memes)
            }
        }
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<ItemViewModel>) {
//        scope.launch {
//            val memes = if (user == null) {
//                repository.fetchMemes(loadBefore = params.key)
//            } else {
//                repository.fetchMemesByUser(userId = user.userId!!, loadBefore = params.key)
//            }
//
//            callback.onResult(memes)
//        }
    }

    override fun getKey(item: ItemViewModel): String = item.id
}