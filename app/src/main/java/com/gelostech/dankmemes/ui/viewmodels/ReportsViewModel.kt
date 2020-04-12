package com.gelostech.dankmemes.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.gelostech.dankmemes.data.Status
import com.gelostech.dankmemes.data.datasource.ReportsDataSource
import com.gelostech.dankmemes.data.models.Report
import com.gelostech.dankmemes.data.repositories.ReportsRepository

/**
 * ViewModel for reports
 */
class ReportsViewModel constructor(private val repository: ReportsRepository): ViewModel() {
    private val _showStatusLiveData = MutableLiveData<Status>()
    val showStatusLiveData: MutableLiveData<Status>
        get() = _showStatusLiveData

    /**
     * Function to fetch reports
     */
    fun fetchReports(): LiveData<PagedList<Report>> {
        val pagingConfig = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(5)
                .build()

        val reportFactory = ReportsDataSource.Factory(repository, viewModelScope) {
            _showStatusLiveData.postValue(it)
        }
        return LivePagedListBuilder<String, Report>(reportFactory, pagingConfig).build()
    }


}