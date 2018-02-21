package com.gelostech.dankmemes.models;

/**
 * Created by tirgei on 6/25/17.
 */

public class FaveListModel {
    private String commentId;
    private String name;
    private String picUrl;
    private String faveKey;
    private String uploadDay;

    public FaveListModel(){}

    public FaveListModel(String name, String commentId, String picUrl, String faveKey, String uploadDay){
        this.commentId = commentId;
        this.name = name;
        this.picUrl = picUrl;
        this.faveKey = faveKey;
        this.uploadDay = uploadDay;
    }

    public String getUploadDay() {
        return uploadDay;
    }

    public void setUploadDay(String uploadDay) {
        this.uploadDay = uploadDay;
    }

    public String getFaveKey() {
        return faveKey;
    }

    public void setFaveKey(String faveKey) {
        this.faveKey = faveKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }
}
