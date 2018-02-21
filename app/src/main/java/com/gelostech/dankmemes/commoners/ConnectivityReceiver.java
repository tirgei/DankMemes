package com.gelostech.dankmemes.commoners;

/**
 * Created by root on 9/8/17.
 */


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectivityReceiver extends BroadcastReceiver {
    public static ConnectivityReceiverListener listener = null;

    public ConnectivityReceiver(){
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try{
            boolean isVisible = MyApplication.isActivityVisible();

            if(isVisible){
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

                if(networkInfo != null && networkInfo.isConnected()){
                    if(listener != null)
                        listener.onNetworkChanged(true);
                } else {
                    if(listener != null)
                        listener.onNetworkChanged(false);
                }
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public interface ConnectivityReceiverListener{
        void onNetworkChanged(boolean isConnected);
    }

    public void setConnectivityReceiverListener(Context c){
        this.listener = (ConnectivityReceiverListener) c;
    }

}
