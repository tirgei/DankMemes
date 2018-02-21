package com.gelostech.dankmemes.commoners;

import android.app.Application;

/**
 * Created by root on 9/8/17.
 */

public class MyApplication extends Application {

    private static Boolean activityVisible;

    public static boolean isActivityVisible(){
        return activityVisible;
    }

    public static void activityResumed(){
        activityVisible = true;
    }

    public static void activityPaused(){
        activityVisible = false;
    }

}