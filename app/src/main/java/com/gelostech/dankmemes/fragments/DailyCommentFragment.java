package com.gelostech.dankmemes.fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gelostech.dankmemes.R;
import com.gelostech.dankmemes.adapters.DailyCommentAdapter;
import com.gelostech.dankmemes.models.CommentModel;
import com.gelostech.dankmemes.models.UserModel;
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
public class DailyCommentFragment extends Fragment {
    private Toolbar toolbar;
    private List<CommentModel> modelList;
    private RecyclerView listView;
    private DailyCommentAdapter commentAdapter;
    private RecyclerView.LayoutManager lm;
    private EditText getComment;
    private Long comment, time, userName;
    private Button sendComment, thumbsUp, thumbsDown;
    private String key;
    private ProgressDialog pd;
    private DatabaseReference dr;

    public DailyCommentFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_daily_comment, container, false);
        key = getArguments().getString("commentKey");
        pd = new ProgressDialog(getContext());
        pd.setMessage("Loading comments...");
        pd.setCancelable(false);
        pd.show();

        toolbar = (Toolbar) view.findViewById(R.id.daily_comment_toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Clicked!!!!!!!!", Toast.LENGTH_SHORT).show();
                if(getFragmentManager().getBackStackEntryCount() != 0)
                    getFragmentManager().popBackStack();
            }
        });

        getComment = (EditText) view.findViewById(R.id.daily_enter_comment);

        modelList = new ArrayList<>();
        commentAdapter = new DailyCommentAdapter(modelList);
        lm = new LinearLayoutManager(getActivity());
        listView = (RecyclerView) view.findViewById(R.id.daily_comment_list);

        listView.setHasFixedSize(true);
        listView.setLayoutManager(lm);
        listView.setAdapter(commentAdapter);

        sendComment = (Button) view.findViewById(R.id.daily_comment_send);
        sendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(getComment.getText().toString())){
                    Toast.makeText(getActivity(), "Please enter a comment!", Toast.LENGTH_SHORT).show();
                } else {
                    newComments();
                    commentAdapter.notifyDataSetChanged();
                    getComment.setText("");
                }
            }
        });

        checkIfComments();
        fetchComments();

        return view;
    }

    private void fetchComments(){
        dr = FirebaseDatabase.getInstance().getReference("comments");

        dr.child(key).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                pd.dismiss();
                CommentModel comments = dataSnapshot.getValue(CommentModel.class);
                modelList.add(comments);
                listView.setAdapter(commentAdapter);
                commentAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Meme_list_comments: ", databaseError.getMessage());
            }
        });
    }

    private void newComments(){
        dr = FirebaseDatabase.getInstance().getReference("comments").child(key);

        UserModel model = new UserModel();

        String uploadId = dr.push().getKey();
        CommentModel myComment = new CommentModel("Tirgei", System.currentTimeMillis(), getComment.getText().toString(), key, uploadId);
        dr.child(uploadId).setValue(myComment);

    }

    private void checkIfComments(){
        dr = FirebaseDatabase.getInstance().getReference("comments");

        dr.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    pd.dismiss();
                    Toast.makeText(getContext(), "No comments available!", Toast.LENGTH_LONG).show();
                } else {
                    //fetchComments();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Meme List comments: ", "Error checking if comments available");
            }
        });

    }


}
