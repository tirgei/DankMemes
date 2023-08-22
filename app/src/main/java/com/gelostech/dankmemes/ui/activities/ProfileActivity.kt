package com.gelostech.dankmemes.ui.activities

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.Status
import com.gelostech.dankmemes.data.models.Meme
import com.gelostech.dankmemes.data.models.User
import com.gelostech.dankmemes.data.responses.GenericResponse
import com.gelostech.dankmemes.data.wrappers.ObservableUser
import com.gelostech.dankmemes.databinding.ActivityProfileBinding
import com.gelostech.dankmemes.ui.adapters.ProfileMemesAdapter
import com.gelostech.dankmemes.ui.base.BaseActivity
import com.gelostech.dankmemes.ui.callbacks.ProfileMemesCallback
import com.gelostech.dankmemes.ui.viewmodels.MemesViewModel
import com.gelostech.dankmemes.ui.viewmodels.UsersViewModel
import com.gelostech.dankmemes.utils.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_profile.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class ProfileActivity : BaseActivity() {
    private lateinit var memesAdapter: ProfileMemesAdapter
    private lateinit var binding: ActivityProfileBinding
    private val memesViewModel: MemesViewModel by viewModel()
    private val usersViewModel: UsersViewModel by viewModel()
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile)
        binding.lifecycleOwner = this

        userId = intent.getStringExtra(Constants.USER_ID)!!

        initViews()
        initUserObserver()
        initStatusObserver()
        initResponseObserver()

        usersViewModel.fetchObservableUser(userId)
    }

    private fun initViews() {
        setSupportActionBar(viewProfileToolbar)
        supportActionBar?.apply {
            title = null
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        memesAdapter = ProfileMemesAdapter(memesCallback)

        val gridLayoutManager  = GridLayoutManager(this, 3)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (memesAdapter.getItemViewType(position)) {
                    ProfileMemesAdapter.VIEW_TYPE.PROFILE.ordinal -> 3
                    else -> 1
                }
            }
        }

        viewProfileRv.apply {
            setHasFixedSize(true)
            layoutManager = gridLayoutManager
            addItemDecoration(RecyclerFormatter.GridItemDecoration(this@ProfileActivity, R.dimen.grid_layout_margin))
            itemAnimator = DefaultItemAnimator()
            (itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
            adapter = memesAdapter
        }

        profileRefresh.setOnRefreshListener {
            memesAdapter.currentList?.dataSource?.invalidate()
            runDelayed(2500) { profileRefresh.isRefreshing = false }
        }
    }

    /**
     * Initialize observer for User LiveData
     */
    private fun initUserObserver() {
        usersViewModel.observableUserLiveData.observe(this, Observer {
            when (it.status) {
                Status.LOADING -> {
                    Timber.e("Fetching $userId's profile")
                    loading.showView()
                }

                Status.SUCCESS -> {
                    loading.hideView()
                    if (it.user != null) {
                        initMemesObserver(it.user)
                    }
                    else errorFetchingProfile()
                }

                Status.ERROR -> errorFetchingProfile()
            }
        })
    }

    /**
     * Initialize function to observer Memes LiveData
     */
    private fun initMemesObserver(user: ObservableUser) {
        memesViewModel.fetchMemesByUser(user).observe(this, Observer {
            memesAdapter.submitList(it)
        })
    }

    /**
     * Initialize function to observer Empty State LiveData
     */
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
                    loading.hideView()
                    emptyState.hideView()
                }
            }
        })
    }

        /**
     * Initialize observer for Generic Response LiveData
     */
    private fun initResponseObserver() {
        memesViewModel.genericResponseLiveData.observe(this, Observer {
            when (it.status) {
                Status.LOADING -> { Timber.e("Loading...") }

                Status.SUCCESS -> {
                    when (it.item) {
                        GenericResponse.ITEM_RESPONSE.REPORT_MEME -> toast("Meme reported \uD83D\uDC4A")
                        else -> Timber.e("Success \uD83D\uDE03")
                    }
                }

                Status.ERROR -> { toast("${it.error}. Please try again") }
            }
        })
    }

    private val memesCallback = object : ProfileMemesCallback {
        override fun onMemeClicked(view: View, meme: Meme) {
            val memeId = meme.id!!

            val i = Intent(this@ProfileActivity, MemeActivity::class.java)
            i.putExtra(Constants.MEME_ID, memeId)
            startActivity(i)
            AppUtils.slideRight(this@ProfileActivity)
        }

        override fun onMemeLongClicked(meme: Meme) {
            // Report meme
        }

        override fun onProfileClicked(view: View, user: User) {
            val image = ((view as CircleImageView).drawable as BitmapDrawable).bitmap
            AppUtils.saveTemporaryImage(this@ProfileActivity, image)

            val i = Intent(this@ProfileActivity, ViewMemeActivity::class.java)
            i.putExtra(Constants.PIC_URL, user.userAvatar!!)
            startActivity(i)
            AppUtils.fadeIn(this@ProfileActivity)
        }
    }

    private fun errorFetchingProfile() {
        loading.hideView()
        longToast("Error fetching user profile")
        onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> onBackPressed()
        }

        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        AppUtils.slideLeft(this)
    }

}
