package com.gelostech.dankmemes.ui.fragments


import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cocosw.bottomsheet.BottomSheet
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.models.Fave
import com.gelostech.dankmemes.data.models.Meme
import com.gelostech.dankmemes.data.models.Report
import com.gelostech.dankmemes.ui.activities.CommentActivity
import com.gelostech.dankmemes.ui.activities.ProfileActivity
import com.gelostech.dankmemes.ui.activities.ViewMemeActivity
import com.gelostech.dankmemes.ui.adapters.MemesAdapter
import com.gelostech.dankmemes.ui.adapters.PagedMemesAdapter
import com.gelostech.dankmemes.ui.base.BaseFragment
import com.gelostech.dankmemes.ui.callbacks.MemesCallback
import com.gelostech.dankmemes.ui.viewmodels.MemesViewModel
import com.gelostech.dankmemes.utils.AppUtils
import com.gelostech.dankmemes.utils.Constants
import com.gelostech.dankmemes.utils.RecyclerFormatter
import com.gelostech.dankmemes.utils.showView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.mopub.nativeads.MoPubNativeAdPositioning
import com.mopub.nativeads.MoPubRecyclerAdapter
import com.mopub.nativeads.MoPubStaticNativeAdRenderer
import com.mopub.nativeads.ViewBinder
import kotlinx.android.synthetic.main.fragment_all.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import org.koin.android.ext.android.inject
import timber.log.Timber

