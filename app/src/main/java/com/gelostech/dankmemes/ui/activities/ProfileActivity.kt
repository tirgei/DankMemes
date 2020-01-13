package com.gelostech.dankmemes.ui.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.cocosw.bottomsheet.BottomSheet
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.Status
import com.gelostech.dankmemes.data.models.Meme
import com.gelostech.dankmemes.data.models.Report
import com.gelostech.dankmemes.data.models.User
import com.gelostech.dankmemes.data.responses.GenericResponse
import com.gelostech.dankmemes.data.wrappers.ObservableUser
import com.gelostech.dankmemes.ui.adapters.ProfileMemesAdapter
import com.gelostech.dankmemes.ui.base.BaseActivity
import com.gelostech.dankmemes.ui.callbacks.MemesCallback
import com.gelostech.dankmemes.ui.viewmodels.MemesViewModel
import com.gelostech.dankmemes.ui.viewmodels.UsersViewModel
import com.gelostech.dankmemes.utils.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_profile.*
import org.jetbrains.anko.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class ProfileActivity : BaseActivity() {
    private lateinit var memesAdapter: ProfileMemesAdapter
    private lateinit var bs: BottomSheet.Builder
    private val memesViewModel: MemesViewModel by viewModel()
    private val usersViewModel: UsersViewModel by viewModel()
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        userId = intent.getStringExtra(Constants.USER_ID)!!

        initViews()
        initUserObserver()
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
                        GenericResponse.ITEM_RESPONSE.REPORT_MEME -> toast("Meme reported \uD83D\uDC4A")
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
                R.id.memeIcon, R.id.memeUser -> { Timber.e("Clicked on current profile") }

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
                            R.id.memeImage, R.id.memeMore -> AppUtils.loadBitmapFromUrl(this@ProfileActivity, meme.imageUrl!!)
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
            AppUtils.saveTemporaryImage(this@ProfileActivity, image)

            val i = Intent(this@ProfileActivity, ViewMemeActivity::class.java)
            i.putExtra(Constants.PIC_URL, user.userAvatar!!)
            startActivity(i)
            AppUtils.fadeIn(this@ProfileActivity)
        }
    }

    /**
     * Launch activity to view full meme photo
     */
    private fun showMeme(meme: Meme, image: Bitmap) {
        AppUtils.saveTemporaryImage(this, image)

        val i = Intent(this, ViewMemeActivity::class.java)
        i.putExtra(Constants.PIC_URL, meme.imageUrl)
        i.putExtra(Constants.CAPTION, meme.caption)
        startActivity(i)
        AppUtils.fadeIn(this)
    }

    /**
     * Show BottomSheet with extra actions
     */
    private fun showBottomSheet(meme: Meme, image: Bitmap) {
        bs = when (sessionManager.getAdminStatus()) {
            Constants.ADMIN, Constants.SUPER_ADMIN -> BottomSheet.Builder(this).sheet(R.menu.main_bottomsheet_admin)
            else -> BottomSheet.Builder(this).sheet(R.menu.main_bottomsheet)
        }

        bs.listener { _, which ->
            when(which) {
                R.id.bs_share -> AppUtils.shareImage(this, image)

                R.id.bs_save -> {
                    AppUtils.requestStoragePermission(this) { granted ->
                        if (granted) AppUtils.saveImage(this, image)
                        else longToast("Storage permission is required to save memes")
                    }
                }

                R.id.bs_report -> showReportDialog(meme)
            }

        }.show()

    }

    /**
     * Launch the comments activity
     */
    private fun showComments(memeId: String) {
        val i = Intent(this, CommentActivity::class.java)
        i.putExtra(Constants.MEME_ID, memeId)
        startActivity(i)
        overridePendingTransition(R.anim.enter_b, R.anim.exit_a)
    }

    /**
     * Show dialog for reporting meme
     * @param meme - Meme to report
     */
    private fun showReportDialog(meme: Meme) {
        val editText = EditText(this)
        val layout = FrameLayout(this)
        layout.setPaddingRelative(45,15,45,0)
        layout.addView(editText)

        alert("Please provide a reason for reporting") {
            customView = layout

            positiveButton("Report") {
                if (!AppUtils.validated(editText)) {
                    toast("Please enter a reason to report")
                    return@positiveButton
                }

                val report = Report()
                report.memeId = meme.id
                report.memePosterId = meme.memePosterID
                report.reporterId = getUid()
                report.memeUrl = meme.imageUrl
                report.reason = editText.text.toString().trim()
                report.time = System.currentTimeMillis()

                memesViewModel.reportMeme(report)
            }

            negativeButton("Cancel"){}
        }.show()
    }

    private fun errorFetchingProfile() {
        loading.hideView()
        longToast("Error fetching user profile")
        onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> onBackPressed()
        }

        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.enter_a, R.anim.exit_b)
    }

}
