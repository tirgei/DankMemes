package com.gelostech.dankmemes.fragments


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.activities.ViewMemeActivity
import com.gelostech.dankmemes.adapters.MemesAdapter
import com.gelostech.dankmemes.commoners.BaseFragment
import com.gelostech.dankmemes.commoners.DankMemesUtil
import com.gelostech.dankmemes.models.MemeModel
import com.gelostech.dankmemes.models.UserModel
import com.gelostech.dankmemes.utils.loadUrl
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_profile.*


class ProfileFragment : BaseFragment(), MemesAdapter.OnItemClickListener {
    private lateinit var memesAdapter: MemesAdapter
    private lateinit var image: Bitmap
    private lateinit var user: UserModel
    private lateinit var profileRef: DatabaseReference

    companion object {
        private val TAG = ProfileFragment::class.java.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()

        profileRef = getDatabaseReference().child("users").child(getUid())
        profileRef.addValueEventListener(profileListener)
        loadSample()
    }

    private fun initViews() {
        profileRv.setHasFixedSize(true)
        profileRv.layoutManager = LinearLayoutManager(activity)
        profileRv.itemAnimator = DefaultItemAnimator()

        memesAdapter = MemesAdapter(activity!!, this)
        profileRv.adapter = memesAdapter

        profileImage.setOnClickListener {
            temporarilySaveImage()
            startActivity(Intent(activity, ViewMemeActivity::class.java))
            activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private val profileListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
            Log.e(TAG, "Error loading profile: ${p0.message}")
        }

        override fun onDataChange(p0: DataSnapshot) {
            user = p0.getValue(UserModel::class.java)!!

            profileName.text = user.userName
            profileBio.text = user.userBio
            profileImage.loadUrl(user.userAvatar!!)
        }
    }

    private fun temporarilySaveImage() {
        image = (profileImage.drawable as BitmapDrawable).bitmap
        DankMemesUtil.saveTemporaryImage(activity!!, image)
    }

    fun getUser() : UserModel = user

    private fun loadSample() {
        val meme1 = MemeModel("1", "Hahahah... ", "fd", R.drawable.games, 23, 45, System.currentTimeMillis(), "Vincent Tirgei", "AEXdYtUP83MSvrb0aaIqCHN31G23")
        memesAdapter.addMeme(meme1)

        val meme2 = MemeModel("1", "Do y'all agree with this?", "fd", R.drawable.games, 23, 45, System.currentTimeMillis(), "Vincent Tirgei", "AEXdYtUP83MSvrb0aaIqCHN31G23")
        memesAdapter.addMeme(meme2)

        val meme3 = MemeModel("1", "Ahhhhh... my ribs :D", "fd", R.drawable.games, 23, 45, System.currentTimeMillis(), "Vincent Tirgei", "AEXdYtUP83MSvrb0aaIqCHN31G23")
        memesAdapter.addMeme(meme3)
    }

    override fun onItemClick(meme: MemeModel, viewID: Int, image: Bitmap?) {

    }

    override fun onDestroy() {
        profileRef.removeEventListener(profileListener)
        super.onDestroy()
    }

}
