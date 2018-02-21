package com.gelostech.dankmemes.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gelostech.dankmemes.R;
import com.gelostech.dankmemes.commoners.ButtonBounceInterpolator;
import com.gelostech.dankmemes.models.CommentModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by root on 6/28/17.
 */

public class MemeListCommentAdapter extends RecyclerView.Adapter<MemeListCommentAdapter.CommentHolder> {
    private List<CommentModel> commentModel;
    private Context context;

    public static class CommentHolder extends RecyclerView.ViewHolder {
        public TextView dateStamp, commentText, userName, userIcon;
        /*public Button thumbsDown;
        public static Button thumbsUp;*/

        public CommentHolder(View view) {
            super(view);

            dateStamp = (TextView) view.findViewById(R.id.meme_list_comment_time);
            commentText = (TextView) view.findViewById(R.id.meme_list_comment_content);
            userName = (TextView) view.findViewById(R.id.meme_list_user_name);
           /* thumbsDown = (Button) view.findViewById(R.id.meme_list_thumbs_down);
            thumbsUp = (Button) view.findViewById(R.id.meme_list_thumbs_up);*/
            userIcon = (TextView) view.findViewById(R.id.memes_list_icon);
        }

        private void setButtonLike(Boolean liked){
            /*if(liked){
                thumbsUp.setBackgroundResource(R.drawable.ic_thumb_up_selected);
                //Toast.makeText(context, "Liked true", Toast.LENGTH_SHORT).show();
            } else {
                thumbsUp.setBackgroundResource(R.drawable.ic_thumb_up_unselected);
               // Toast.makeText(context, "Liked false", Toast.LENGTH_SHORT).show();
            }*/
        }


    }

    public MemeListCommentAdapter(Context context, List<CommentModel> models) {
        this.commentModel = models;
        this.context = context;
    }

    @Override
    public CommentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.meme_list_comment_item, parent, false);

        return new CommentHolder(view);
    }

    Boolean liked;
    @Override
    public void onBindViewHolder(final CommentHolder holder, final int position) {
        holder.setIsRecyclable(false);
        final CommentModel model = commentModel.get(position);
        final Animation myAnim = AnimationUtils.loadAnimation(context, R.anim.like_unlike);
        ButtonBounceInterpolator bounceInterpolator = new ButtonBounceInterpolator(0.2, 20);
        myAnim.setInterpolator(bounceInterpolator);
        final DatabaseReference addLike = FirebaseDatabase.getInstance().getReference("comments").child(model.getPicKey()).child(model.getCommentKey());
        final SharedPreferences pref = context.getSharedPreferences("DankMemes", Context.MODE_PRIVATE);
        final String userFaveId = pref.getString("userID", null);

        /*holder.thumbsUp.setTag(position);
        holder.thumbsUp.setId(position);*/
        /*holder.thumbsDown.setTag(position);
        holder.thumbsDown.setId(position);*/

        /*if (model.getLiked() != null){
            if(model.getLiked().containsKey(userFaveId)){
                holder.setButtonLike(true);
            } else {
                holder.setButtonLike(false);
            }
        }
        if (model.getHated() != null){
            if(model.getHated().containsKey(userFaveId)){
                holder.thumbsDown.setBackgroundResource(R.drawable.ic_thumb_down_selected);
            }
        }*/

        holder.userIcon.setText(model.getUserName().toString().substring(0, 1).toUpperCase());
        holder.dateStamp.setText(formattedTime(model.getTimeStamp()));
        holder.commentText.setText(model.getComment());
        holder.userName.setText(model.getUserName());

//        holder.thumbsUp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                context = v.getContext();
//                holder.thumbsUp.startAnimation(myAnim);
//                //holder.setButtonLike(true);
//                likeComment(addLike, userFaveId);
//
//
//            }
//
//        });

        /*holder.thumbsDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            context = v.getContext();

                holder.thumbsDown.startAnimation(myAnim);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        addLike.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.child("liked").hasChild(userFaveId)){
                                    addLike.child("liked").child(userFaveId).removeValue();
                                    updateLike(true, addLike, "hates", "likes");
                                    addLike.child("hated").child(userFaveId).setValue(true);
                                    holder.thumbsDown.setBackgroundResource(R.drawable.ic_thumb_down_selected);
                                    holder.thumbsUp.setBackgroundResource(R.drawable.ic_thumb_up_unselected);
                                } else if(dataSnapshot.child("hated").hasChild(userFaveId)){
                                    updateLike(false, addLike, "hates", "likes");
                                    addLike.child("hated").child(userFaveId).removeValue();
                                    holder.thumbsDown.setBackgroundResource(R.drawable.ic_thumb_down_unselected);
                                } else {
                                    updateLike(true, addLike, "hates", "likes");
                                    addLike.child("hated").child(userFaveId).setValue(true);
                                    holder.thumbsUp.setBackgroundResource(R.drawable.ic_thumb_down_selected);

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        notifyDataSetChanged();
                    }
                }, 1500);

            }
        });
*/


    }

    @Override
    public int getItemCount() {
        return commentModel.size();
    }

    private String formattedTime(long time){
        SimpleDateFormat detailFormat = new SimpleDateFormat("dd MMM, h:mm a");
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");

        Date today = new Date(System.currentTimeMillis());
        Date previous = new Date(time);

        if(previous.before(today)){
            return detailFormat.format(time);
        } else {
            return timeFormat.format(time);
        }

    }

    private void updateLike(final Boolean update, DatabaseReference ref, String child, String child2){
        ref.child(child).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() != null) {
                    int value = mutableData.getValue(Integer.class);
                    if(update) {
                        value++;
                    } else {
                        value--;
                    }
                    mutableData.setValue(value);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d("Adding Like: ", "likeTransaction:onComplete:" + databaseError);
            }
        });

        ref.child(child2).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() != null) {
                    int value = mutableData.getValue(Integer.class);
                    if(value!=0){
                        if(update) {
                            value--;
                        } else {
                            value++;
                        }
                        mutableData.setValue(value);
                    }

                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d("Adding Like: ", "likeTransaction:onComplete:" + databaseError);
            }
        });
    }

    private void likeComment(final DatabaseReference addLike,final String userFaveId){
        addLike.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("hated").hasChild(userFaveId)){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            addLike.child("hated").child(userFaveId).removeValue();
                            updateLike(true, addLike, "likes", "hates");
                            addLike.child("liked").child(userFaveId).setValue(true);
                        }
                    }, 1000);

                    //holder.thumbsDown.setBackgroundResource(R.drawable.ic_thumb_down_unselected);

                } else if(dataSnapshot.child("liked").hasChild(userFaveId)){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            updateLike(false, addLike, "likes", "hates");
                            addLike.child("liked").child(userFaveId).removeValue();
                        }
                    }, 1000);
                    //holder.setButtonLike(false);

                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            updateLike(true, addLike, "likes", "hates");
                            addLike.child("liked").child(userFaveId).setValue(true);
                        }
                    }, 1000);
                    //holder.setButtonLike(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //notifyItemChanged(position);
       // CommentHolder.thumbsUp.setBackgroundResource(R.drawable.ic_thumb_up_selected);
    }

}