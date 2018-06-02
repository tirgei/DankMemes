package com.gelostech.dankmemes.fragments


import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.cocosw.bottomsheet.BottomSheet
import com.cocosw.bottomsheet.BottomSheetHelper

import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.activities.ProfileActivity
import com.gelostech.dankmemes.activities.ViewMemeActivity
import com.gelostech.dankmemes.adapters.MemesAdapter
import com.gelostech.dankmemes.commoners.BaseFragment
import com.gelostech.dankmemes.models.MemeModel
import com.gelostech.dankmemes.utils.RecyclerFormatter
import kotlinx.android.synthetic.main.fragment_home.*
import org.jetbrains.anko.toast


class HomeFragment : BaseFragment(), MemesAdapter.OnItemClickListener {
    private lateinit var memesAdapter: MemesAdapter
    private lateinit var bs: BottomSheet.Builder

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()

        Handler().postDelayed({loadSample()}, 2000)
    }

    private fun initViews() {
        homeRv.setHasFixedSize(true)
        homeRv.layoutManager = LinearLayoutManager(activity)
        homeRv.itemAnimator = DefaultItemAnimator()

        memesAdapter = MemesAdapter(activity!!, this)
        homeRv.adapter = memesAdapter
    }

    private fun loadSample() {
        if (homeShimmer.isShown) homeShimmer.stopShimmerAnimation()
        homeShimmer.visibility = View.GONE

        val meme1 = MemeModel("1", "Hahahah... ", "fd", R.drawable.games, 23, 45, System.currentTimeMillis(), "James Michael")
        memesAdapter.addMeme(meme1)

        val meme2 = MemeModel("1", "Do y'all agree with this?", "fd", R.drawable.prof, 23, 45, System.currentTimeMillis(), "Mickey Mouse")
        memesAdapter.addMeme(meme2)

        val meme3 = MemeModel("1", "Ahhhhh... my ribs :D", "fd", R.drawable.games, 23,  45, System.currentTimeMillis(), "Hellen Wanker")
        memesAdapter.addMeme(meme3)
    }

    override fun onItemClick(meme: MemeModel, viewID: Int) {
        when(viewID) {
            0 -> activity?.toast("Like")
            1 -> showBottomSheet(meme)
            2 -> activity?.toast("Fave")
            3 -> activity?.toast("Comment")
            4 -> showMeme(meme)
            5 -> showProfile(meme)

        }
    }

    private fun showProfile(meme: MemeModel) {
        val i = Intent(activity, ProfileActivity::class.java)
        i.putExtra("userId", meme.memePoster)
        startActivity(i)
        activity?.overridePendingTransition(R.anim.enter_b, R.anim.exit_a)
    }

    private fun showMeme(meme: MemeModel) {
        val i = Intent(activity, ViewMemeActivity::class.java)
        i.putExtra("isFave", false)
        i.putExtra("meme", meme)
        startActivity(i)
        activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun showBottomSheet(meme: MemeModel) {
        bs = if (getUid() != null) {
            BottomSheet.Builder(activity!!).sheet(R.menu.main_bottomsheet)
        } else {
            BottomSheet.Builder(activity!!).sheet(R.menu.main_bottomsheet_me)
        }

        bs.listener { _, which ->

            when(which) {
                R.id.bs_share -> activity?.toast("Share")
                R.id.bs_delete -> activity?.toast("Delete")
                R.id.bs_save -> activity?.toast("Save")
                R.id.bs_report -> activity?.toast("Report")
            }

        }.show()

    }

    override fun onResume() {
        super.onResume()
        homeShimmer.startShimmerAnimation()
    }

    override fun onPause() {
        homeShimmer.stopShimmerAnimation()
        super.onPause()
    }


}
