package com.gelostech.dankmemes.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gelostech.dankmemes.R;
import com.gelostech.dankmemes.adapters.FavesListAdapter;
import com.gelostech.dankmemes.commoners.ConnectivityReceiver;
import com.gelostech.dankmemes.models.FaveListModel;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavoritesListFragment extends Fragment implements ConnectivityReceiver.ConnectivityReceiverListener{
    private List<FaveListModel> modelList;
    private RecyclerView rv;
    private RecyclerView.LayoutManager lm;
    private FavesListAdapter listAdapter;
    private Toolbar toolbar;
    private DatabaseReference dr;
    private ProgressBar pb;
    private RelativeLayout relativeLayout;
    private TextView noMemes;
    private InterstitialAd interstitialAd;

    public FavoritesListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favorites_list, container, false);

        toolbar = (Toolbar) getActivity().findViewById(R.id.faves_activity_toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Favorites");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        rv = (RecyclerView) view.findViewById(R.id.faves_list);
        modelList = new ArrayList<>();
        lm = new GridLayoutManager(getActivity(), 3);
        listAdapter = new FavesListAdapter(getContext(), modelList);
        pb = (ProgressBar) view.findViewById(R.id.loading_fave_memes);
        pb.setVisibility(View.VISIBLE);
        relativeLayout = (RelativeLayout) view.findViewById(R.id.faves_no_internet);
        noMemes = (TextView) view.findViewById(R.id.faves_null_text);

        rv.setHasFixedSize(true);
        rv.setLayoutManager(lm);

        interstitialAd = new InterstitialAd(getActivity());
        interstitialAd.setAdUnitId(getActivity().getResources().getString(R.string.interstitial_ad_id));
        interstitialAd.loadAd(new AdRequest.Builder().build());
        interstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                interstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(getActivity().getSupportFragmentManager().getBackStackEntryCount() != 0) {
                    getActivity().getSupportFragmentManager().popBackStack();
                    //Toast.makeText(getActivity(), "Exiting Faves..", Toast.LENGTH_SHORT).show();
                } else {
                    getActivity().finish();
                    getActivity().overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
                    //Toast.makeText(getActivity(), "Exiting Faves..", Toast.LENGTH_SHORT).show();

                    if(interstitialAd.isLoaded()){
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                interstitialAd.show();
                            }
                        }, 700);
                    }
                }
            }
        });

        checkIfFaves();
        fetchFaves();

        return view;
    }

    private void checkIfFaves(){
        dr = FirebaseDatabase.getInstance().getReference("favorites");

        final SharedPreferences pref = getActivity().getSharedPreferences("DankMemes", Context.MODE_PRIVATE);
        final String userFaveId = pref.getString("userID", null);

        dr.child(userFaveId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    pb.setVisibility(View.GONE);
                    relativeLayout.setVisibility(View.VISIBLE);
                    noMemes.setText("No memes favorited yet!");
                    //Toast.makeText(getContext(), "No memes favorited yet!", Toast.LENGTH_LONG).show();
                } else {
                    //fillComments();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Meme List comments: ", "Error checking if comments available");
            }
        });

    }

    private void fetchFaves(){
        final SharedPreferences pref = getActivity().getSharedPreferences("DankMemes", Context.MODE_PRIVATE);
        final String userFaveId = pref.getString("userID", null);

        dr = FirebaseDatabase.getInstance().getReference("favorites");

        dr.child(userFaveId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                FaveListModel model = dataSnapshot.getValue(FaveListModel.class);
                modelList.add(model);
                rv.setAdapter(listAdapter);
                listAdapter.notifyDataSetChanged();
                rv.setVisibility(View.VISIBLE);
                pb.setVisibility(View.GONE);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                pb.setVisibility(View.GONE);
                Log.d("Faves fetch error: ", databaseError.getMessage());
            }
        });
    }


    @Override
    public void onNetworkChanged(boolean isConnected) {
        if(isConnected){
            checkIfFaves();
            fetchFaves();
        } else {
            relativeLayout.setVisibility(View.VISIBLE);
            noMemes.setText("No internet connection!");
        }
    }
}
