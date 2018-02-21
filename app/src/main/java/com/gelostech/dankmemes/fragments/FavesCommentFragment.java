package com.gelostech.dankmemes.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.gelostech.dankmemes.R;
import com.gelostech.dankmemes.adapters.FavesCommentAdapter;
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

/**fetchComments
 * A simple {@link Fragment} subclass.
 */
public class FavesCommentFragment extends Fragment {
    private Toolbar toolbar;
    private List<CommentModel> modelList;
    private RecyclerView listView;
    private FavesCommentAdapter commentAdapter;
    private RecyclerView.LayoutManager lm;
    private EditText getComment;
    private Button sendComment;
    private String key;
    private DatabaseReference dr;
    private ProgressBar pb;
    private SharedPreferences preferences;

    public FavesCommentFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_faves_comment, container, false);

        key = getArguments().getString("commentKey");
        pb = (ProgressBar) view.findViewById(R.id.loading_faves_comments);

        toolbar = (Toolbar) getActivity().findViewById(R.id.faves_activity_toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.comment_title));
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity(), "Clicked!!!!!!!!", Toast.LENGTH_SHORT).show();
                if(getActivity().getSupportFragmentManager().getBackStackEntryCount() != 0)
                    getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        getComment = (EditText) view.findViewById(R.id.faves_enter_comment);
        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        modelList = new ArrayList<>();
        commentAdapter = new FavesCommentAdapter(getContext(), modelList);
        lm = new LinearLayoutManager(getActivity());
        listView = (RecyclerView) view.findViewById(R.id.faves_comment_list);

        listView.setHasFixedSize(true);
        listView.setLayoutManager(lm);
        listView.setAdapter(commentAdapter);

        sendComment = (Button) view.findViewById(R.id.faves_comment_send);
        sendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = preferences.getString("user_name", null);
                if(userName == null){
                    addUserName();
                } else {
                    if(TextUtils.isEmpty(getComment.getText().toString())){
                        Toast.makeText(getActivity(), "Please enter a comment!", Toast.LENGTH_SHORT).show();
                    } else {
                        newComments();
                        commentAdapter.notifyDataSetChanged();
                        getComment.setText("");
                    }
                }
            }
        });

        checkIfComments();
        fetchComments();

        return  view;
    }

    private void fetchComments(){
        dr = FirebaseDatabase.getInstance().getReference("comments");

        dr.child(key).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                CommentModel comments = dataSnapshot.getValue(CommentModel.class);
                modelList.add(comments);
                listView.setAdapter(commentAdapter);
                commentAdapter.notifyDataSetChanged();
                listView.setVisibility(View.VISIBLE);
                pb.setVisibility(View.GONE);

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
        pb.setVisibility(View.VISIBLE);
        dr = FirebaseDatabase.getInstance().getReference("comments");

        dr.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    pb.setVisibility(View.GONE);
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

    private void addUserName(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = (getActivity()).getLayoutInflater();
        final  View dialogView  = inflater.inflate(R.layout.custom_dialog, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        final EditText text = (EditText) dialogView.findViewById(R.id.report_reason);
        text.setHint("Enter username");
        builder.setTitle("Username");
        //builder.setMessage("Please enter a username to be able to post comments");
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int whichButton){
                if(TextUtils.isEmpty(text.getText().toString())){
                    Toast.makeText(getContext(), "Please enter name", Toast.LENGTH_LONG).show();
                } else {
                    SharedPreferences pref = getActivity().getSharedPreferences("DankMemes", Context.MODE_PRIVATE);
                    String userFaveId = pref.getString("userID", null);

                    UserModel model = new UserModel();

                    SharedPreferences.Editor prefEditor = preferences.edit();
                    prefEditor.putString("user_name", text.getText().toString());
                    prefEditor.apply();

                    model.setUserId(userFaveId);
                    model.setUserName(text.getText().toString());

                    DatabaseReference dr = FirebaseDatabase.getInstance().getReference("users").child(userFaveId);
                    dr.setValue(model);

                    newComments();

                }
            }
        });
        builder.setNegativeButton(null, null);

        final AlertDialog d = builder.create();
        d.show();

    }


}
