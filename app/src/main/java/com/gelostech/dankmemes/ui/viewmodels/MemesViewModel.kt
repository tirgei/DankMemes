package com.gelostech.dankmemes.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.gelostech.dankmemes.data.datasource.MemesDataSource
import com.gelostech.dankmemes.data.repositories.MemesRepository
import com.gelostech.dankmemes.data.wrappers.ObservableMeme

class MemesViewModel constructor(private val repository: MemesRepository): ViewModel() {
    private val scope = viewModelScope

    private val pagingConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPrefetchDistance(15)
            .build()

    val memesLiveData: LiveData<PagedList<ObservableMeme>> =
            LivePagedListBuilder<String, ObservableMeme>(
                    MemesDataSource.Factory(repository, scope), pagingConfig
            ).build()


}