package com.gelostech.dankmemes.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.gelostech.dankmemes.R;
import com.gelostech.dankmemes.activities.DailyMemesActivity;
import com.gelostech.dankmemes.adapters.DailyAdapter;
import com.gelostech.dankmemes.commoners.FirebaseConstants;
import com.gelostech.dankmemes.models.UploadModel;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */

public class DailyMemesFragment extends Fragment {
    private List<Object> modelList;
    private DatabaseReference dr;
    private String date;
    private RecyclerView rv;
    private LinearLayoutManager lm;
    private DailyAdapter adapter;
    private ProgressBar pb;

    public DailyMemesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_daily_memes, container, false);

        DailyMemesActivity dailyMemes = (DailyMemesActivity) getActivity();
        date = dailyMemes.getIntent().getExtras().getString("Date");
        //Toast.makeText(getContext(), date, Toast.LENGTH_LONG).show();

        modelList = new ArrayList<>();
        rv = (RecyclerView) view.findViewById(R.id.daily_memes_rv);
        rv.setHasFixedSize(true);
        lm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(lm);
        adapter = new DailyAdapter(getActivity(), modelList);
        rv.setAdapter(adapter);
        pb = (ProgressBar) view.findViewById(R.id.loading_daily_memes);

        loadMemes();
        return view;
    }

    private void loadMemes(){
        dr = FirebaseDatabase.getInstance().getReference(FirebaseConstants.MEMES_PATH_UPLOADS + "/");
        dr.child(date).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                UploadModel model = dataSnapshot.getValue(UploadModel.class);
                modelList.add(model);
                rv.setVisibility(View.VISIBLE);
                pb.setVisibility(View.GONE);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Daily memes error: ", databaseError.getMessage());
            }
        });


    }


}