class AllFragment : BaseFragment() {
    private lateinit var memesAdapter: MemesAdapter
    private lateinit var pagedMemesAdapter: PagedMemesAdapter
    private lateinit var mopubAdapter: MoPubRecyclerAdapter
    private lateinit var bs: BottomSheet.Builder
    private lateinit var loadMoreFooter: RelativeLayout
    private lateinit var query: Query
    private var lastDocument: DocumentSnapshot? = null
    private var loading = false
    private val memesViewModel: MemesViewModel by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_all, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initMopub()
        allShimmer.startShimmerAnimation()

//        load()
        initMemesObserver()
    }

    private fun initViews() {
        memesAdapter = MemesAdapter(memesCallback)
        pagedMemesAdapter = PagedMemesAdapter(memesCallback)

        allRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            addItemDecoration(RecyclerFormatter.DoubleDividerItemDecoration(activity!!))
            itemAnimator = DefaultItemAnimator()
            (itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
            loadMoreFooterView as RelativeLayout
            adapter = pagedMemesAdapter
        }

        allRv.setOnLoadMoreListener {
            if (!loading) {
                loadMoreFooter.showView()
                load()
            }
        }

        allRefresh.isEnabled = false
        allRefresh.setOnRefreshListener {
            Handler().postDelayed({allRefresh?.isRefreshing = false}, 2500)
        }

    }

    /**
     * Initialize observer for Memes LiveData
     */
    private fun initMemesObserver() {
        memesViewModel.memesLiveData.observe(this, Observer {
            if (it.isEmpty())
                toast("Empty memes")
            else
                pagedMemesAdapter.submitList(it)
        })
    }

    private fun load() {
        query = if (lastDocument == null) {
            getFirestore().collection(Constants.MEMES)
                    .orderBy(Constants.TIME, Query.Direction.DESCENDING)
                    .limit(31)
        } else {
            loading = true

            getFirestore().collection(Constants.MEMES)
                    .orderBy(Constants.TIME, Query.Direction.DESCENDING)
                    .startAfter(lastDocument!!)
                    .limit(21)
        }

        query.addSnapshotListener { p0, p1 ->
            hasPosts()
            loading = false

            if (p1 != null) {
                Timber.e("Error loading initial memes: $p1")

            }

            if (p0 == null || p0.isEmpty) {
                noPosts()
            } else {
                lastDocument = p0.documents[p0.size()-1]

                for (change: DocumentChange in p0.documentChanges) {

                    when(change.type) {
                        DocumentChange.Type.ADDED -> {
                            val meme = change.document.toObject(Meme::class.java)
                            memesAdapter.addMeme(meme)
                        }

                        DocumentChange.Type.MODIFIED -> {
                            val meme = change.document.toObject(Meme::class.java)
                            memesAdapter.updateMeme(meme)
                        }

                        DocumentChange.Type.REMOVED -> {
                            val meme = change.document.toObject(Meme::class.java)
                            memesAdapter.removeMeme(meme)
                        }


                    }
                }

            }

        }
    }

    private fun initMopub() {
        val adPositioning = MoPubNativeAdPositioning.MoPubServerPositioning()
        mopubAdapter = MoPubRecyclerAdapter(activity!!, memesAdapter, adPositioning)
        mopubAdapter.setContentChangeStrategy(MoPubRecyclerAdapter.ContentChangeStrategy.MOVE_ALL_ADS_WITH_CONTENT)

        val myViewBinder = ViewBinder.Builder(R.layout.item_native)
                .titleId(R.id.native_title)
                .textId(R.id.native_text)
                .mainImageId(R.id.native_main_image)
                .iconImageId(R.id.native_icon_image)
                .callToActionId(R.id.native_cta)
                .privacyInformationIconImageId(R.id.native_privacy_information_icon_image)
                .build()

        val myRenderer = MoPubStaticNativeAdRenderer(myViewBinder)
        mopubAdapter.registerAdRenderer(myRenderer)
        allRv.adapter = mopubAdapter
        if (isConnected()) mopubAdapter.loadAds(activity!!.getString(R.string.mopub_native_ad))

    }

    private val memesCallback = object : MemesCallback {
        override fun onMemeClicked(view: View, meme: Meme) {
            val memeId = meme.id!!

            when(view.id) {
                R.id.memeComment -> showComments(memeId)
                R.id.memeIcon, R.id.memeUser -> showProfile(memeId)
                R.id.memeFave -> favePost(memeId)

                R.id.memeLike -> {
                    animateView(view)
                    likePost(memeId)
                }

                else -> {
                    doAsync {
                        // Get bitmap of shown meme
                        val imageBitmap = when(view.id) {
                            R.id.memeImage, R.id.memeMore -> AppUtils.loadBitmapFromUrl(activity!!, meme.imageUrl!!)
                            else -> null
                        }

                        uiThread {
                            if (view.id == R.id.memeMore) showBottomsheetAdmin(meme, imageBitmap!!)
                            else showMeme(meme, imageBitmap!!)
                        }
                    }
                }
            }
        }
    }

    private fun showProfile(userId: String) {
        if (userId != getUid()) {
            val i = Intent(activity, ProfileActivity::class.java)
            i.putExtra("userId", userId)
            startActivity(i)
            activity?.overridePendingTransition(R.anim.enter_b, R.anim.exit_a)
        }
    }

    private fun showMeme(meme: Meme, image: Bitmap) {
        AppUtils.saveTemporaryImage(activity!!, image)

        val i = Intent(activity, ViewMemeActivity::class.java)
        i.putExtra(Constants.PIC_URL, meme.imageUrl)
        i.putExtra("caption", meme.caption)
        startActivity(i)
        AppUtils.fadeIn(activity!!)
    }

    private fun showBottomSheet(meme: Meme, image: Bitmap) {
        bs = if (getUid() != meme.memePosterID) {
            BottomSheet.Builder(activity!!).sheet(R.menu.main_bottomsheet)
        } else {
            BottomSheet.Builder(activity!!).sheet(R.menu.main_bottomsheet_me)
        }

        bs.listener { _, which ->

            when(which) {
                R.id.bs_share -> AppUtils.shareImage(activity!!, image)
                R.id.bs_delete -> deletePost(meme)
                R.id.bs_save -> {
                    if (storagePermissionGranted()) {
                        AppUtils.saveImage(activity!!, image)
                    } else requestStoragePermission()
                }
                R.id.bs_report -> showReportDialog(meme)
            }

        }.show()

    }

    private fun showBottomsheetAdmin(meme: Meme, image: Bitmap) {
        bs = BottomSheet.Builder(activity!!).sheet(R.menu.main_bottomsheet_admin)

        bs.listener { _, which ->

            when(which) {
                R.id.bs_share -> AppUtils.shareImage(activity!!, image)
                R.id.bs_delete -> deletePost(meme)
                R.id.bs_save -> {
                    if (storagePermissionGranted()) {
                        AppUtils.saveImage(activity!!, image)
                    } else requestStoragePermission()
                }
                R.id.bs_report -> showReportDialog(meme)
            }

        }.show()
    }

    private fun showComments(memeId: String) {
        val i = Intent(activity, CommentActivity::class.java)
        i.putExtra("memeId", memeId)
        startActivity(i)
        activity?.overridePendingTransition(R.anim.enter_b, R.anim.exit_a)
    }

    private fun deletePost(meme: Meme) {
        activity!!.alert("Delete this meme?") {
            positiveButton("DELETE") {
                getFirestore().collection(Constants.MEMES).document(meme.id!!).delete()
            }
            negativeButton("CANCEL"){}
        }.show()
    }

    private fun likePost(id: String) {
        val docRef = getFirestore().collection(Constants.MEMES).document(id)

        getFirestore().runTransaction {

            val meme =  it[docRef].toObject(Meme::class.java)
            val likes = meme!!.likes
            var likesCount = meme.likesCount

            if (likes.containsKey(getUid())) {
                likesCount -= 1
                likes.remove(getUid())

            } else  {
                likesCount += 1
                likes[getUid()] = true
            }

            it.update(docRef, Constants.LIKES, likes)
            it.update(docRef, Constants.LIKES_COUNT, likesCount)

            return@runTransaction null
        }.addOnSuccessListener {
            Timber.e("Meme liked")
        }.addOnFailureListener {
            Timber.e("Error liking meme")
        }
    }

    private fun favePost(id: String) {
        val docRef = getFirestore().collection(Constants.MEMES).document(id)

        getFirestore().runTransaction {

            val meme =  it[docRef].toObject(Meme::class.java)
            val faves = meme!!.faves

            if (faves.containsKey(getUid())) {
                faves.remove(getUid())

                getFirestore().collection(Constants.FAVORITES).document(getUid()).collection(Constants.USER_FAVES).document(meme.id!!).delete()
            } else  {
                faves[getUid()] = true

                val fave = Fave()
                fave.id = meme.id!!
                fave.imageUrl = meme.imageUrl!!
                fave.time = meme.time!!

                getFirestore().collection(Constants.FAVORITES).document(getUid()).collection(Constants.USER_FAVES).document(meme.id!!).set(fave)
            }

            it.update(docRef, Constants.FAVES, faves)

            return@runTransaction null
        }.addOnSuccessListener {
            Timber.e("Meme faved")
        }.addOnFailureListener {
            Timber.e("Error faving meme")
        }

    }

    private fun showReportDialog(meme: Meme) {
        val editText = EditText(activity)
        val layout = FrameLayout(activity!!)
        layout.setPaddingRelative(45,15,45,0)
        layout.addView(editText)

        activity!!.alert("Please provide a reason for reporting") {
            customView = layout

            positiveButton("REPORT") {
                if (!AppUtils.validated(editText)) {
                    activity!!.toast("Please enter a reason to report")
                    return@positiveButton
                }

                val key = getDatabaseReference().child("reports").push().key
                val reason = editText.text.toString().trim()

                val report = Report()
                report.id = key
                report.memeId = meme.id
                report.memePosterId = meme.memePosterID
                report.reporterId = getUid()
                report.memeUrl = meme.imageUrl
                report.reason = reason
                report.time = System.currentTimeMillis()

                getDatabaseReference().child("reports").child(key!!).setValue(report).addOnCompleteListener {
                    activity!!.toast("Meme reported!")
                }

            }

            negativeButton("CANCEL"){}
        }.show()
    }

    fun getRecyclerView(): RecyclerView {
        return allRv
    }

    private fun hasPosts() {
        allShimmer?.stopShimmerAnimation()
        allShimmer?.visibility = View.GONE
    }

    private fun noPosts() {

    }

    override fun onDestroy() {
        mopubAdapter.destroy()
        super.onDestroy()
    }

}

