package com.gelostech.dankmemes.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.gelostech.dankmemes.R;
import com.gelostech.dankmemes.activities.FavoritesActivity;
import com.gelostech.dankmemes.fragments.DailyMemesFragment;
import com.gelostech.dankmemes.fragments.FavesItemFragment;
import com.gelostech.dankmemes.models.FaveListModel;
import com.github.captain_miao.optroundcardview.OptRoundCardView;

import java.util.List;

/**
 * Created by tirgei on 6/25/17.
 */

public class FavesListAdapter extends RecyclerView.Adapter<FavesListAdapter.FaveViewHolder>{
    private List<FaveListModel> listModels;
    private Context context;

    public class FaveViewHolder extends RecyclerView.ViewHolder{
        private ImageView imageView;
        private OptRoundCardView cv;

        public FaveViewHolder(View view){
            super(view);

            imageView = (ImageView) view.findViewById(R.id.fave_select_sample_meme);
            cv = (OptRoundCardView) view.findViewById(R.id.fave_item_card);

        }

    }

    public FavesListAdapter(Context context, List<FaveListModel> models){
        this.listModels = models;
        this.context = context;
    }

    @Override
    public FaveViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(parent.getContext()).inflate(R.layout.faves_list_card, parent, false);

        return new FaveViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FaveViewHolder holder, int position) {
        final FaveListModel faveListModel = listModels.get(position);

        Glide.with(context).load(faveListModel.getPicUrl()).thumbnail(0.05f).into(holder.imageView);

        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = v.getContext();
                FavesItemFragment fi = new FavesItemFragment();
                FragmentManager fm  = ((FavoritesActivity) context).getSupportFragmentManager();
                Bundle bundle = new Bundle();
                bundle.putString("faveId", faveListModel.getFaveKey());
                fi.setArguments(bundle);
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(R.id.faves_fragments_holder, fi);
                fragmentTransaction.addToBackStack("fave_list");
                fragmentTransaction.commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listModels.size();
    }

}
