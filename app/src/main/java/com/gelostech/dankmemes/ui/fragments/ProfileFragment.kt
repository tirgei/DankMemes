package com.gelostech.dankmemes.ui.fragments


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.cocosw.bottomsheet.BottomSheet
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.Status
import com.gelostech.dankmemes.data.models.Meme
import com.gelostech.dankmemes.data.models.User
import com.gelostech.dankmemes.data.responses.GenericResponse
import com.gelostech.dankmemes.data.wrappers.ObservableUser
import com.gelostech.dankmemes.ui.activities.CommentActivity
import com.gelostech.dankmemes.ui.activities.ViewMemeActivity
import com.gelostech.dankmemes.ui.adapters.MemesAdapter
import com.gelostech.dankmemes.ui.base.BaseFragment
import com.gelostech.dankmemes.ui.callbacks.MemesCallback
import com.gelostech.dankmemes.ui.viewmodels.MemesViewModel
import com.gelostech.dankmemes.ui.viewmodels.UsersViewModel
import com.gelostech.dankmemes.utils.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.loading
import org.jetbrains.anko.alert
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.longToast
import org.jetbrains.anko.uiThread
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class ProfileFragment : BaseFragment() {
    private lateinit var memesAdapter: MemesAdapter
    private lateinit var bs: BottomSheet.Builder
    private val memesViewModel: MemesViewModel by viewModel()
    private val usersViewModel: UsersViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initUserObserver()
        initResponseObserver()

        usersViewModel.fetchObservableUser(sessionManager.getUserId())
    }

    private fun initViews() {
        memesAdapter = MemesAdapter(memesCallback)

        profileRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            addItemDecoration(RecyclerFormatter.DoubleDividerItemDecoration(activity!!))
            itemAnimator = DefaultItemAnimator()
            (itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
            adapter = memesAdapter
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
                    if (it.user != null) initMemesObserver(it.user)
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

    private val memesCallback = object : MemesCallback {
        override fun onMemeClicked(view: View, meme: Meme) {
            val memeId = meme.id!!

            when(view.id) {
                R.id.memeComment -> showComments(memeId)
                R.id.memeIcon, R.id.memeUser -> { Timber.e("Clicked on my profile") }

                R.id.memeFave -> {
                    AppUtils.animateView(view)
                    memesViewModel.faveMeme(memeId, getUid())
                }

                R.id.memeLike -> {
                    AppUtils.animateView(view)
                    memesViewModel.likeMeme(memeId, getUid())
                }

                else -> {
                    doAsync {
                        // Get bitmap of shown meme
                        val imageBitmap = when(view.id) {
                            R.id.memeImage, R.id.memeMore -> AppUtils.loadBitmapFromUrl(activity!!, meme.imageUrl!!)
                            else -> null
                        }

                        uiThread {
                            imageBitmap?.let {
                                if (view.id == R.id.memeMore) showBottomSheet(meme, imageBitmap)
                                else showMeme(meme, imageBitmap)
                            }
                        }
                    }
                }
            }
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

    /**
     * Launch activity to view full meme photo
     */
    private fun showMeme(meme: Meme, image: Bitmap) {
        AppUtils.saveTemporaryImage(activity!!, image)

        val i = Intent(activity, ViewMemeActivity::class.java)
        i.putExtra(Constants.PIC_URL, meme.imageUrl)
        i.putExtra(Constants.CAPTION, meme.caption)
        startActivity(i)
        AppUtils.fadeIn(activity!!)
    }

    /**
     * Show BottomSheet with extra actions
     */
    private fun showBottomSheet(meme: Meme, image: Bitmap) {
        bs = BottomSheet.Builder(activity!!).sheet(R.menu.main_bottomsheet_me)

        bs.listener { _, which ->
            when(which) {
                R.id.bs_share -> AppUtils.shareImage(activity!!, image)

                R.id.bs_delete -> {
                    activity!!.alert("Delete this meme?") {
                        title = "Delete Meme"
                        positiveButton("Delete") { memesViewModel.deleteMeme(meme.id!!) }
                        negativeButton("Cancel") {}
                    }.show()
                }

                R.id.bs_save -> {
                    AppUtils.requestStoragePermission(activity!!) { granted ->
                        if (granted) AppUtils.saveImage(activity!!, image)
                        else longToast("Storage permission is required to save memes")
                    }
                }
            }

        }.show()

    }

    /**
     * Launch the comments activity
     */
    private fun showComments(memeId: String) {
        val i = Intent(activity, CommentActivity::class.java)
        i.putExtra(Constants.MEME_ID, memeId)
        startActivity(i)
        activity?.overridePendingTransition(R.anim.enter_b, R.anim.exit_a)
    }

    private fun errorFetchingProfile() {
        loading.hideView()
        viewProfileEmptyState.showView()
    }

}
