package com.gelostech.dankmemes.data.datasource

import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import com.gelostech.dankmemes.data.models.Notification
import com.gelostech.dankmemes.data.repositories.NotificationsRepository
import com.gelostech.dankmemes.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class NotificationsDataSource constructor(private val repository: NotificationsRepository,
                                  private val scope: CoroutineScope,
                                  private val onEmptyAction: (Boolean) -> Unit): ItemKeyedDataSource<String, Notification>(), KoinComponent {

    private val sessionManager: SessionManager by inject()
    private val userId = sessionManager.getUserId()

    class Factory(private val repository: NotificationsRepository,
                  private val scope: CoroutineScope,
                  private val onEmptyAction: (Boolean) -> Unit): DataSource.Factory<String, Notification>() {
        override fun create(): DataSource<String, Notification> {
            return NotificationsDataSource(repository, scope, onEmptyAction)
        }
    }

    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<Notification>) {
        Timber.e("Loading initial...")

        scope.launch {
            val notifications = repository.fetchNotifications(userId)

            onEmptyAction(notifications.isEmpty())
            callback.onResult(notifications)
        }
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<Notification>) {
        scope.launch {
            val notifications = repository.fetchNotifications(userId = userId, loadAfter = params.key)
            Timber.e("Notifications fetched: ${notifications.size}")
            callback.onResult(notifications)
        }
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<Notification>) {
        scope.launch {
            val notifications = repository.fetchNotifications(userId = userId, loadBefore = params.key)
            Timber.e("Notifications fetched: ${notifications.size}")
            callback.onResult(notifications)
        }
    }

    override fun getKey(item: Notification): String = item.id!!
}