package com.gelostech.dankmemes.models;

/**
 * Created by tirgei on 7/7/17.
 */

public class UserModel {
    private String userName, userId;

    public UserModel(){}

    public UserModel(String userName, String userId){
        this.userName = userName;

        this.userId = userId;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
