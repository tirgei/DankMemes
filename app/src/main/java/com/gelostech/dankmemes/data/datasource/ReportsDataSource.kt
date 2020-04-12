package com.gelostech.dankmemes.data.datasource

import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import com.gelostech.dankmemes.data.Status
import com.gelostech.dankmemes.data.models.Report
import com.gelostech.dankmemes.data.repositories.ReportsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber


class ReportsDataSource constructor(private val repository: ReportsRepository,
                                          private val scope: CoroutineScope,
                                          private val status: (Status) -> Unit): ItemKeyedDataSource<String, Report>(), KoinComponent {

    class Factory(private val repository: ReportsRepository,
                  private val scope: CoroutineScope,
                  private val status: (Status) -> Unit): DataSource.Factory<String, Report>() {
        override fun create(): DataSource<String, Report> {
            return ReportsDataSource(repository, scope, status)
        }
    }

    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<Report>) {
        status(Status.LOADING)

        scope.launch {
            val reports = repository.fetchReports()

            if (reports.isEmpty()) status(Status.ERROR) else status(Status.SUCCESS)
            callback.onResult(reports)
        }
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<Report>) {
        scope.launch {
            val reports = repository.fetchReports(loadAfter = params.key)
            callback.onResult(reports)
        }
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<Report>) {
        scope.launch {
            val reports = repository.fetchReports(loadBefore = params.key)
            callback.onResult(reports)
        }
    }

    override fun getKey(item: Report): String = item.id!!
}