package com.gelostech.dankmemes.fragments


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
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
import com.gelostech.dankmemes.activities.ViewMemeActivity
import com.gelostech.dankmemes.adapters.MemesAdapter
import com.gelostech.dankmemes.commoners.BaseFragment
import com.gelostech.dankmemes.commoners.Config
import com.gelostech.dankmemes.commoners.AppUtils
import com.gelostech.dankmemes.models.FaveModel
import com.gelostech.dankmemes.models.MemeModel
import com.gelostech.dankmemes.models.UserModel
import com.gelostech.dankmemes.utils.RecyclerFormatter
import com.gelostech.dankmemes.utils.loadUrl
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_profile.*
import org.jetbrains.anko.alert


class ProfileFragment : BaseFragment(), MemesAdapter.OnItemClickListener {
    private lateinit var memesAdapter: MemesAdapter
    private lateinit var image: Bitmap
    private lateinit var user: UserModel
    private lateinit var profileRef: DatabaseReference
    private lateinit var memesQuery: Query
    private lateinit var bs: BottomSheet.Builder

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
        memesQuery = getDatabaseReference().child("dank-memes").orderByChild("memePosterID").equalTo(getUid())

        profileRef.addValueEventListener(profileListener)
//        memesQuery.addChildEventListener(memesChildListener)
//        memesQuery.addValueEventListener(memesValueListener)
    }

    private fun initViews() {
        profileRv.setHasFixedSize(true)
        profileRv.layoutManager = LinearLayoutManager(activity!!)
        profileRv.addItemDecoration(RecyclerFormatter.DoubleDividerItemDecoration(activity!!))
        profileRv.itemAnimator = DefaultItemAnimator()
        profileHeader.attachTo(profileRv)

        memesAdapter = MemesAdapter(activity!!, this)
        profileRv.adapter = memesAdapter
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

            profileImage.setOnClickListener {
                temporarilySaveImage()

                val i = Intent(activity, ViewMemeActivity::class.java)
                i.putExtra(Config.PIC_URL, user.userAvatar!!)
                AppUtils.fadeIn(activity!!)
            }
        }
    }
//
//    private val memesValueListener = object : ValueEventListener {
//        override fun onCancelled(p0: DatabaseError) {
//            Log.e(TAG, "Error loading memes: ${p0.message}")
//        }
//
//        override fun onDataChange(p0: DataSnapshot) {
//            if (p0.exists()) {
//                profileEmptyState.visibility = View.GONE
//            } else {
//                profileEmptyState.visibility = View.VISIBLE
//            }
//        }
//    }
//
//    private val memesChildListener = object : ChildEventListener {
//        override fun onCancelled(p0: DatabaseError) {
//            Log.e(TAG, "Error loading memes: ${p0.message}")
//        }
//
//        override fun onChildMoved(p0: DataSnapshot, p1: String?) {
//            Log.e(TAG, "Meme moved: ${p0.key}")
//        }
//
//        override fun onChildChanged(p0: DataSnapshot, p1: String?) {
//            val meme = p0.getValue(MemeModel::class.java)
//            memesAdapter.updateMeme(meme!!)
//        }
//
//        override fun onChildAdded(p0: DataSnapshot, p1: String?) {
//            val meme = p0.getValue(MemeModel::class.java)
//            memesAdapter.addMeme(meme!!)
//        }
//
//        override fun onChildRemoved(p0: DataSnapshot) {
//            val meme = p0.getValue(MemeModel::class.java)
//            memesAdapter.removeMeme(meme!!)
//        }
//    }

    private fun temporarilySaveImage() {
        image = (profileImage.drawable as BitmapDrawable).bitmap
        AppUtils.saveTemporaryImage(activity!!, image)
    }

    fun getUser() : UserModel = user

    override fun onItemClick(meme: MemeModel, viewID: Int, image: Bitmap?) {
        when(viewID) {
            0 -> likePost(meme.id!!)
            1 -> showBottomSheet(meme, image!!)
            2 -> favePost(meme.id!!)
            3 -> showComments(meme)
            4 -> showMeme(meme, image!!)
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
        bs = BottomSheet.Builder(activity!!).sheet(R.menu.main_bottomsheet_me)

        bs.listener { _, which ->

            when(which) {
                R.id.bs_share -> AppUtils.shareImage(activity!!, image)
                R.id.bs_delete -> deletePost(meme)
                R.id.bs_save -> {
                    if (storagePermissionGranted()) {
                        AppUtils.saveImage(activity!!, image)
                    } else requestStoragePermission()
                }
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
                    fave.id = meme.id!!
                    fave.imageUrl = meme.imageUrl!!

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

    override fun onDestroy() {
        profileRef.removeEventListener(profileListener)
//        memesQuery.removeEventListener(memesChildListener)
//        memesQuery.removeEventListener(memesValueListener)
        super.onDestroy()
    }


}
