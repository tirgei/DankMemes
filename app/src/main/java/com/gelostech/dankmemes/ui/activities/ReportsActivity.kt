package com.gelostech.dankmemes.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.Status
import com.gelostech.dankmemes.data.models.Report
import com.gelostech.dankmemes.databinding.ActivityReportsBinding
import com.gelostech.dankmemes.ui.adapters.ReportsAdapter
import com.gelostech.dankmemes.ui.base.BaseActivity
import com.gelostech.dankmemes.ui.callbacks.ReportsCallback
import com.gelostech.dankmemes.ui.viewmodels.ReportsViewModel
import com.gelostech.dankmemes.ui.viewmodels.UsersViewModel
import com.gelostech.dankmemes.utils.*
import kotlinx.android.synthetic.main.activity_reports.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import org.koin.android.ext.android.inject

class ReportsActivity : BaseActivity() {
    private lateinit var reportsAdapter: ReportsAdapter
    private val reportsViewModel: ReportsViewModel by inject()
    private val usersViewModel: UsersViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityReportsBinding = DataBindingUtil.setContentView(this, R.layout.activity_reports)
        binding.lifecycleOwner = this

        initViews()
        initStatusObserver()
        initReportsObserver()
        initResponseObserver()
    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null

        reportsAdapter = ReportsAdapter(callback)
        rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@ReportsActivity)
            itemAnimator = null
            addItemDecoration(RecyclerFormatter.DoubleDividerItemDecoration(this@ReportsActivity))
            adapter = reportsAdapter
        }

        refresh.setOnRefreshListener {
            reportsAdapter.currentList?.dataSource?.invalidate()
            runDelayed(2500) { refresh.isRefreshing = false }
        }
    }

    private fun initStatusObserver() {
        reportsViewModel.showStatusLiveData.observe(this, Observer {
            when (it) {
                Status.LOADING -> {
                    emptyState.hideView()
                    loading.showView()
                }

                Status.ERROR -> {
                    loading.hideView()
                    emptyState.showView()
                }

                else -> {
                    hideViews(loading, emptyState)
                }
            }
        })
    }

    private fun initReportsObserver() {
        reportsViewModel.fetchReports().observe(this, Observer {
            reportsAdapter.submitList(it)
        })
    }

    private fun initResponseObserver() {
        usersViewModel.genericResponseLiveData.observe(this, Observer {
            when(it.status) {
                Status.LOADING -> showLoading("Muting user...")

                Status.SUCCESS -> {
                    hideLoading()
                    longToast("${it.value} muted successfully")
                }

                Status.ERROR -> {
                    hideLoading()
                    toast(it.error.toString())
                }
            }
        })
    }

    private val callback = object : ReportsCallback {
        override fun onReportClicked(report: Report) {
            usersViewModel.muteUser(report.memePosterId!!)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        AppUtils.slideLeft(this)
    }
}
