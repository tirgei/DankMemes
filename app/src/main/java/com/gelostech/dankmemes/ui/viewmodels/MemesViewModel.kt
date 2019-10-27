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
import kotlinx.coroutines.launch

class MemesViewModel constructor(private val repository: MemesRepository): ViewModel() {
    private val _postMemeLiveData = MutableLiveData<GenericResponse>()
    val postMemeLiveData: MutableLiveData<GenericResponse>
        get() = _postMemeLiveData

    private val pagingConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPrefetchDistance(15)
            .build()

    val memesLiveData: LiveData<PagedList<ObservableMeme>> =
            LivePagedListBuilder<String, ObservableMeme>(
                    MemesDataSource.Factory(repository, viewModelScope), pagingConfig
            ).build()

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

}