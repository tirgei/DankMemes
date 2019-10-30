package com.gelostech.dankmemes.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.gelostech.dankmemes.data.Result
import com.gelostech.dankmemes.data.datasource.MemesDataSource
import com.gelostech.dankmemes.data.models.Meme
import com.gelostech.dankmemes.data.repositories.MemesRepository
import com.gelostech.dankmemes.data.responses.GenericResponse
import com.gelostech.dankmemes.data.wrappers.ObservableMeme
import com.gelostech.dankmemes.utils.Constants
import kotlinx.coroutines.launch

class MemesViewModel constructor(private val repository: MemesRepository): ViewModel() {
    private val _postMemeLiveData = MutableLiveData<GenericResponse>()
    val postMemeLiveData: MutableLiveData<GenericResponse>
        get() = _postMemeLiveData

    private var _memesLiveData: LiveData<PagedList<ObservableMeme>>

    init {
        _memesLiveData = initializePagedMemesBuilder().build()
    }

    /**
     * Function to post new Meme
     * @param imageUri - Uri of the selected meme
     * @param meme - Meme model
     */
    fun postMeme(imageUri: Uri, meme: Meme) {
        viewModelScope.launch {
            _postMemeLiveData.value = GenericResponse.loading()

            repository.postMeme(imageUri, meme) {
                when(it) {
                    is Result.Success -> {
                        _postMemeLiveData.postValue(GenericResponse.success(it.data))
                    }

                    is Result.Error -> {
                        _postMemeLiveData.postValue(GenericResponse.error(it.error))
                    }
                }
            }
        }
    }

    /**
     * Function to fetch memes
     */
    fun fetchMemes(): LiveData<PagedList<ObservableMeme>> = _memesLiveData

    /**
     * Function to fetch all memes
     */
    private fun initializePagedMemesBuilder(): LivePagedListBuilder<String, ObservableMeme> {
        val pagingConfig = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(5)
                .build()

        val memeFactory = MemesDataSource.Factory(repository, viewModelScope)
        return LivePagedListBuilder<String, ObservableMeme>(memeFactory, pagingConfig)
    }

}