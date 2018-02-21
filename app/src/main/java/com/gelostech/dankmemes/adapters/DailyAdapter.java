package com.gelostech.dankmemes.adapters;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cocosw.bottomsheet.BottomSheet;
import com.gelostech.dankmemes.R;
import com.gelostech.dankmemes.activities.DailyActivity;
import com.gelostech.dankmemes.activities.DailyActivity;
import com.gelostech.dankmemes.activities.MemesActivity;
import com.gelostech.dankmemes.commoners.ButtonBounceInterpolator;
import com.gelostech.dankmemes.commoners.FirebaseConstants;
import com.gelostech.dankmemes.models.CommentModel;
import com.gelostech.dankmemes.models.FaveListModel;
import com.gelostech.dankmemes.models.ReportImageModel;
import com.gelostech.dankmemes.models.UploadModel;
import com.gelostech.dankmemes.models.UserModel;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 8/21/17.
 */

public class DailyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ActivityCompat.OnRequestPermissionsResultCallback{
    private Context context;
    private List<Object> models;
    private DatabaseReference dr, f;
    private PopupWindow popWindow;
    private EditText getComment;
    private static final int NATIVE_AD = 0, MEME = 1;

    public static class DailyViewHolder extends RecyclerView.ViewHolder{
        public ImageView imageView, likeIcon, faveIcon;
        public TextView likeText, faveText;
        public Button moreOptions, comment;
        private ProgressBar pb;
        public LinearLayout likePost, commentPost, favePost;
        public TextView likesCount, commentCount, memesDot;
        public RelativeLayout memesNumbers;

        public DailyViewHolder(View view) {
            super(view);

            imageView = (ImageView) view.findViewById(R.id.day_memes);
            moreOptions = (Button) view.findViewById(R.id.day_memes_list_more_options);
            comment = (Button) view.findViewById(R.id.day_memes_list_comment);
            pb= (ProgressBar) view.findViewById(R.id.day_loading_into_imageview);
            likePost = (LinearLayout) view.findViewById(R.id.day_memes_list_like_post);
            likeIcon = (ImageView) view.findViewById(R.id.day_memes_list_like_icon);
            likeText = (TextView) view.findViewById(R.id.day_memes_list_like_text);
            commentPost = (LinearLayout) view.findViewById(R.id.day_memes_list_comment_post);
            favePost = (LinearLayout) view.findViewById(R.id.day_memes_list_fave_post);
            faveIcon = (ImageView) view.findViewById(R.id.day_memes_list_fave_icon);
            faveText = (TextView) view.findViewById(R.id.day_memes_list_fave_text);
            likesCount = (TextView) view.findViewById(R.id.day_memes_likes_count);
            commentCount = (TextView) view.findViewById(R.id.day_memes_comments_count);
            memesNumbers = (RelativeLayout) view.findViewById(R.id.day_memes_numbers);
            memesDot = (TextView) view.findViewById(R.id.day_memes_dot);

        }
    }

    public static class NativeAdsDailyHolder extends RecyclerView.ViewHolder{

        public NativeAdsDailyHolder(View itemView) {
            super(itemView);
        }
    }

