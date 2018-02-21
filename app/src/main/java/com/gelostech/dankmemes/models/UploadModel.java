package com.gelostech.dankmemes.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tirgei on 7/5/17.
 */

public class UploadModel {
    private String picKey;
    private String name;
    private String url;
    private String day;
    private long time;
    public int favesCount = 0;
    public int commentCount = 0;
    public int numLikes = 0;
    private String uploadDay;
    public Map<String, Boolean> hasFaved = new HashMap<>();
    public Map<String, Boolean> hasLiked = new HashMap<>();

    public UploadModel(){}

    public UploadModel(String picKey, String name, String url, String uploadDay){
        this.picKey = picKey;
        this.name = name;
        this.url = url;
        this.uploadDay = uploadDay;
    }

    public int getNumLikes() {
        return numLikes;
    }

    public void setNumLikes(int numLikes) {
        this.numLikes = numLikes;
    }

    public int getFavesCount() {
        return favesCount;
    }

    public void setFavesCount(int favesCount) {
        this.favesCount = favesCount;
    }

    public Map<String, Boolean> getHasLiked() {
        return hasLiked;
    }

    public void setHasLiked(Map<String, Boolean> hasLiked) {
        this.hasLiked = hasLiked;
    }

    public Map<String, Boolean> getHasFaved() {
        return hasFaved;
    }

    public void setHasFaved(Map<String, Boolean> hasFaved) {
        this.hasFaved = hasFaved;
    }

    public String getUploadDay() {
        return uploadDay;
    }

    public void setUploadDay(String uploadDay) {
        this.uploadDay = uploadDay;
    }


    public String getPicKey() {
        return picKey;
    }

    public void setPicKey(String picKey) {
        this.picKey = picKey;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDay() {
        return day;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public void setDay(String day) {
        this.day = day;
    }

}
