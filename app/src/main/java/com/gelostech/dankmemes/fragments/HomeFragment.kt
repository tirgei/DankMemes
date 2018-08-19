package com.gelostech.dankmemes.fragments


import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.cocosw.bottomsheet.BottomSheet
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.activities.CommentActivity
import com.gelostech.dankmemes.activities.ProfileActivity
import com.gelostech.dankmemes.activities.ViewMemeActivity
import com.gelostech.dankmemes.adapters.MemesAdapter
import com.gelostech.dankmemes.commoners.BaseFragment
import com.gelostech.dankmemes.commoners.Config
import com.gelostech.dankmemes.commoners.AppUtils
import com.gelostech.dankmemes.models.FaveModel
import com.gelostech.dankmemes.models.MemeModel
import com.gelostech.dankmemes.models.ReportModel
import com.gelostech.dankmemes.utils.RecyclerFormatter
import com.gelostech.dankmemes.utils.showView
import com.google.firebase.database.*
import com.google.firebase.database.Transaction
import com.google.firebase.firestore.*
import com.google.firebase.firestore.Query
import com.mopub.nativeads.MoPubNativeAdPositioning
import com.mopub.nativeads.MoPubRecyclerAdapter
import com.mopub.nativeads.MoPubStaticNativeAdRenderer
import com.mopub.nativeads.ViewBinder
import kotlinx.android.synthetic.main.fragment_home.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast


class HomeFragment : BaseFragment(), MemesAdapter.OnItemClickListener {
    private lateinit var memesAdapter: MemesAdapter
    private lateinit var mopubAdapter: MoPubRecyclerAdapter
    private lateinit var bs: BottomSheet.Builder
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var lastDocument: DocumentSnapshot
    private lateinit var loadMoreFooter: RelativeLayout

    companion object {
        private var TAG = HomeFragment::class.java.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initMopub()
        homeShimmer.startShimmerAnimation()

        loadInitial()
    }

