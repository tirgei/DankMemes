package com.gelostech.dankmemes.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gelostech.dankmemes.R;
import com.gelostech.dankmemes.models.CommentModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by root on 6/28/17.
 */

public class DailyCommentAdapter extends RecyclerView.Adapter<DailyCommentAdapter.CommentHolder> {
    private List<CommentModel> commentModel;
    private Context context;

    public class CommentHolder extends RecyclerView.ViewHolder {
        public TextView dateStamp, commentText, userName;
        public Button thumbsDown, thumbsUp;

        public CommentHolder(View view) {
            super(view);

            dateStamp = (TextView) view.findViewById(R.id.daily_comment_time);
            commentText = (TextView) view.findViewById(R.id.daily_comment_content);
            userName = (TextView) view.findViewById(R.id.daily_user_name);
            thumbsDown = (Button) view.findViewById(R.id.daily_thumbs_down);
            thumbsUp = (Button) view.findViewById(R.id.daily_thumbs_up);
        }

    }

    public DailyCommentAdapter(List<CommentModel> models) {
        this.commentModel = models;
    }

    @Override
    public CommentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.daily_comment_item, parent, false);

        return new CommentHolder(view);
    }

    @Override
    public void onBindViewHolder(CommentHolder holder, int position) {
        CommentModel model = commentModel.get(position);
        holder.dateStamp.setText(formattedTime(model.getTimeStamp()));
        holder.commentText.setText(model.getComment());
        holder.userName.setText(model.getUserName());

        holder.thumbsUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context = v.getContext();
                Toast.makeText(context, "Like clicked!", Toast.LENGTH_SHORT).show();
            }
        });

        holder.thumbsDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context = v.getContext();
                Toast.makeText(context, "Lame clicked!", Toast.LENGTH_SHORT).show();
            }
        });

        //notifyDataSetChanged();
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

}