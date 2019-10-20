package com.gelostech.dankmemes.ui.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.cocosw.bottomsheet.BottomSheet
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.ui.adapters.MemesAdapter
import com.gelostech.dankmemes.utils.AppUtils
import com.gelostech.dankmemes.ui.base.BaseActivity
import com.gelostech.dankmemes.utils.Constants
import com.gelostech.dankmemes.data.models.FaveModel
import com.gelostech.dankmemes.data.models.MemeModel
import com.gelostech.dankmemes.data.models.ReportModel
import com.gelostech.dankmemes.data.models.UserModel
import com.gelostech.dankmemes.utils.RecyclerFormatter
import com.gelostech.dankmemes.utils.hideView
import com.gelostech.dankmemes.utils.loadUrl
import com.gelostech.dankmemes.utils.showView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.android.synthetic.main.activity_profile.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import timber.log.Timber

class ProfileActivity : BaseActivity(), MemesAdapter.OnItemClickListener {
    private lateinit var memesAdapter: MemesAdapter
    private lateinit var image: Bitmap
    private lateinit var profileRef: DatabaseReference
    private lateinit var bs: BottomSheet.Builder
    private lateinit var name: String
    private lateinit var userId: String
    private lateinit var loadMoreFooter: RelativeLayout
    private var lastDocument: DocumentSnapshot? = null
    private lateinit var query: com.google.firebase.firestore.Query
    private var loading = false

    companion object {
        private var TAG = ProfileActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        userId = intent.getStringExtra("userId")

        initViews()
        load(true)

        profileRef = getDatabaseReference().child("users").child(userId)
        profileRef.addValueEventListener(profileListener)
    }

    private fun initViews() {
        setSupportActionBar(viewProfileToolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.profile)

        viewProfileRv.setHasFixedSize(true)
        viewProfileRv.layoutManager = LinearLayoutManager(this)
        viewProfileRv.addItemDecoration(RecyclerFormatter.DoubleDividerItemDecoration(this))
        viewProfileRv.itemAnimator = DefaultItemAnimator()
        (viewProfileRv.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
        viewProfileHeader.attachTo(viewProfileRv)

        memesAdapter = MemesAdapter(this, this)
        viewProfileRv.adapter = memesAdapter

        loadMoreFooter = viewProfileRv.loadMoreFooterView as RelativeLayout
        viewProfileRv.setOnLoadMoreListener {
            if (!loading) {
                loadMoreFooter.showView()
                load(false)
            }
        }

    }

    private fun load(initial: Boolean) {
        query = if (lastDocument == null) {
            getFirestore().collection(Constants.MEMES)
                    .whereEqualTo(Constants.POSTER_ID, userId)
                    .orderBy(Constants.TIME, com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(15)
        } else {
            loading = true

            getFirestore().collection(Constants.MEMES)
                    .whereEqualTo(Constants.POSTER_ID, userId)
                    .orderBy(Constants.TIME, com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .startAfter(lastDocument!!)
                    .limit(15)
        }

        query.addSnapshotListener { p0, p1 ->
            hasPosts()
            loading = false

            if (p1 != null) {
                Timber.e( "Error loading initial memes: $p1")
            }

            if (p0 == null || p0.isEmpty) {
                if (initial) noPosts()
            } else {
                lastDocument = p0.documents[p0.size()-1]

                for (change: DocumentChange in p0.documentChanges) {
                    Timber.e("Loading changed document")

                    when(change.type) {
                        DocumentChange.Type.ADDED -> {
                            val meme = change.document.toObject(MemeModel::class.java)
                            memesAdapter.addMeme(meme)

                            Timber.e("Changed document: ADDED")
                        }

                        DocumentChange.Type.MODIFIED -> {
                            val meme = change.document.toObject(MemeModel::class.java)
                            memesAdapter.updateMeme(meme)

                            Timber.e("Changed document: MODIFIED")
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

    private fun temporarilySaveImage() {
        image = (viewProfileImage.drawable as BitmapDrawable).bitmap
        AppUtils.saveTemporaryImage(this, image)
    }

    private val profileListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
            Log.e(TAG, "Error loading profile: ${p0.message}")
        }

        override fun onDataChange(p0: DataSnapshot) {
            val user = p0.getValue(UserModel::class.java)!!
            name = user.userName!!

            viewProfileName.text = user.userName
            viewProfileBio.text = user.userBio
            viewProfileImage.loadUrl(user.userAvatar!!)

            viewProfileImage.setOnClickListener {
                temporarilySaveImage()
                val i = Intent(this@ProfileActivity, ViewMemeActivity::class.java)
                i.putExtra(Constants.PIC_URL, user.userAvatar!!)
                startActivity(i)
                AppUtils.fadeIn(this@ProfileActivity)
            }
        }
    }

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
        AppUtils.saveTemporaryImage(this, image)

        val i = Intent(this, ViewMemeActivity::class.java)
        i.putExtra(Constants.PIC_URL, meme.imageUrl)
        i.putExtra("caption", meme.caption)
        startActivity(i)
        AppUtils.fadeIn(this)
    }

    private fun showBottomSheet(meme: MemeModel, image: Bitmap) {
        bs = BottomSheet.Builder(this).sheet(R.menu.main_bottomsheet)

        bs.listener { _, which ->

            when(which) {
                R.id.bs_share -> AppUtils.shareImage(this, image)
                R.id.bs_save -> {
                    if (storagePermissionGranted()) {
                        AppUtils.saveImage(this, image)
                    } else requestStoragePermission()
                }
                R.id.bs_report -> showReportDialog(meme)
            }

        }.show()

    }

    private fun showComments(meme: MemeModel) {
        val i = Intent(this, CommentActivity::class.java)
        i.putExtra("memeId", meme.id)
        startActivity(i)
        overridePendingTransition(R.anim.enter_b, R.anim.exit_a)
    }

    private fun likePost(id: String) {
        val docRef = getFirestore().collection(Constants.MEMES).document(id)

        getFirestore().runTransaction {

            val meme =  it[docRef].toObject(MemeModel::class.java)
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

            val meme =  it[docRef].toObject(MemeModel::class.java)
            val faves = meme!!.faves

            if (faves.containsKey(getUid())) {
                faves.remove(getUid())

                getFirestore().collection(Constants.FAVORITES).document(getUid()).collection(Constants.USER_FAVES).document(meme.id!!).delete()
            } else  {
                faves[getUid()] = true

                val fave = FaveModel()
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

    private fun showReportDialog(meme: MemeModel) {
        val editText = EditText(this)
        val layout = FrameLayout(this)
        layout.setPaddingRelative(45,15,45,0)
        layout.addView(editText)

        alert("Please provide a reason for reporting") {
            customView = layout

            positiveButton("REPORT") {
                if (!AppUtils.validated(editText)) {
                    toast("Please enter a reason to report")
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
                    toast("Meme reported!")
                }

            }

            negativeButton("CANCEL"){}
        }.show()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> onBackPressed()
        }

        return true
    }

    private fun hasPosts() {
        viewProfileEmptyState?.hideView()
    }

    private fun noPosts() {
        viewProfileEmptyState?.showView()
        viewProfileEmptyStateText.text = "$name hasn't posted any memes yet"
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.enter_a, R.anim.exit_b)
    }

    override fun onDestroy() {
        profileRef.removeEventListener(profileListener)
        super.onDestroy()
    }
}
