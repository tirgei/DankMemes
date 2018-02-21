package com.gelostech.dankmemes.models;

/**
 * Created by tirgei on 7/8/17.
 */

public class ReportImageModel {
    private String picUrl;
    private String reason;
    private String picId;
    private  String picDate;

    public ReportImageModel(){}

    public ReportImageModel(String picUrl, String reason, String picId, String picDate){
        this.picUrl = picUrl;
        this.reason = reason;
        this.picId = picId;
        this.picDate = picDate;
    }

    public String getPicDate() {
        return picDate;
    }

    public void setPicDate(String picDate) {
        this.picDate = picDate;
    }

    public String getPicId() {
        return picId;
    }

    public void setPicId(String picId) {
        this.picId = picId;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
