package com.gelostech.dankmemes.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.ui.activities.ProfileActivity
import com.gelostech.dankmemes.ui.adapters.NotificationsAdapter
import com.gelostech.dankmemes.ui.callbacks.NotificationsCallback
import com.gelostech.dankmemes.ui.base.BaseFragment
import com.gelostech.dankmemes.data.models.Notification
import com.gelostech.dankmemes.databinding.FragmentNotificationsBinding
import com.gelostech.dankmemes.ui.activities.MemeActivity
import com.gelostech.dankmemes.ui.viewmodels.NotificationsViewModel
import com.gelostech.dankmemes.utils.*
import kotlinx.android.synthetic.main.fragment_notifications.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class NotificationsFragment : BaseFragment() {
    private lateinit var notificationsAdapter: NotificationsAdapter
    private val notificationsViewModel: NotificationsViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val binding = DataBindingUtil.inflate<FragmentNotificationsBinding>(inflater, R.layout.fragment_notifications, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initEmptyStateObserver()
        initNotificationsObserver()
    }

    private fun initViews() {
        notificationsAdapter = NotificationsAdapter(callback)

        notifRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            addItemDecoration(RecyclerFormatter.SimpleDividerItemDecoration(activity!!))
            adapter = notificationsAdapter
            AppUtils.handleHomeScrolling(this)
        }

        notifRefresh.setOnRefreshListener {
            notificationsAdapter.currentList?.dataSource?.invalidate()
            runDelayed(2500) { notifRefresh.isRefreshing = false }
        }
    }

    private fun initNotificationsObserver() {
        notificationsViewModel.fetchNotifications().observe(this, Observer {
            notificationsAdapter.submitList(it)
        })
    }

    /**
     * Initialize function to observer Empty State LiveData
     */
    private fun initEmptyStateObserver() {
        notificationsViewModel.showEmptyStateLiveData.observe(this, Observer {
            when (it) {
                true -> {
                    notifRv.hideView()
                    emptyState.showView()
                }
                else -> {
                    emptyState.hideView()
                    notifRv.showView()
                }
            }
        })
    }

    private val callback = object : NotificationsCallback {
        override fun onNotificationClicked(view: View, notification: Notification) {
            when(view.id) {
                R.id.avatar -> {
                    val i = Intent(activity, ProfileActivity::class.java)
                    i.putExtra(Constants.USER_ID, notification.userId)
                    startActivity(i)
                    AppUtils.slideRight(activity!!)
                }

                else -> {
                    val i = Intent(activity, MemeActivity::class.java)
                    i.putExtra(Constants.MEME_ID, notification.memeId)
                    startActivity(i)
                    AppUtils.slideRight(activity!!)
                }
            }
        }
    }

}

