package com.gelostech.dankmemes.ui.fragments


import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cocosw.bottomsheet.BottomSheet
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.Status
import com.gelostech.dankmemes.data.models.Fave
import com.gelostech.dankmemes.data.models.Meme
import com.gelostech.dankmemes.data.models.Report
import com.gelostech.dankmemes.data.responses.GenericResponse
import com.gelostech.dankmemes.ui.activities.CommentActivity
import com.gelostech.dankmemes.ui.activities.ProfileActivity
import com.gelostech.dankmemes.ui.activities.ViewMemeActivity
import com.gelostech.dankmemes.ui.adapters.PagedMemesAdapter
import com.gelostech.dankmemes.ui.base.BaseFragment
import com.gelostech.dankmemes.ui.callbacks.MemesCallback
import com.gelostech.dankmemes.ui.viewmodels.MemesViewModel
import com.gelostech.dankmemes.utils.*
import kotlinx.android.synthetic.main.fragment_all.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import org.koin.android.ext.android.inject
import timber.log.Timber

class AllFragment : BaseFragment() {
    private lateinit var pagedMemesAdapter: PagedMemesAdapter
    private lateinit var bs: BottomSheet.Builder
    private val memesViewModel: MemesViewModel by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_all, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()

        allShimmer.startShimmerAnimation()
        initMemesObserver()
        initResponseObserver()
    }

    private fun initViews() {
        pagedMemesAdapter = PagedMemesAdapter(memesCallback)
        allRefresh.isEnabled = false

        allRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            addItemDecoration(RecyclerFormatter.DoubleDividerItemDecoration(activity!!))
            itemAnimator = DefaultItemAnimator()
            (itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
            adapter = pagedMemesAdapter
        }
    }

    /**
     * Initialize observer for Memes LiveData
     */
    private fun initMemesObserver() {
        memesViewModel.fetchMemes().observe(this, Observer {
            allShimmer?.stopShimmerAnimation()
            allShimmer?.visibility = View.GONE
            pagedMemesAdapter.submitList(it)
        })
    }

    /**
     * Initialize observer for Generic Response LiveData
     */
    private fun initResponseObserver() {
        memesViewModel.genericResponseLiveData.observe(this, Observer {
            when (it.status) {
                Status.LOADING -> { Timber.e("Loading...") }

                Status.SUCCESS -> { Timber.e("Success \uD83D\uDE03") }

                Status.ERROR -> {
                    val message = when (it.item) {
                        GenericResponse.ITEM_RESPONSE.LIKE_MEME -> "Error liking meme"
                        GenericResponse.ITEM_RESPONSE.FAVE_MEME -> "Error faving meme"
                        GenericResponse.ITEM_RESPONSE.DELETE_MEME -> "Error deleting meme"
                        else -> "An error has occurred. "
                    }

                    toast("$message. Please try again")
                }
            }
        })
    }

    private val memesCallback = object : MemesCallback {
        override fun onMemeClicked(view: View, meme: Meme) {
            val memeId = meme.id!!

            when(view.id) {
                R.id.memeComment -> showComments(memeId)
                R.id.memeIcon, R.id.memeUser -> showProfile(memeId)

                R.id.memeFave -> {
                    animateView(view)
                    memesViewModel.faveMeme(memeId, getUid())
                }

                R.id.memeLike -> {
                    animateView(view)
                    memesViewModel.likeMeme(memeId, getUid())
                }

                else -> {
                    doAsync {
                        // Get bitmap of shown meme
                        val imageBitmap = when(view.id) {
                            R.id.memeImage, R.id.memeMore -> AppUtils.loadBitmapFromUrl(activity!!, meme.imageUrl!!)
                            else -> null
                        }

                        uiThread {
                            if (view.id == R.id.memeMore) showBottomSheet(meme, imageBitmap!!)
                            else showMeme(meme, imageBitmap!!)
                        }
                    }
                }
            }
        }
    }

    /**
     * Launch the Profile of the meme poster
     * @param userId - ID of the user
     */
    private fun showProfile(userId: String) {
        if (userId != getUid()) {
            val i = Intent(activity, ProfileActivity::class.java)
            i.putExtra("userId", userId)
            startActivity(i)
            activity?.overridePendingTransition(R.anim.enter_b, R.anim.exit_a)
        }
    }

    /**
     * Launch activity to view full meme photo
     */
    private fun showMeme(meme: Meme, image: Bitmap) {
        AppUtils.saveTemporaryImage(activity!!, image)

        val i = Intent(activity, ViewMemeActivity::class.java)
        i.putExtra(Constants.PIC_URL, meme.imageUrl)
        i.putExtra("caption", meme.caption)
        startActivity(i)
        AppUtils.fadeIn(activity!!)
    }

    /**
     * Show BottomSheet with extra actions
     */
    private fun showBottomSheet(meme: Meme, image: Bitmap) {
//        bs = if (getUid() != meme.memePosterID) {
//            BottomSheet.Builder(activity!!).sheet(R.menu.main_bottomsheet)
//        } else {
//            BottomSheet.Builder(activity!!).sheet(R.menu.main_bottomsheet_me)
//        }
        bs = BottomSheet.Builder(activity!!).sheet(R.menu.main_bottomsheet_admin)

        bs.listener { _, which ->
            when(which) {
                R.id.bs_share -> AppUtils.shareImage(activity!!, image)

                R.id.bs_delete -> {
                    activity!!.alert("Delete this meme?") {
                        title = "Delete Meme"
                        positiveButton("Delete") { memesViewModel.deleteMeme(meme.id!!) }
                        negativeButton("Cancel") {}
                    }.show()
                }

                R.id.bs_save -> {
                    if (storagePermissionGranted()) {
                        AppUtils.saveImage(activity!!, image)
                    } else requestStoragePermission()
                }

                R.id.bs_report -> showReportDialog(meme)
            }

        }.show()

    }

    /**
     * Launch the comments activity
     */
    private fun showComments(memeId: String) {
        val i = Intent(activity, CommentActivity::class.java)
        i.putExtra("memeId", memeId)
        startActivity(i)
        activity?.overridePendingTransition(R.anim.enter_b, R.anim.exit_a)
    }

    private fun favePost(id: String) {
//        val docRef = getFirestore().collection(Constants.MEMES).document(id)
//
//        getFirestore().runTransaction {
//
//            val meme =  it[docRef].toObject(Meme::class.java)
//            val faves = meme!!.faves
//
//            if (faves.containsKey(getUid())) {
//                faves.remove(getUid())
//
//                getFirestore().collection(Constants.FAVORITES).document(getUid()).collection(Constants.USER_FAVES).document(meme.id!!).delete()
//            } else  {
//                faves[getUid()] = true
//
//                val fave = Fave()
//                fave.id = meme.id!!
//                fave.imageUrl = meme.imageUrl!!
//                fave.time = meme.time!!
//
//                getFirestore().collection(Constants.FAVORITES).document(getUid()).collection(Constants.USER_FAVES).document(meme.id!!).set(fave)
//            }
//
//            it.update(docRef, Constants.FAVES, faves)
//
//            return@runTransaction null
//        }.addOnSuccessListener {
//            Timber.e("Meme faved")
//        }.addOnFailureListener {
//            Timber.e("Error faving meme")
//        }
//
    }

    private fun showReportDialog(meme: Meme) {
//        val editText = EditText(activity)
//        val layout = FrameLayout(activity!!)
//        layout.setPaddingRelative(45,15,45,0)
//        layout.addView(editText)
//
//        activity!!.alert("Please provide a reason for reporting") {
//            customView = layout
//
//            positiveButton("REPORT") {
//                if (!AppUtils.validated(editText)) {
//                    activity!!.toast("Please enter a reason to report")
//                    return@positiveButton
//                }
//
//                val key = getDatabaseReference().child("reports").push().key
//                val reason = editText.text.toString().trim()
//
//                val report = Report()
//                report.id = key
//                report.memeId = meme.id
//                report.memePosterId = meme.memePosterID
//                report.reporterId = getUid()
//                report.memeUrl = meme.imageUrl
//                report.reason = reason
//                report.time = System.currentTimeMillis()
//
//                getDatabaseReference().child("reports").child(key!!).setValue(report).addOnCompleteListener {
//                    activity!!.toast("Meme reported!")
//                }
//
//            }
//
//            negativeButton("CANCEL"){}
//        }.show()
    }

    fun getRecyclerView(): RecyclerView {
        return allRv
    }
}

