package com.gelostech.dankmemes.models;

import java.util.Map;

/**
 * Created by root on 6/28/17.
 */

public class CommentModel {
    private String userName, comment;
    private Long timeStamp;
    private int likes, hates;
    private String picKey, commentKey;
    private Map<String, Boolean> liked;
    private Map<String, Boolean> hated;

    public CommentModel(){}

    public CommentModel(String userName, Long timeStamp, String comment, String picKey, String commentKey){
        this.userName = userName;
        this.timeStamp = timeStamp;
        this.comment = comment;
        this.commentKey = commentKey;
        this.picKey = picKey;

    }

    public Map<String, Boolean> getLiked() {
        return liked;
    }

    public void setLiked(Map<String, Boolean> liked) {
        this.liked = liked;
    }

    public Map<String, Boolean> getHated() {
        return hated;
    }

    public void setHated(Map<String, Boolean> hated) {
        this.hated = hated;
    }

    public String getPicKey() {
        return picKey;
    }

    public void setPicKey(String picKey) {
        this.picKey = picKey;
    }

    public String getCommentKey() {
        return commentKey;
    }

    public void setCommentKey(String commentKey) {
        this.commentKey = commentKey;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getHates() {
        return hates;
    }

    public void setHates(int hates) {
        this.hates = hates;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
