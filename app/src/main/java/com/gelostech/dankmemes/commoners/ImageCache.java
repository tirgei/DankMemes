package com.gelostech.dankmemes.commoners;

import android.app.Application;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

/**
 * Created by root on 9/18/17.
 */

public class ImageCache extends Application {

    @Override
    public void onCreate(){
        super.onCreate();

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso image = builder.build();
        //image.setIndicatorsEnabled(true);
        image.setLoggingEnabled(true);
        Picasso.setSingletonInstance(image);
    }

}