    private fun initViews() {
        layoutManager = LinearLayoutManager(activity)

        homeRv.setHasFixedSize(true)
        homeRv.layoutManager = layoutManager
        homeRv.addItemDecoration(RecyclerFormatter.DoubleDividerItemDecoration(activity!!))
        homeRv.itemAnimator = DefaultItemAnimator()
        (homeRv.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false

        memesAdapter = MemesAdapter(activity!!, this)

        loadMoreFooter = homeRv.loadMoreFooterView as RelativeLayout
        homeRv.setOnLoadMoreListener {
            loadMoreFooter.showView()
            loadMore()
        }

        homeRefresh.isEnabled = false
        homeRefresh.setOnRefreshListener {
            Handler().postDelayed({homeRefresh?.isRefreshing = false}, 2500)
        }

    }

    private fun loadInitial() {
        getFirestore().collection(Config.MEMES)
                .whereEqualTo(Config.POSTER_ID, Config.ADMIN_ID)
                .orderBy(Config.TIME, Query.Direction.DESCENDING)
                .limit(31)
                .addSnapshotListener { p0, p1 ->
                    hasPosts()

                    if (p1 != null) {
                        Log.e(TAG, "Error loading initial memes: $p1")

                    }

                    if (p0 == null || p0.isEmpty) {
                        noPosts()
                    } else {
                        lastDocument = p0.documents[p0.size()-1]

                        for (change: DocumentChange in p0.documentChanges) {

                            when(change.type) {
                                DocumentChange.Type.ADDED -> {
                                    val meme = change.document.toObject(MemeModel::class.java)
                                    memesAdapter.addMeme(meme)
                                }

                                DocumentChange.Type.MODIFIED -> {
                                    val meme = change.document.toObject(MemeModel::class.java)
                                    memesAdapter.updateMeme(meme)
                                }

                                DocumentChange.Type.REMOVED -> {
                                    val meme = change.document.toObject(MemeModel::class.java)
                                    memesAdapter.removeMeme(meme)
                                }


                            }
                        }

                    }

                }

    }

    private fun loadMore() {
        Log.e(TAG, "Loading from ${memesAdapter.getLastKey()}")

        getFirestore().collection(Config.MEMES)
                .whereEqualTo(Config.POSTER_ID, Config.ADMIN_ID)
                .orderBy(Config.TIME, Query.Direction.DESCENDING)
                .startAfter(lastDocument)
                .limit(21)
                .addSnapshotListener { p0, p1 ->
                    //hasPosts()

                    if (p1 != null) {
                        Log.e(TAG, "Error loading more memes: $p1")

                    }

                    if (p0 == null || p0.isEmpty) {
                        Log.e(TAG, "No more memes")
                    } else {
                        lastDocument = p0.documents[p0.size()-1]

                        for (change: DocumentChange in p0.documentChanges) {

                            when(change.type) {
                                DocumentChange.Type.ADDED -> {
                                    val meme = change.document.toObject(MemeModel::class.java)
                                    memesAdapter.addMeme(meme)
                                }

                                DocumentChange.Type.MODIFIED -> {
                                    val meme = change.document.toObject(MemeModel::class.java)
                                    memesAdapter.updateMeme(meme)
                                }

                                DocumentChange.Type.REMOVED -> {
                                    val meme = change.document.toObject(MemeModel::class.java)
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
        homeRv.adapter = mopubAdapter
        if (isConnected()) mopubAdapter.loadAds(activity!!.getString(R.string.ad_unit_id_native))

    }

    override fun onItemClick(meme: MemeModel, viewID: Int, image: Bitmap?) {
        when(viewID) {
            0 -> likePost(meme.id!!)
            1 -> showBottomSheet(meme, image!!)
            2 -> favePost(meme.id!!)
            3 -> showComments(meme)
            4 -> showMeme(meme, image!!)
            5 -> showProfile(meme)

        }
    }

    private fun showProfile(meme: MemeModel) {
        if (meme.memePosterID != getUid()) {
            val i = Intent(activity, ProfileActivity::class.java)
            i.putExtra("userId", meme.memePosterID)
            startActivity(i)
            activity?.overridePendingTransition(R.anim.enter_b, R.anim.exit_a)
        }
    }

    private fun showMeme(meme: MemeModel, image: Bitmap) {
        AppUtils.saveTemporaryImage(activity!!, image)

        val i = Intent(activity, ViewMemeActivity::class.java)
        i.putExtra(Config.PIC_URL, meme.imageUrl)
        i.putExtra("caption", meme.caption)
        startActivity(i)
        AppUtils.fadeIn(activity!!)
    }

    private fun showBottomSheet(meme: MemeModel, image: Bitmap) {
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

    private fun showComments(meme: MemeModel) {
        val i = Intent(activity, CommentActivity::class.java)
        i.putExtra("memeId", meme.id)
        startActivity(i)
        activity?.overridePendingTransition(R.anim.enter_b, R.anim.exit_a)
    }

    private fun deletePost(meme: MemeModel) {
        val dbRef = getDatabaseReference().child("dank-memes").child(meme.id!!)

        activity!!.alert("Delete this meme?") {
            positiveButton("DELETE") {
                dbRef.removeValue()
            }
            negativeButton("CANCEL"){}
        }.show()
    }

    private fun likePost(id: String) {
        getDatabaseReference().child("dank-memes").child(id).runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val meme = mutableData.getValue<MemeModel>(MemeModel::class.java)

                if (meme!!.likes.containsKey(getUid())) {
                    meme.likesCount = meme.likesCount!! - 1
                    meme.likes.remove(getUid())

                } else  {
                    meme.likesCount = meme.likesCount!! + 1
                    meme.likes[getUid()] = true
                }

                mutableData.value = meme
                return Transaction.success(mutableData)
            }

            override fun onComplete(databaseError: DatabaseError?, b: Boolean, dataSnapshot: DataSnapshot?) {

                Log.d(javaClass.simpleName, "postTransaction:onComplete: $databaseError")
            }
        })
    }

    private fun favePost(id: String) {
        getDatabaseReference().child("dank-memes").child(id).runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val meme = mutableData.getValue<MemeModel>(MemeModel::class.java)

                if (meme!!.faves.containsKey(getUid())) {
                    meme.faves.remove(getUid())

                    getDatabaseReference().child("favorites").child(getUid()).child(meme.id!!).removeValue()

                } else  {
                    meme.faves[getUid()] = true

                    val fave = FaveModel()
                    fave.faveKey = meme.id!!
                    fave.commentId = meme.id!!
                    fave.picUrl = meme.imageUrl!!

                    getDatabaseReference().child("favorites").child(getUid()).child(meme.id!!).setValue(fave)
                }

                mutableData.value = meme
                return Transaction.success(mutableData)
            }

            override fun onComplete(databaseError: DatabaseError?, b: Boolean, dataSnapshot: DataSnapshot?) {

                Log.d(javaClass.simpleName, "postTransaction:onComplete: $databaseError")
            }
        })
    }

    private fun showReportDialog(meme: MemeModel) {
        val editText = EditText(activity)
        val layout = FrameLayout(activity)
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

                val report = ReportModel()
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
        return homeRv
    }

    private fun hasPosts() {
        homeShimmer.stopShimmerAnimation()
        homeShimmer.visibility = View.GONE
    }

    private fun noPosts() {

    }

    override fun onDestroy() {
        mopubAdapter.destroy()
        super.onDestroy()
    }

}
