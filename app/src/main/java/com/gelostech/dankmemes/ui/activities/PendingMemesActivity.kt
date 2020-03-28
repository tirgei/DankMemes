package com.gelostech.dankmemes.ui.activities

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.Status
import com.gelostech.dankmemes.data.models.PendingMeme
import com.gelostech.dankmemes.databinding.ActivityPendingMemesBinding
import com.gelostech.dankmemes.ui.adapters.PendingMemesAdapter
import com.gelostech.dankmemes.ui.base.BaseActivity
import com.gelostech.dankmemes.ui.callbacks.PendingMemesCallback
import com.gelostech.dankmemes.ui.viewmodels.MemesViewModel
import com.gelostech.dankmemes.utils.*
import kotlinx.android.synthetic.main.activity_pending_memes.*
import org.koin.android.ext.android.inject

class PendingMemesActivity : BaseActivity() {
    private val memesViewModel: MemesViewModel by inject()
    private lateinit var memesAdapter: PendingMemesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityPendingMemesBinding = DataBindingUtil.setContentView(this, R.layout.activity_pending_memes)
        binding.lifecycleOwner = this

        initViews()
        initStatusObserver()
        initMemesObserver()
    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null

        memesAdapter = PendingMemesAdapter(callback)
        rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@PendingMemesActivity)
            itemAnimator = null
            addItemDecoration(RecyclerFormatter.DoubleDividerItemDecoration(this@PendingMemesActivity))
            adapter = memesAdapter
        }
    }

    private val callback = object : PendingMemesCallback {
        override fun onPendingMemeClicked(view: View, meme: PendingMeme) {

        }
    }

    private fun initStatusObserver() {
        memesViewModel.showStatusLiveData.observe(this, Observer {
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

    private fun initMemesObserver() {
        memesViewModel.fetchPendingMemes().observe(this, Observer {
            memesAdapter.submitList(it as PagedList<PendingMeme>)
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        AppUtils.slideLeft(this)
    }
}
