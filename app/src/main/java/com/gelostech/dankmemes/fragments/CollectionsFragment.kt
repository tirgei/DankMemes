package com.gelostech.dankmemes.fragments


import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.activities.ViewMemeActivity
import com.gelostech.dankmemes.adapters.CollectionsAdapter
import com.gelostech.dankmemes.adapters.FavesAdapter
import com.gelostech.dankmemes.commoners.BaseFragment
import com.gelostech.dankmemes.commoners.DankMemesUtil
import com.gelostech.dankmemes.models.CollectionModel
import com.gelostech.dankmemes.models.FaveModel
import com.gelostech.dankmemes.models.MemeModel
import com.gelostech.dankmemes.utils.RecyclerFormatter
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_collections.*
import org.jetbrains.anko.alert

class CollectionsFragment : BaseFragment(), FavesAdapter.OnItemClickListener{
    private lateinit var favesAdapter: FavesAdapter
    private lateinit var favesQuery: Query

    companion object {
        private var TAG = CollectionsFragment::class.java.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_collections, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()

        favesQuery = getDatabaseReference().child("favorites").child(getUid())
        favesQuery.addValueEventListener(favesValueListener)
        favesQuery.addChildEventListener(favesChildListener)

    }

    private fun initViews() {
        collectionsRv.setHasFixedSize(true)
        collectionsRv.layoutManager = GridLayoutManager(activity!!, 3)
        collectionsRv.addItemDecoration(RecyclerFormatter.GridItemDecoration(activity!!, R.dimen.grid_layout_margin))

        favesAdapter = FavesAdapter(this)
        collectionsRv.adapter = favesAdapter
    }

    private val favesValueListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
            Log.e(TAG, "Error fetching faves: ${p0.message}")
        }

        override fun onDataChange(p0: DataSnapshot) {
            if (p0.exists()) {
                collectionsEmptyState.visibility = View.GONE
                collectionsRv.visibility = View.VISIBLE
            } else {
                collectionsRv.visibility = View.GONE
                collectionsEmptyState.visibility = View.VISIBLE
            }
        }
    }

    private val favesChildListener = object : ChildEventListener {
        override fun onCancelled(p0: DatabaseError) {
            Log.e(TAG, "Error fetching faves: ${p0.message}")
        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            Log.e(TAG, "Fave moved: ${p0.key}")
        }

        override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            val fave = p0.getValue(FaveModel::class.java)
            favesAdapter.updateFave(fave!!)
        }

        override fun onChildAdded(p0: DataSnapshot, p1: String?) {
            val fave = p0.getValue(FaveModel::class.java)
            favesAdapter.addFave(fave!!)
        }

        override fun onChildRemoved(p0: DataSnapshot) {
            val fave = p0.getValue(FaveModel::class.java)
            favesAdapter.removeFave(fave!!)
        }
    }

    override fun onItemClick(fave: FaveModel, image: Bitmap) {
        val i = Intent(activity, ViewMemeActivity::class.java)
        DankMemesUtil.saveTemporaryImage(activity!!, image)
        startActivity(i)
        activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onLongItemClick(fave: FaveModel) {
        activity!!.alert("Remove meme from favorites?"){
            positiveButton("REMOVE") {
                removeFave(fave.faveKey!!)
            }
            negativeButton("CANCEL") {}
        }.show()
    }

    private fun removeFave(id: String) {
        getDatabaseReference().child("dank-memes").child(id).runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val meme = mutableData.getValue<MemeModel>(MemeModel::class.java)

                meme!!.faves.remove(getUid())
                getDatabaseReference().child("favorites").child(getUid()).child(meme.id!!).removeValue()

                mutableData.value = meme
                return Transaction.success(mutableData)
            }

            override fun onComplete(databaseError: DatabaseError?, b: Boolean, dataSnapshot: DataSnapshot?) {

                Log.d(javaClass.simpleName, "postTransaction:onComplete: $databaseError")
            }
        })
    }

    override fun onDestroy() {
        favesQuery.removeEventListener(favesValueListener)
        favesQuery.removeEventListener(favesChildListener)
        super.onDestroy()
    }
}
