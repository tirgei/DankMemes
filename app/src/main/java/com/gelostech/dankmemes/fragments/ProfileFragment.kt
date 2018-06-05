package com.gelostech.dankmemes.fragments


import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.adapters.MemesAdapter
import com.gelostech.dankmemes.models.MemeModel
import com.gelostech.dankmemes.utils.loadUrl
import kotlinx.android.synthetic.main.fragment_profile.*


class ProfileFragment : Fragment(), MemesAdapter.OnItemClickListener {
    private lateinit var memesAdapter: MemesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        loadSample()
    }

    private fun initViews() {
        profileRv.setHasFixedSize(true)
        profileRv.layoutManager = LinearLayoutManager(activity)
        profileRv.itemAnimator = DefaultItemAnimator()

        memesAdapter = MemesAdapter(activity!!, this)
        profileRv.adapter = memesAdapter

        profileImage.loadUrl(R.drawable.person)
        profileName.text = "Vincent Tirgei"
        profileBio.text = "Here's some random brief summary about me :) "
    }

    private fun loadSample() {
        val meme1 = MemeModel("1", "Hahahah... ", "fd", R.drawable.games, 23, 45, System.currentTimeMillis(), "Vincent Tirgei")
        memesAdapter.addMeme(meme1)

        val meme2 = MemeModel("1", "Do y'all agree with this?", "fd", R.drawable.games, 23, 45, System.currentTimeMillis(), "Vincent Tirgei")
        memesAdapter.addMeme(meme2)

        val meme3 = MemeModel("1", "Ahhhhh... my ribs :D", "fd", R.drawable.games, 23, 45, System.currentTimeMillis(), "Vincent Tirgei")
        memesAdapter.addMeme(meme3)
    }

    override fun onItemClick(meme: MemeModel, viewID: Int, image: Bitmap?) {

    }
}
