package com.gelostech.dankmemes.services;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by root on 7/15/17.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
private static final String TAG = "MyFirebaseIDService";

    @Override
    public void onTokenRefresh(){
        String tokenRefresh = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refresh token: " + tokenRefresh);
        sendRegistrationToServer(tokenRefresh);

    }

    private void sendRegistrationToServer(String token){

    }

}
