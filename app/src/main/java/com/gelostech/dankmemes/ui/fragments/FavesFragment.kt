package com.gelostech.dankmemes.ui.fragments


import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.ui.activities.ViewMemeActivity
import com.gelostech.dankmemes.ui.adapters.FavesAdapter
import com.gelostech.dankmemes.utils.AppUtils
import com.gelostech.dankmemes.ui.base.BaseFragment
import com.gelostech.dankmemes.utils.Constants
import com.gelostech.dankmemes.data.models.Fave
import com.gelostech.dankmemes.data.models.Meme
import com.gelostech.dankmemes.ui.adapters.PagedFavesAdapter
import com.gelostech.dankmemes.ui.callbacks.FavesCallback
import com.gelostech.dankmemes.ui.viewmodels.MemesViewModel
import com.gelostech.dankmemes.utils.RecyclerFormatter
import com.gelostech.dankmemes.utils.hideView
import com.gelostech.dankmemes.utils.showView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.makeramen.roundedimageview.RoundedDrawable
import com.makeramen.roundedimageview.RoundedImageView
import kotlinx.android.synthetic.main.fragment_faves.*
import org.jetbrains.anko.alert
import org.koin.android.ext.android.inject
import timber.log.Timber

class FavesFragment : BaseFragment() {
    private lateinit var favesAdapter: PagedFavesAdapter
    private val memesViewModel: MemesViewModel by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_faves, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initFavesObserver()
    }

    private fun initViews() {
        favesAdapter = PagedFavesAdapter(favesCallback)

        favesRv.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(activity!!, 3)
            addItemDecoration(RecyclerFormatter.GridItemDecoration(activity!!, R.dimen.grid_layout_margin))
            adapter = favesAdapter
        }
    }

    /**
     * Initialize observer for Faves LiveData
     */
    private fun initFavesObserver() {
        memesViewModel.fetchFaves().observe(this, Observer {
            favesAdapter.submitList(it)
        })
    }

    private val favesCallback = object : FavesCallback {
        override fun onFaveClick(view: RoundedImageView, meme: Fave, longClick: Boolean) {
            if (longClick) handleLongClick(meme.id!!)
            else handleClick(meme.imageUrl!!, (view.drawable as RoundedDrawable).sourceBitmap)
        }
    }

    /**
     * Function to handle click on Fave item
     */
    private fun handleClick(imageUrl: String, image: Bitmap) {
        AppUtils.saveTemporaryImage(activity!!, image)

        val i = Intent(activity, ViewMemeActivity::class.java)
        i.putExtra(Constants.PIC_URL, imageUrl)
        startActivity(i)
        AppUtils.fadeIn(activity!!)
    }

    /**
     * Function to handle long click on Fave item
     */
    private fun handleLongClick(faveId: String) {
        activity!!.alert("Remove meme from favorites?"){
            positiveButton("REMOVE") {
                removeFave(faveId)
            }
            negativeButton("CANCEL") {}
        }.show()
    }

    private fun removeFave(id: String) {
        val docRef = getFirestore().collection(Constants.MEMES).document(id)

        getFirestore().runTransaction {

            val meme =  it[docRef].toObject(Meme::class.java)
            val faves = meme!!.faves

            faves.remove(getUid())
            getFirestore().collection(Constants.FAVORITES).document(getUid()).collection(Constants.USER_FAVES).document(meme.id!!).delete()

            it.update(docRef, Constants.FAVES, faves)

            return@runTransaction null
        }.addOnSuccessListener {
            Timber.e("Meme faved")
        }.addOnFailureListener {
            Timber.e("Error faving meme")
        }

    }

}
