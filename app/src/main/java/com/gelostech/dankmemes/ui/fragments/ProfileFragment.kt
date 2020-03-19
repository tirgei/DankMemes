package com.gelostech.dankmemes.ui.fragments

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.Status
import com.gelostech.dankmemes.data.models.Meme
import com.gelostech.dankmemes.data.models.User
import com.gelostech.dankmemes.data.responses.GenericResponse
import com.gelostech.dankmemes.data.wrappers.ObservableUser
import com.gelostech.dankmemes.databinding.FragmentProfileBinding
import com.gelostech.dankmemes.ui.activities.MemeActivity
import com.gelostech.dankmemes.ui.activities.ViewMemeActivity
import com.gelostech.dankmemes.ui.adapters.ProfileMemesAdapter
import com.gelostech.dankmemes.ui.base.BaseFragment
import com.gelostech.dankmemes.ui.callbacks.ProfileMemesCallback
import com.gelostech.dankmemes.ui.viewmodels.MemesViewModel
import com.gelostech.dankmemes.ui.viewmodels.UsersViewModel
import com.gelostech.dankmemes.utils.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.loading
import org.jetbrains.anko.alert
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class ProfileFragment : BaseFragment() {
    private lateinit var memesAdapter: ProfileMemesAdapter
    private val memesViewModel: MemesViewModel by viewModel()
    private val usersViewModel: UsersViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val binding = DataBindingUtil.inflate<FragmentProfileBinding>(inflater, R.layout.fragment_profile, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initUserObserver()
        initResponseObserver()

        usersViewModel.fetchObservableUser(sessionManager.getUserId())
    }

    private fun initViews() {
        memesAdapter = ProfileMemesAdapter(memesCallback, true)

        val gridLayoutManager  = GridLayoutManager(activity, 3)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (memesAdapter.getItemViewType(position)) {
                    ProfileMemesAdapter.VIEW_TYPE.PROFILE.ordinal -> 3
                    else -> 1
                }
            }
        }

        profileRv.apply {
            setHasFixedSize(true)
            layoutManager = gridLayoutManager
            addItemDecoration(RecyclerFormatter.GridItemDecoration(activity!!, R.dimen.grid_layout_margin))
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
                    Timber.e("Fetching my profile")
                    loading.showView()
                }

                Status.SUCCESS -> {
                    loading.hideView()
                    if (it.user != null) {
                        initStatusObserver()
                        initMemesObserver(it.user)
                    }
                    else errorFetchingProfile()
                }

                Status.ERROR -> errorFetchingProfile()
            }
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
     * Initialize function to observer Memes LiveData
     */
    private fun initMemesObserver(user: ObservableUser) {
        memesViewModel.fetchMemesByUser(user).observe(this, Observer {
            memesAdapter.submitList(it)
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
                        GenericResponse.ITEM_RESPONSE.DELETE_MEME -> toast("Meme deleted \uD83D\uDEAE️")
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

            val i = Intent(activity, MemeActivity::class.java)
            i.putExtra(Constants.MEME_ID, memeId)
            startActivity(i)
            AppUtils.animateEnterRight(activity!!)
        }

        override fun onMemeLongClicked(meme: Meme) {
            activity!!.alert("Delete this meme?") {
                title = "Delete Meme"
                positiveButton("Delete") { memesViewModel.deleteMeme(meme.id!!) }
                negativeButton("Cancel") {}
            }.show()
        }

        override fun onProfileClicked(view: View, user: User) {
            val image = ((view as CircleImageView).drawable as BitmapDrawable).bitmap
            AppUtils.saveTemporaryImage(activity!!, image)

            val i = Intent(activity, ViewMemeActivity::class.java)
            i.putExtra(Constants.PIC_URL, user.userAvatar!!)
            startActivity(i)
            AppUtils.fadeIn(activity!!)
        }
    }

    private fun errorFetchingProfile() {
        loading.hideView()
//        profileEmptyState.showView()
    }

}
