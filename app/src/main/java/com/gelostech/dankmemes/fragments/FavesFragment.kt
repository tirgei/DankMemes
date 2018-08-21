package com.gelostech.dankmemes.fragments


import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.activities.ViewMemeActivity
import com.gelostech.dankmemes.adapters.FavesAdapter
import com.gelostech.dankmemes.commoners.BaseFragment
import com.gelostech.dankmemes.commoners.Config
import com.gelostech.dankmemes.commoners.AppUtils
import com.gelostech.dankmemes.models.FaveModel
import com.gelostech.dankmemes.models.MemeModel
import com.gelostech.dankmemes.utils.RecyclerFormatter
import com.gelostech.dankmemes.utils.hideView
import com.gelostech.dankmemes.utils.showView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_faves.*
import org.jetbrains.anko.alert
import timber.log.Timber

class FavesFragment : BaseFragment(), FavesAdapter.OnItemClickListener{
    private lateinit var favesAdapter: FavesAdapter
    private lateinit var loadMoreFooter: RelativeLayout
    private var lastDocument: DocumentSnapshot? = null
    private lateinit var query: Query
    private var loading = false

    companion object {
        private var TAG = FavesFragment::class.java.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_faves, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        load(true)
    }

    private fun initViews() {
        favesRv.setHasFixedSize(true)
        favesRv.layoutManager = GridLayoutManager(activity!!, 3)
        favesRv.addItemDecoration(RecyclerFormatter.GridItemDecoration(activity!!, R.dimen.grid_layout_margin))

        favesAdapter = FavesAdapter(this)
        favesRv.adapter = favesAdapter

        loadMoreFooter = favesRv.loadMoreFooterView as RelativeLayout
        favesRv.setOnLoadMoreListener {
            if (!loading) {
                loadMoreFooter.showView()
                load(false)
            }
        }
    }

    private fun load(initial: Boolean) {
        query = if (lastDocument == null) {
            getFirestore().collection(Config.FAVORITES).document(getUid()).collection(Config.USER_FAVES)
                    .orderBy(Config.TIME, Query.Direction.DESCENDING)
                    .limit(15)
        } else {
            loading = true

            getFirestore().collection(Config.FAVORITES).document(getUid()).collection(Config.USER_FAVES)
                    .orderBy(Config.TIME, Query.Direction.DESCENDING)
                    .startAfter(lastDocument!!)
                    .limit(15)
        }

        query.addSnapshotListener { p0, p1 ->
            hasPosts()
            loading = false

            if (p1 != null) {
                Timber.e("Error loading faves: $p1")
            }

            if (p0 == null || p0.isEmpty) {
                if (initial) {
                    noPosts()
                }
            } else {
                lastDocument = p0.documents[p0.size()-1]

                for (change: DocumentChange in p0.documentChanges) {
                    Timber.e("Loading changed document")

                    when(change.type) {
                        DocumentChange.Type.ADDED -> {
                            val fave = change.document.toObject(FaveModel::class.java)
                            favesAdapter.addFave(fave)
                        }

                        DocumentChange.Type.MODIFIED -> {
                            val fave = change.document.toObject(FaveModel::class.java)
                            favesAdapter.updateFave(fave)
                        }

                        DocumentChange.Type.REMOVED -> {
                            val fave = change.document.toObject(FaveModel::class.java)
                            favesAdapter.removeFave(fave)
                        }


                    }
                }

            }

        }
    }

    override fun onItemClick(fave: FaveModel, image: Bitmap) {
        AppUtils.saveTemporaryImage(activity!!, image)

        val i = Intent(activity, ViewMemeActivity::class.java)
        i.putExtra(Config.PIC_URL, fave.imageUrl)
        startActivity(i)
        AppUtils.fadeIn(activity!!)
    }

    override fun onLongItemClick(fave: FaveModel) {
        activity!!.alert("Remove meme from favorites?"){
            positiveButton("REMOVE") {
                removeFave(fave.id!!)
            }
            negativeButton("CANCEL") {}
        }.show()
    }

    private fun removeFave(id: String) {
        val docRef = getFirestore().collection(Config.MEMES).document(id)

        getFirestore().runTransaction {

            val meme =  it[docRef].toObject(MemeModel::class.java)
            val faves = meme!!.faves

            faves.remove(getUid())
            getFirestore().collection(Config.FAVORITES).document(getUid()).collection(Config.USER_FAVES).document(meme.id!!).delete()

            it.update(docRef, Config.FAVES, faves)

            return@runTransaction null
        }.addOnSuccessListener {
            Timber.e("Meme faved")
        }.addOnFailureListener {
            Timber.e("Error faving meme")
        }

    }

    private fun hasPosts() {
        collectionsEmptyState?.hideView()
        favesRv?.showView()
    }

    private fun noPosts() {
        favesRv?.hideView()
        collectionsEmptyState?.showView()
    }

}
