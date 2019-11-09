package com.gelostech.dankmemes.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.gelostech.dankmemes.data.datasource.NotificationsDataSource
import com.gelostech.dankmemes.data.models.Notification
import com.gelostech.dankmemes.data.repositories.NotificationsRepository

class NotificationsViewModel constructor(private val repository: NotificationsRepository): ViewModel() {
    private var _notificationsLiveData: LiveData<PagedList<Notification>>

    init {
        _notificationsLiveData = initializePagedNotificationsBuilder().build()
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

        val faveFactory = NotificationsDataSource.Factory(repository, viewModelScope)
        return LivePagedListBuilder<String, Notification>(faveFactory, pagingConfig)
    }

}