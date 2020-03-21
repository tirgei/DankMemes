package com.gelostech.dankmemes.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.gelostech.dankmemes.data.datasource.NotificationsDataSource
import com.gelostech.dankmemes.data.models.Notification
import com.gelostech.dankmemes.data.repositories.NotificationsRepository

class NotificationsViewModel constructor(private val repository: NotificationsRepository): ViewModel() {
    private var _notificationsLiveData: LiveData<PagedList<Notification>>
    private val _showEmptyStateLiveData = MutableLiveData<Boolean>()
    val showEmptyStateLiveData: MutableLiveData<Boolean>
        get() = _showEmptyStateLiveData

    init {
        _notificationsLiveData = initializePagedNotificationsBuilder().build()
        _showEmptyStateLiveData.value = false
    }

    /**
     * Function to fetch notification
     */
    fun fetchNotifications(): LiveData<PagedList<Notification>> = _notificationsLiveData

    /**
     * Function to fetch all notifications
     */
    private fun initializePagedNotificationsBuilder(): LivePagedListBuilder<String, Notification> {
        val pagingConfig = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(5)
                .build()

        val faveFactory = NotificationsDataSource.Factory(repository, viewModelScope) {
            _showEmptyStateLiveData.value = it
        }
        return LivePagedListBuilder<String, Notification>(faveFactory, pagingConfig)
    }

}