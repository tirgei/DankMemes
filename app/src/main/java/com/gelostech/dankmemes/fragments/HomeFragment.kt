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




class HomeFragment : BaseFragment(), MemesAdapter.OnItemClickListener {
    private lateinit var memesAdapter: MemesAdapter
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
        homeShimmer.startShimmerAnimation()

        memesQuery = getDatabaseReference().child("dank-memes").orderByChild("time")
        memesQuery.addValueEventListener(memesValueListener)
        memesQuery.addChildEventListener(memesChildListener)
    }

    private fun initViews() {
        homeRv.setHasFixedSize(true)
        homeRv.layoutManager = LinearLayoutManager(activity)
        homeRv.addItemDecoration(RecyclerFormatter.DoubleDividerItemDecoration(activity!!))
        homeRv.itemAnimator = DefaultItemAnimator()

        memesAdapter = MemesAdapter(activity!!, this)
        homeRv.adapter = memesAdapter

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
            Log.e(TAG, "Error laoding memes: ${p0.message}")
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
            0 -> activity?.toast("Like")
            1 -> showBottomSheet(meme)
            2 -> activity?.toast("Fave")
            3 -> showComments(meme)
            4 -> showMeme(meme, image!!)
            5 -> showProfile(meme)

        }
    }

    private fun showProfile(meme: MemeModel) {
        val i = Intent(activity, ProfileActivity::class.java)
        i.putExtra("userId", meme.memePoster)
        startActivity(i)
        activity?.overridePendingTransition(R.anim.enter_b, R.anim.exit_a)
    }

    private fun showMeme(meme: MemeModel, image: Bitmap) {
        val i = Intent(activity, ViewMemeActivity::class.java)
        DankMemesUtil.saveTemporaryImage(activity!!, image)
        i.putExtra("caption", meme.caption)
        startActivity(i)
        activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun showBottomSheet(meme: MemeModel) {
        bs = if (getUid() != meme.memePosterID) {
            BottomSheet.Builder(activity!!).sheet(R.menu.main_bottomsheet)
        } else {
            BottomSheet.Builder(activity!!).sheet(R.menu.main_bottomsheet_me)
        }

        bs.listener { _, which ->

            when(which) {
                R.id.bs_share -> activity?.toast("Share")
                R.id.bs_delete -> deletePost(meme)
                R.id.bs_save -> activity?.toast("Save")
                R.id.bs_report -> activity?.toast("Report")
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

        activity?.alert("Delete this meme?") {
            positiveButton("DELETE") {
                dbRef.removeValue()
            }
            negativeButton("CANCEL"){}
        }
    }

    override fun onDestroy() {
        memesQuery.removeEventListener(memesValueListener)
        memesQuery.removeEventListener(memesChildListener)
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
