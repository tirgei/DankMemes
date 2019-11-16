package com.gelostech.dankmemes.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gelostech.dankmemes.data.Result
import com.gelostech.dankmemes.data.models.Comment
import com.gelostech.dankmemes.data.repositories.CommentsRepository
import com.gelostech.dankmemes.data.responses.GenericResponse
import kotlinx.coroutines.launch

class CommentsViewModel constructor(private val repository: CommentsRepository): ViewModel() {
    private val _genericResponseLiveData = MutableLiveData<GenericResponse>()
    val genericResponseLiveData = _genericResponseLiveData

    /**
     * Funtion to post new comment
     * @param comment - The comment model
     */
    fun postComment(comment: Comment) {
        viewModelScope.launch {
            _genericResponseLiveData.value = GenericResponse.loading()

            when (val result = repository.postComment(comment)) {
                is Result.Success -> {
                    _genericResponseLiveData.value = GenericResponse.success(result.data, item = GenericResponse.ITEM_RESPONSE.POST_COMMENT)
                }

                is Result.Error -> {
                    _genericResponseLiveData.value = GenericResponse.error(result.error)
                }
            }
        }
    }

}