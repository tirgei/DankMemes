package com.gelostech.dankmemes.ui.fragments


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.ui.activities.ProfileActivity
import com.gelostech.dankmemes.ui.adapters.NotificationsAdapter
import com.gelostech.dankmemes.ui.callbacks.NotificationsCallback
import com.gelostech.dankmemes.utils.AppUtils
import com.gelostech.dankmemes.ui.base.BaseFragment
import com.gelostech.dankmemes.utils.Constants
import com.gelostech.dankmemes.data.models.Notification
import com.gelostech.dankmemes.utils.RecyclerFormatter
import com.gelostech.dankmemes.utils.hideView
import com.gelostech.dankmemes.utils.showView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_notifications.*
import timber.log.Timber


class NotificationsFragment : BaseFragment(), NotificationsCallback {
    private lateinit var loadMoreFooter: RelativeLayout
    private lateinit var adapter: NotificationsAdapter
    private lateinit var query: Query
    private var lastDocument: DocumentSnapshot? = null
    private var loading = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        load(true)
    }

    private fun initViews() {
        notifRv.setHasFixedSize(true)
        notifRv.layoutManager = LinearLayoutManager(activity)
        notifRv.addItemDecoration(RecyclerFormatter.SimpleDividerItemDecoration(activity!!))
        adapter = NotificationsAdapter(this)
        notifRv.adapter = adapter

        loadMoreFooter = notifRv.loadMoreFooterView as RelativeLayout
        notifRv.setOnLoadMoreListener {
            if (!loading) {
                loadMoreFooter.showView()
                load(false)
            }
        }

        notifRefresh.isEnabled = false
        notifRefresh.setOnRefreshListener {
            Handler().postDelayed({notifRefresh?.isRefreshing = false}, 2500)
        }

    }

    private fun load(initial: Boolean) {
        query = if (lastDocument == null) {
            getFirestore().collection(Constants.NOTIFICATIONS).document(getUid()).collection(Constants.USER_NOTIFS)
                    .orderBy(Constants.TIME, Query.Direction.DESCENDING)
                    .limit(25)
        } else {
            loading = true

            getFirestore().collection(Constants.NOTIFICATIONS).document(getUid()).collection(Constants.USER_NOTIFS)
                    .orderBy(Constants.TIME, Query.Direction.DESCENDING)
                    .startAfter(lastDocument!!)
                    .limit(25)
        }

        query.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            loading = false

            if (firebaseFirestoreException != null) {
                Timber.e("Error fetching notifications: $firebaseFirestoreException")
            }

            if (querySnapshot == null || querySnapshot.isEmpty) {
                if (initial) noNotifs()

            } else {
                if (initial) hasNotifs()
                lastDocument = querySnapshot.documents[querySnapshot.size()-1]

                for (change in querySnapshot.documentChanges) {

                    when(change.type) {

                        DocumentChange.Type.ADDED -> {
                            val notif = change.document.toObject(Notification::class.java)
                            adapter.addNotification(notif)
                        }

                        DocumentChange.Type.MODIFIED -> {
                            val notif = change.document.toObject(Notification::class.java)
                            adapter.updateNotif(notif)
                        }

                        DocumentChange.Type.REMOVED -> {
                            val notif = change.document.toObject(Notification::class.java)
                            adapter.removeNotif(notif)
                        }

                    }

                }

            }
        }

    }

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

    private fun noNotifs() {
        notifRefresh?.hideView()
        notifsEmptyState?.showView()
    }

    private fun hasNotifs() {
        notifsEmptyState?.hideView()
        notifRefresh?.showView()
    }
}

