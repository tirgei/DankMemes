package com.gelostech.dankmemes.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gelostech.dankmemes.data.Result
import com.gelostech.dankmemes.data.models.Comment
import com.gelostech.dankmemes.data.repositories.CommentsRepository
import com.gelostech.dankmemes.data.responses.CommentsResponse
import com.gelostech.dankmemes.data.responses.GenericResponse
import kotlinx.coroutines.launch

class CommentsViewModel constructor(private val repository: CommentsRepository): ViewModel() {
    private val _genericResponseLiveData = MutableLiveData<GenericResponse>()
    val genericResponseLiveData = _genericResponseLiveData

    private val _commentsLiveData = MutableLiveData<CommentsResponse>()
    val commentsLiveData = _commentsLiveData

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

    /**
     * Function to delete comment
     * @param memeId - ID of the meme
     * @param commentId - ID of the comment
     */
    fun deleteComment(memeId: String, commentId: String) {
        viewModelScope.launch {
            _genericResponseLiveData.value = GenericResponse.loading()

            when (val result = repository.deleteComment(memeId, commentId)) {
                is Result.Success -> {
                    _genericResponseLiveData.value = GenericResponse.success(result.data,
                            item = GenericResponse.ITEM_RESPONSE.DELETE_COMMENT,
                            id = commentId)
                }

                is Result.Error -> {
                    _genericResponseLiveData.value = GenericResponse.error(result.error)
                }
            }
        }
    }

    /**
     * Function to fetch all comments for a Meme
     */
    fun fetchComments(memeId: String) {
        viewModelScope.launch {
            _commentsLiveData.value = CommentsResponse.loading()

            when (val result = repository.fetchComments(memeId)) {
                is Result.Success -> {
                    _commentsLiveData.value = CommentsResponse.success(result.data)
                }

                is Result.Error -> {
                    _commentsLiveData.value = CommentsResponse.error(result.error)
                }
            }
        }
    }
}