package com.gelostech.dankmemes.fragments


import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cocosw.bottomsheet.BottomSheet

import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.activities.CommentActivity
import com.gelostech.dankmemes.activities.ProfileActivity
import com.gelostech.dankmemes.activities.ViewMemeActivity
import com.gelostech.dankmemes.adapters.MemesAdapter
import com.gelostech.dankmemes.commoners.BaseFragment
import com.gelostech.dankmemes.commoners.DankMemesUtil
import com.gelostech.dankmemes.models.MemeModel
import com.gelostech.dankmemes.utils.RecyclerFormatter
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_home.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import android.support.v7.widget.RecyclerView
import android.widget.EditText
import android.widget.FrameLayout
import com.gelostech.dankmemes.models.FaveModel
import com.gelostech.dankmemes.models.ReportModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Transaction
import com.google.firebase.database.MutableData
import com.mopub.nativeads.MoPubNativeAdPositioning
import com.mopub.nativeads.MoPubRecyclerAdapter
import com.mopub.nativeads.MoPubStaticNativeAdRenderer
import com.mopub.nativeads.ViewBinder


class HomeFragment : BaseFragment(), MemesAdapter.OnItemClickListener {
    private lateinit var memesAdapter: MemesAdapter
    private lateinit var mopubAdapter: MoPubRecyclerAdapter
    private lateinit var bs: BottomSheet.Builder
    private lateinit var memesQuery: Query
    private lateinit var bottomNavigationStateListener: HomeBottomNavigationStateListener

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

        memesQuery = getDatabaseReference().child("dank-memes")
        memesQuery.addValueEventListener(memesValueListener)
        memesQuery.addChildEventListener(memesChildListener)
    }

    private fun initViews() {
        homeRv.setHasFixedSize(true)
        homeRv.layoutManager = LinearLayoutManager(activity)
        homeRv.addItemDecoration(RecyclerFormatter.DoubleDividerItemDecoration(activity!!))
        homeRv.itemAnimator = DefaultItemAnimator()
        (homeRv.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false

        memesAdapter = MemesAdapter(activity!!, this)

        homeRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (dy > 0) {
                    bottomNavigationStateListener.homeHideBottomNavigation()
                } else if (dy < 0) {
                    bottomNavigationStateListener.homeShowBottomNavigation()

                }
            }
        })
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

    private val memesValueListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
            Log.e(TAG, "Error loading memes: ${p0.message}")
        }

        override fun onDataChange(p0: DataSnapshot) {
            if (p0.exists()) {
                if (homeShimmer.isShown) homeShimmer.stopShimmerAnimation()
                homeShimmer.visibility = View.GONE
            }
        }
    }

    private val memesChildListener = object : ChildEventListener {
        override fun onCancelled(p0: DatabaseError) {
            Log.e(TAG, "Error loading memes: ${p0.message}")
        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            Log.e(TAG, "Meme moved: ${p0.key}")
        }

        override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            val meme = p0.getValue(MemeModel::class.java)
            memesAdapter.updateMeme(meme!!)
        }

        override fun onChildAdded(p0: DataSnapshot, p1: String?) {
            val meme = p0.getValue(MemeModel::class.java)
            memesAdapter.addMeme(meme!!)
        }

        override fun onChildRemoved(p0: DataSnapshot) {
            val meme = p0.getValue(MemeModel::class.java)
            memesAdapter.removeMeme(meme!!)
        }
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
        val i = Intent(activity, ViewMemeActivity::class.java)
        DankMemesUtil.saveTemporaryImage(activity!!, image)
        i.putExtra("caption", meme.caption)
        startActivity(i)
        activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun showBottomSheet(meme: MemeModel, image: Bitmap) {
        bs = if (getUid() != meme.memePosterID) {
            BottomSheet.Builder(activity!!).sheet(R.menu.main_bottomsheet)
        } else {
            BottomSheet.Builder(activity!!).sheet(R.menu.main_bottomsheet_me)
        }

        bs.listener { _, which ->

            when(which) {
                R.id.bs_share -> DankMemesUtil.shareImage(activity!!, image)
                R.id.bs_delete -> deletePost(meme)
                R.id.bs_save -> {
                    if (storagePermissionGranted()) {
                        DankMemesUtil.saveImage(activity!!, image)
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
                val meme = dataSnapshot!!.getValue<MemeModel>(MemeModel::class.java)
                //Toast.makeText(getActivity(), "faveKey " + article.getFaveKey(), Toast.LENGTH_SHORT).show();

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
                val meme = dataSnapshot!!.getValue<MemeModel>(MemeModel::class.java)
                //Toast.makeText(getActivity(), "faveKey " + article.getFaveKey(), Toast.LENGTH_SHORT).show();

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
                if (!DankMemesUtil.validated(editText)) {
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

    override fun onResume() {
        mopubAdapter.refreshAds(activity!!.getString(R.string.ad_unit_id_native))
        super.onResume()
    }

    override fun onDestroy() {
        memesQuery.removeEventListener(memesValueListener)
        memesQuery.removeEventListener(memesChildListener)
        mopubAdapter.destroy()
        super.onDestroy()
    }

    interface HomeBottomNavigationStateListener{
        fun homeHideBottomNavigation()
        fun homeShowBottomNavigation()
    }

    fun bottomNavigationListener(bottomNavigationStateListener: HomeBottomNavigationStateListener) {
        this.bottomNavigationStateListener = bottomNavigationStateListener
    }


}
