package com.gelostech.dankmemes.ui.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.Status
import com.gelostech.dankmemes.databinding.ActivityAddMemesBinding
import com.gelostech.dankmemes.ui.base.BaseActivity
import com.gelostech.dankmemes.ui.viewmodels.MemesViewModel
import com.gelostech.dankmemes.utils.AppUtils
import com.gelostech.dankmemes.utils.hideView
import com.gelostech.dankmemes.utils.showView
import com.mikepenz.ionicons_typeface_library.Ionicons
import kotlinx.android.synthetic.main.activity_add_memes.*
import org.koin.android.ext.android.inject
import timber.log.Timber

class AddMemesActivity : BaseActivity() {
    private val memesViewModel: MemesViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityAddMemesBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_memes)
        binding.lifecycleOwner = this

        initViews()
        initMemesObserver()

        fetchMemes()
    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null
    }

    private fun initMemesObserver() {
        memesViewModel.rssMemesLiveData.observe(this, Observer {
            when (it.status) {
                Status.LOADING -> {
                    loading.showView()
                    emptyState.hideView()
                }

                Status.SUCCESS -> {
                    loading.hideView()
                    Timber.e("Fetched: ${it.data?.size}")
                }

                Status.ERROR -> {
                    loading.hideView()
                    emptyState.showView()
                }
            }
        })
    }

    private fun fetchMemes(source: String = "FunnyNoGif") {
        memesViewModel.fetchRssMemes(source)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar_add_memes, menu)

        val sourceMenu = menu?.findItem(R.id.menu_post)
        sourceMenu?.icon = AppUtils.getDrawable(this, Ionicons.Icon.ion_ios_color_filter, R.color.color_secondary, 20)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> onBackPressed()

            R.id.menu_add_memes -> {

            }

            else -> Timber.e("Invalid option")
        }

        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        AppUtils.slideLeft(this)
    }
}
