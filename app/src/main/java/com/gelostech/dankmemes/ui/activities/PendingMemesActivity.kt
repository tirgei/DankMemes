package com.gelostech.dankmemes.ui.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.Status
import com.gelostech.dankmemes.data.models.Meme
import com.gelostech.dankmemes.data.models.PendingMeme
import com.gelostech.dankmemes.data.responses.GenericResponse
import com.gelostech.dankmemes.databinding.ActivityPendingMemesBinding
import com.gelostech.dankmemes.ui.adapters.PendingMemesAdapter
import com.gelostech.dankmemes.ui.base.BaseActivity
import com.gelostech.dankmemes.ui.callbacks.PendingMemesCallback
import com.gelostech.dankmemes.ui.viewmodels.MemesViewModel
import com.gelostech.dankmemes.utils.*
import kotlinx.android.synthetic.main.activity_pending_memes.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import org.koin.android.ext.android.inject
import timber.log.Timber

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
        initResponseObserver()
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

        refresh.setOnRefreshListener {
            refreshList()
            runDelayed(2500) { refresh.isRefreshing = false }
        }
    }

    private val callback = object : PendingMemesCallback {
        override fun onPendingMemeClicked(view: View, meme: PendingMeme) {
            when (view.id) {
                R.id.post -> {
                    postMeme(meme)
                }

                R.id.delete -> {
                    alert("Delete this meme?") {
                        title = "Delete Meme"
                        positiveButton("Delete") {
                            Timber.e("Deleting: ${meme.id}")
                            memesViewModel.deletePendingMeme(meme.id!!)
                        }
                        negativeButton("Cancel") {}
                    }.show()
                }
            }
        }
    }

    private fun postMeme(pendingMeme: PendingMeme) {
        if (!Connectivity.isConnected(this)) {
            toast("Please turn on your internet connection")
            return
        }

        // Create new meme object
        val meme = Meme()
        meme.likesCount = 0
        meme.commentsCount = 0
        meme.imageUrl = pendingMeme.link
        meme.thumbnail = pendingMeme.link
        meme.memePoster = sessionManager.getUsername()
        meme.memePosterAvatar = sessionManager.getUserAvatar()
        meme.memePosterID = sessionManager.getUserId()
        meme.time = System.currentTimeMillis()

        memesViewModel.postPendingMeme(pendingMeme.id!!, meme)
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

    /**
     * Initialize observer for Meme LiveData
     */
    private fun initResponseObserver() {
        memesViewModel.genericResponseLiveData.observe(this, Observer {
            when (it.status) {
                Status.LOADING -> {
                    showLoading("Processing meme...")
                }

                Status.SUCCESS -> {
                    hideLoading()

                    when (it.item) {
                        GenericResponse.ITEM_RESPONSE.POST_MEME -> {
                            refreshList()
                            sessionManager.hasNewContent(true)
                            toast("Meme posted \uD83E\uDD2A\uD83E\uDD2A")
                        }

                        GenericResponse.ITEM_RESPONSE.DELETE_MEME -> {
                            refreshList()
                            toast("Meme deleted \uD83D\uDEAE️")
                        }

                        else -> Timber.e("WTF just happened?")
                    }

                }

                Status.ERROR -> {
                    hideLoading()
                    toast(it.error!!)
                }
            }
        })
    }

    private fun refreshList() {
        memesAdapter.currentList?.dataSource?.invalidate()
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
