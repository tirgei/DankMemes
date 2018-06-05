package com.gelostech.dankmemes.fragments


import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.activities.ViewMemeActivity
import com.gelostech.dankmemes.adapters.CollectionsAdapter
import com.gelostech.dankmemes.adapters.FavesAdapter
import com.gelostech.dankmemes.commoners.DankMemesUtil
import com.gelostech.dankmemes.models.CollectionModel
import com.gelostech.dankmemes.models.FaveModel
import com.gelostech.dankmemes.utils.RecyclerFormatter
import kotlinx.android.synthetic.main.fragment_collections.*

class CollectionsFragment : Fragment(), FavesAdapter.OnItemClickListener{
    private lateinit var collectionsAdapter: CollectionsAdapter
    private lateinit var favesAdapter: FavesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_collections, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()

        loadSample()
    }

    private fun initViews() {
        collectionsRv.setHasFixedSize(true)
        collectionsRv.layoutManager = GridLayoutManager(activity!!, 3)
        collectionsRv.addItemDecoration(RecyclerFormatter.GridItemDecoration(activity!!, R.dimen.grid_layout_margin))

        /*collectionsAdapter = CollectionsAdapter(activity!!)
        collectionsRv.adapter = collectionsAdapter*/

        favesAdapter = FavesAdapter(this)
        collectionsRv.adapter = favesAdapter
    }

//    private fun loadSample() {
//        val collection1 = CollectionModel("1", "Car memes", "sgdgf4564", 45, "ddhdhd")
//        collectionsAdapter.addCollection(collection1)
//
//        val collection2 = CollectionModel("1", "School", "sgdgf4564", 45, "ddhdhd")
//        collectionsAdapter.addCollection(collection2)
//
//        val collection3 = CollectionModel("1", "Dogs", "sgdgf4564", 45, "ddhdhd")
//        collectionsAdapter.addCollection(collection3)
//
//    }

    private fun loadSample() {
        val fave1 = FaveModel("1", "sdvsdvs", R.drawable.games)
        favesAdapter.addFave(fave1)

        val fave2 = FaveModel("2", "FDgf", R.drawable.prof)
        favesAdapter.addFave(fave2)

        val fave3 = FaveModel("23", "SDgds", R.drawable.games)
        favesAdapter.addFave(fave3)

        val fave4 = FaveModel("23", "SDgds", R.drawable.games)
        favesAdapter.addFave(fave4)
    }

    override fun onItemClick(fave: FaveModel, image: Bitmap) {
        val i = Intent(activity, ViewMemeActivity::class.java)
        DankMemesUtil.saveTemporaryImage(activity!!, image)
        startActivity(i)
        activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
