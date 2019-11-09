package com.gelostech.dankmemes.ui.fragments


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.ui.activities.ProfileActivity
import com.gelostech.dankmemes.ui.adapters.NotificationsAdapter
import com.gelostech.dankmemes.ui.callbacks.NotificationsCallback
import com.gelostech.dankmemes.utils.AppUtils
import com.gelostech.dankmemes.ui.base.BaseFragment
import com.gelostech.dankmemes.utils.Constants
import com.gelostech.dankmemes.data.models.Notification
import com.gelostech.dankmemes.ui.viewmodels.NotificationsViewModel
import com.gelostech.dankmemes.utils.RecyclerFormatter
import com.gelostech.dankmemes.utils.hideView
import com.gelostech.dankmemes.utils.showView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_notifications.*
import org.koin.android.ext.android.inject
import timber.log.Timber


class NotificationsFragment : BaseFragment() {
    private lateinit var adapter: NotificationsAdapter
    private val notificationsViewModel: NotificationsViewModel by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initNotificationsObserver()
    }

    private fun initViews() {
        adapter = NotificationsAdapter(callback)

        notifRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            addItemDecoration(RecyclerFormatter.SimpleDividerItemDecoration(activity!!))
            adapter = adapter
        }

        notifRefresh.isEnabled = false
        notifRefresh.setOnRefreshListener {
            Handler().postDelayed({notifRefresh?.isRefreshing = false}, 2500)
        }

    }

    private fun initNotificationsObserver() {
        notificationsViewModel.fetchNotifications().observe(this, Observer {
            adapter.submitList(it)
        })
    }

    private val callback = object : NotificationsCallback {
        override fun onNotificationClicked(view: View, notification: Notification) {
            when(view.id) {
                R.id.avatar -> {
                    val i = Intent(activity, ProfileActivity::class.java)
                    i.putExtra("userId", notification.userId)
                    startActivity(i)
                    AppUtils.animateEnterRight(activity!!)
                }
            }
        }
    }

    private fun noNotifs() {
        notifRefresh?.hideView()
        notifsEmptyState?.showView()
    }

    private fun hasNotifs() {
        notifsEmptyState?.hideView()
        notifRefresh?.showView()
    }
}