    public DailyAdapter(Context context, List<Object> modelList){
        this.context = context;
        this.models = modelList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if(viewType == NATIVE_AD) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.memes_activity_native_ads, parent, false);
            return new NativeAdsDailyHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.meme_date_card, parent, false);
            return new DailyViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        int viewType =getItemViewType(position);

        switch (viewType){
            case MEME:
                final DailyViewHolder holder = (DailyViewHolder) viewHolder;

                holder.favePost.setTag(position);
                holder.imageView.setImageDrawable(null);
                holder.likeIcon.setColorFilter(ContextCompat.getColor(context, R.color.secondaryText));
                holder.likeText.setTextColor(ContextCompat.getColor(context, R.color.secondaryText));
                holder.faveIcon.setColorFilter(ContextCompat.getColor(context, R.color.secondaryText));
                holder.faveText.setTextColor(ContextCompat.getColor(context, R.color.secondaryText));
                holder.commentCount.setText(null);
                holder.likesCount.setText(null);
                holder.memesDot.setVisibility(View.GONE);
                holder.memesNumbers.setVisibility(View.GONE);

                final UploadModel model = (UploadModel) models.get(position);
                //holder.setIsRecyclable(false);
                final SharedPreferences pref = context.getSharedPreferences("DankMemes", Context.MODE_PRIVATE);
                final String userFaveId = pref.getString("userID", null);
                final Animation myAnim = AnimationUtils.loadAnimation(context, R.anim.like_unlike);
                ButtonBounceInterpolator bounceInterpolator = new ButtonBounceInterpolator(0.2, 20);
                myAnim.setInterpolator(bounceInterpolator);

                Picasso.with(context)
                        .load(model.getUrl())
                        .placeholder(R.drawable.blank)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(holder.imageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                holder.pb.setVisibility(View.GONE);

                                if (model.getHasFaved() != null){
                                    if(model.getHasFaved().containsKey(userFaveId)){
                                        holder.faveIcon.setColorFilter(ContextCompat.getColor(context, R.color.gold));
                                        holder.faveText.setTextColor(ContextCompat.getColor(context, R.color.gold));
                                    }

                                }

                                if (model.getHasLiked() != null){
                                    if(model.getHasLiked().containsKey(userFaveId)){
                                        holder.likeIcon.setColorFilter(ContextCompat.getColor(context, R.color.colorAccent));
                                        holder.likeText.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                                    }


                                }

                                if(model.getCommentCount() > 0){
                                    holder.memesNumbers.setVisibility(View.VISIBLE);
                                    holder.commentCount.setVisibility(View.VISIBLE);
                                    if(model.getCommentCount() == 1){
                                        holder.commentCount.setText(String.valueOf(model.getCommentCount()) + " comment");
                                    } else {
                                        holder.commentCount.setText(String.valueOf(model.getCommentCount()) + " comments");
                                    }
                                }

                                if(model.getNumLikes() > 0){
                                    holder.memesNumbers.setVisibility(View.VISIBLE);
                                    holder.likesCount.setVisibility(View.VISIBLE);
                                    if(model.getNumLikes() > 1){
                                        holder.likesCount.setText(String.valueOf(model.getNumLikes()) + " likes");
                                    } else {
                                        holder.likesCount.setText(String.valueOf(model.getNumLikes()) + " like");
                                    }

                                }

                                if(model.getCommentCount() > 0 && model.getNumLikes() > 0){
                                    holder.memesDot.setVisibility(View.VISIBLE);
                                }

                            }

                            @Override
                            public void onError() {
                                holder.pb.setVisibility(View.VISIBLE);
                                holder.pb.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(context, R.color.dividerColor), android.graphics.PorterDuff.Mode.MULTIPLY);
                                
                                Picasso.with(context)
                                        .load(((UploadModel) models.get(position)).getUrl())
                                        .placeholder(R.drawable.blank)
                                        .into(holder.imageView, new Callback() {
                                            @Override
                                            public void onSuccess() {
                                                if(holder.pb.isShown()){
                                                    holder.pb.setVisibility(View.GONE);
                                                }
                                            }

                                            @Override
                                            public void onError() {

                                            }
                                        });

                            }
                        });

                holder.likePost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.likePost.startAnimation(myAnim);
                        f = FirebaseDatabase.getInstance().getReference(FirebaseConstants.MEMES_PATH_UPLOADS).child(model.getUploadDay()).child(model.getPicKey());

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                f.runTransaction(new Transaction.Handler() {
                                    @Override
                                    public Transaction.Result doTransaction(MutableData mutableData) {
                                        UploadModel u = mutableData.getValue(UploadModel.class);

                                        if(u == null){
                                            return Transaction.success(mutableData);
                                        }

                                        if(u.hasLiked.containsKey(userFaveId)){
                                            u.numLikes = u.numLikes - 1;
                                            u.getHasLiked().remove(userFaveId);
                                        } else {
                                            u.numLikes = u.numLikes + 1;
                                            u.hasLiked.put(userFaveId, true);
                                        }

                                        mutableData.setValue(u);

                                        return Transaction.success(mutableData);
                                    }

                                    @Override
                                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                        Log.d("Liking item: ", "postTransaction:onComplete:" + databaseError);

                                        int likes = (Integer.valueOf(dataSnapshot.child("numLikes").getValue().toString()));
                                        int comments = (Integer.valueOf(dataSnapshot.child("commentCount").getValue().toString()));

                                        if(dataSnapshot.child("hasLiked").hasChild(userFaveId)){
                                            //Toast.makeText(context, "Has been liked", Toast.LENGTH_SHORT).show();
                                            holder.likeIcon.setColorFilter(ContextCompat.getColor(context, R.color.colorAccent));
                                            holder.likeText.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));

                                        } else {
                                            //Toast.makeText(context, "Has removed liked", Toast.LENGTH_SHORT).show();
                                            holder.likeIcon.setColorFilter(ContextCompat.getColor(context, R.color.secondaryText));
                                            holder.likeText.setTextColor(ContextCompat.getColor(context, R.color.secondaryText));

                                        }

                                        if(likes > 0){
                                            holder.memesNumbers.setVisibility(View.VISIBLE);
                                            holder.likesCount.setVisibility(View.VISIBLE);

                                            if(likes > 1){
                                                holder.likesCount.setText(likes + " likes");
                                            } else if(likes == 1){
                                                holder.likesCount.setText(likes + " like");
                                            }

                                            if(comments > 0){
                                                holder.memesDot.setVisibility(View.VISIBLE);
                                                holder.commentCount.setVisibility(View.VISIBLE);
                                            }

                                        } else if(comments > 0) {
                                            holder.likesCount.setVisibility(View.GONE);
                                            holder.memesDot.setVisibility(View.GONE);
                                        }

                                    }
                                });
                            }
                        }, 1000);

                        //notifyItemChanged(position);


                    }
                });

                holder.favePost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        holder.favePost.startAnimation(myAnim);

                        f = FirebaseDatabase.getInstance().getReference(FirebaseConstants.MEMES_PATH_UPLOADS).child(model.getUploadDay()).child(model.getPicKey());

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                f.runTransaction(new Transaction.Handler() {
                                    @Override
                                    public Transaction.Result doTransaction(MutableData mutableData) {
                                        UploadModel u = mutableData.getValue(UploadModel.class);

                                        if(u == null){
                                            return Transaction.success(mutableData);
                                        }

                                        if(u.hasFaved.containsKey(userFaveId)){
                                            u.favesCount = u.favesCount - 1;
                                            u.getHasFaved().remove(userFaveId);

                                        } else {
                                            u.favesCount = u.favesCount + 1;
                                            u.hasFaved.put(userFaveId, true);
                                        }

                                        mutableData.setValue(u);

                                        return Transaction.success(mutableData);
                                    }

                                    @Override
                                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                        Log.d("Faving item: ", "postTransaction:onComplete:" + databaseError);

                                        if(dataSnapshot.child("hasFaved").hasChild(userFaveId)){
                                            //Toast.makeText(context, "Has been faved", Toast.LENGTH_SHORT).show();
                                            holder.faveIcon.setColorFilter(ContextCompat.getColor(context, R.color.gold));
                                            holder.faveText.setTextColor(ContextCompat.getColor(context, R.color.gold));

                                            dr = FirebaseDatabase.getInstance().getReference("favorites").child(userFaveId);
                                            FaveListModel faveModel = new FaveListModel(model.getName(), model.getPicKey(), model.getUrl(), model.getPicKey(), model.getUploadDay());
                                            dr.child(model.getPicKey()).setValue(faveModel);

                                        } else {
                                            //Toast.makeText(context, "Has removed faved", Toast.LENGTH_SHORT).show();
                                            holder.faveIcon.setColorFilter(ContextCompat.getColor(context, R.color.secondaryText));
                                            holder.faveText.setTextColor(ContextCompat.getColor(context, R.color.secondaryText));

                                            dr = FirebaseDatabase.getInstance().getReference("favorites").child(userFaveId);
                                            dr.child(model.getPicKey()).removeValue();

                                        }

                                    }
                                });
                            }
                        }, 1000);

                        //notifyItemChanged(position);

                    }
                });

                holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Context c = v.getContext();
                        bottomSheet(c, model);

                        return true;
                    }
                });

                holder.moreOptions.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Context c = v.getContext();
                        bottomSheet(c, model);

                    }
                });

                holder.commentPost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        context = v.getContext();
                        onShowPopup(model.getPicKey(), model.getUploadDay());
                    }
                });

                break;

            case NATIVE_AD:

            default:
                final NativeAdsDailyHolder adViewHolder = (NativeAdsDailyHolder) viewHolder;
                try{
                    NativeExpressAdView adView = (NativeExpressAdView) models.get(position);
                    ViewGroup cardView = (ViewGroup) adViewHolder.itemView;

                    if(cardView.getChildCount() >0){
                        cardView.removeAllViews();
                    }
                    if(adView.getParent() != null){
                        ((ViewGroup) adView.getParent()).removeView(adView);
                    }
                    cardView.addView(adView);

                } catch (ClassCastException e){
                    Log.e("Class cast exception: ", "position = " + position );
                }



        }

    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(position % DailyActivity.ITEMS_PER_AD == 0)
            return NATIVE_AD;
        else
            return MEME;
    }

    private void bottomSheet(Context c,final UploadModel model){
        new BottomSheet.Builder((DailyActivity)c)
                //.title(getString(R.string.more_options))
                .sheet(R.menu.daily_bottomsheet)
                .darkTheme()
                .listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){

                            case R.id.daily_save:
                                checkVersion(model.getUrl());
                                break;

                            case R.id.daily_share:
                                shareImage(model.getUrl());
                                //Toast.makeText(context, "Share this image", Toast.LENGTH_SHORT).show();
                                break;


                            case R.id.daily_report:
                                reportImage(model.getUrl(), model.getUploadDay(), model.getPicKey());
                                break;
                        }
                    }
                }).show();
    }

    private void checkVersion(String url){
        if (Build.VERSION.SDK_INT >= 23)
        {
            if (checkPermission())
            {
                downloadPic(url);
            } else {
                requestPermission(); // Code for permission
            }
        }
        else
        {
            downloadPic(url);
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(context, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                    //downloadPic(imageUrl);
                    Toast.makeText(context, "Permission granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }


    private void downloadPic(String url){
        Toast.makeText(context, "Downloading...", Toast.LENGTH_SHORT).show();
        File direct = new File(Environment.getExternalStorageDirectory() + "/DankMemes");

        if(!direct.exists()){
            direct.mkdirs();
        }

        DownloadManager mgr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri downloadLink = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(downloadLink);

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false).setTitle(context.getResources().getString(R.string.app_name))
                .setDestinationInExternalPublicDir("/DankMemes", "DankMeme-" + System.currentTimeMillis() + ".jpg" );

        mgr.enqueue(request);

    }

    private void shareImage(String url){
        Glide.with(context).asBitmap().load(url).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("image/*");
                i.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(resource));
                context.startActivity(Intent.createChooser(i, "Share Meme..."));
            }
        });
    }

    private Uri getLocalBitmapUri(Bitmap bitmap){
        Uri bitmp = null;

        try{
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_meme_ " + System.currentTimeMillis() + ".jpg");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            bitmp = Uri.fromFile(file);
        } catch (IOException e){
            e.printStackTrace();
        }

        return bitmp;
    }

    private void reportImage(final String url, final String date, final String key){
        dr = FirebaseDatabase.getInstance().getReference("reports");

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = ((DailyActivity) context).getLayoutInflater();
        final  View dialogView  = inflater.inflate(R.layout.custom_dialog, null);
        builder.setView(dialogView);
        final EditText text = (EditText) dialogView.findViewById(R.id.report_reason);
        builder.setTitle("Report Image");
        builder.setPositiveButton("Report", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int whichButton){


                String reason;
                String uploadId = dr.push().getKey();

                if(TextUtils.isEmpty(text.getText().toString())){
                    reason = "";
                } else {
                    reason = text.getText().toString();
                }
                ReportImageModel reportModel = new ReportImageModel(url, reason, key, date);
                dr.child(uploadId).setValue(reportModel);
                Toast.makeText(context, "Image Reported!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int whichButton){

            }
        });

        final AlertDialog d = builder.create();
        d.show();
    }

    public void onShowPopup(final String key, final String uploadDay){

        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // inflate the custom popup layout
        View v = layoutInflater.inflate(R.layout.popup_layout, null, false);
        // get device size
        Display display = ((DailyActivity)context).getWindowManager().getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
//        mDeviceHeight = size.y;
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        // fill the data to the list items
        //setSimpleList(listView);
        ProgressBar pb = (ProgressBar)v.findViewById(R.id.loading_comments);
        pb.setVisibility(View.VISIBLE);
        final List<CommentModel> modelList = new ArrayList<>();
        final MemeListCommentAdapter commentAdapter = new MemeListCommentAdapter(context, modelList);
        final RecyclerView listView = (RecyclerView) v.findViewById(R.id.commentsListView);
        listView.setHasFixedSize(true);
        listView.setItemAnimator(null);
        RecyclerView.ItemAnimator animator = listView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        listView.setLayoutManager(new LinearLayoutManager(context));
        listView.setAdapter(commentAdapter);

        checkIfComments(key, pb);
        fillComments(key, pb, modelList, listView, commentAdapter);
        getComment = (EditText) v.findViewById(R.id.meme_list_enter_comment);
        Button sendComment = (Button) v.findViewById(R.id.meme_list_comment_send);

        sendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = PreferenceManager.getDefaultSharedPreferences(context).getString("user_name", null);
                if (userName == null) {
                    addUserName(key, uploadDay);
                } else {
                    if (TextUtils.isEmpty(getComment.getText().toString())) {
                        Toast.makeText(context, "Please enter a comment!", Toast.LENGTH_SHORT).show();
                    } else {
                        newComments(key, uploadDay);
                        commentAdapter.notifyDataSetChanged();
                        listView.smoothScrollToPosition(modelList.size());
                    }
                }

            }
        });

        // set height depends on the device size
        popWindow = new PopupWindow(v, width,height-50, true );
        // set a background drawable with rounders corners
        popWindow.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.popup_comments));

        popWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        popWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        //popWindow.setAnimationStyle(R.style.PopupAnimation);

        // show the popup at bottom of the screen and set some margin at bottom ie,
        popWindow.showAtLocation(v, Gravity.BOTTOM, 0,100);
    }

    private void checkIfComments(String key, final ProgressBar pb){
        DatabaseReference dr = FirebaseDatabase.getInstance().getReference("comments");

        dr.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(context, "No comments available!", Toast.LENGTH_LONG).show();
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

    private void fillComments(String key,final ProgressBar pb,final List<CommentModel> modelList, final RecyclerView listView, final MemeListCommentAdapter adapter){
        DatabaseReference dr = FirebaseDatabase.getInstance().getReference("comments");

        dr.child(key).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                CommentModel comments = dataSnapshot.getValue(CommentModel.class);
                modelList.add(comments);
                adapter.notifyDataSetChanged();
                listView.setVisibility(View.VISIBLE);
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
                pb.setVisibility(View.GONE);
                Log.d("Meme_list_comments: ", databaseError.getMessage());
            }
        });


    }

    private void addUserName(final String key, final String uploadDay){
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = ((DailyActivity)context).getLayoutInflater();
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
                    Toast.makeText(context, "Please enter name", Toast.LENGTH_LONG).show();
                } else {
                    SharedPreferences pref = context.getSharedPreferences("DankMemes", Context.MODE_PRIVATE);
                    String userFaveId = pref.getString("userID", null);

                    UserModel model = new UserModel();
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor prefEditor = preferences.edit();
                    prefEditor.putString("user_name", text.getText().toString());
                    prefEditor.apply();

                    model.setUserId(userFaveId);
                    model.setUserName(text.getText().toString());

                    DatabaseReference dr = FirebaseDatabase.getInstance().getReference("users").child(userFaveId);
                    dr.setValue(model);

                    newComments(key, uploadDay);
                    Toast.makeText(context, "Comment sent!", Toast.LENGTH_LONG).show();

                }
            }
        });
        builder.setNegativeButton(null, null);

        final AlertDialog d = builder.create();
        d.show();

    }

    private void newComments(String key, String uploadDay){
        DatabaseReference dr = FirebaseDatabase.getInstance().getReference("comments").child(key);
        DatabaseReference f = FirebaseDatabase.getInstance().getReference(FirebaseConstants.MEMES_PATH_UPLOADS).child(uploadDay).child(key);
        String userName = PreferenceManager.getDefaultSharedPreferences(context).getString("user_name", null);

        f.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                UploadModel u = mutableData.getValue(UploadModel.class);

                if (u == null) {
                    return Transaction.success(mutableData);
                }

                u.commentCount = u.commentCount + 1;

                mutableData.setValue(u);

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });

        String uploadId = dr.push().getKey();
        CommentModel myComment = new CommentModel(userName, System.currentTimeMillis(), getComment.getText().toString(), key, uploadId);
        dr.child(uploadId).setValue(myComment);

        getComment.setText(null);

    }
}
