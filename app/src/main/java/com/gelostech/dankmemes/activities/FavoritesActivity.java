package com.gelostech.dankmemes.activities;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.gelostech.dankmemes.R;
import com.gelostech.dankmemes.commoners.ConnectivityReceiver;
import com.gelostech.dankmemes.commoners.MyApplication;
import com.gelostech.dankmemes.fragments.FavoritesListFragment;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

public class FavoritesActivity extends AppCompatActivity {
    private ConnectivityReceiver connectivityReceiver;
    private AdView adView;
    private InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        connectivityReceiver = new ConnectivityReceiver();
        adView = (AdView) findViewById(R.id.faves_activity_banner);
        adView.loadAd(new AdRequest.Builder().build());
        adView.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adView.setVisibility(View.VISIBLE);
            }
        });

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad_id));
        interstitialAd.loadAd(new AdRequest.Builder().build());
        interstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                interstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });

        if (savedInstanceState == null) {
            FavoritesListFragment f1= new FavoritesListFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.faves_fragments_holder, f1);
            fragmentTransaction.commit();

        }

    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
            finish();
            overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);

            if(interstitialAd.isLoaded()){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        interstitialAd.show();
                    }
                }, 700);
            }

        }
        else {
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FavoritesActivity.this.registerReceiver(connectivityReceiver,
                    new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }

        MyApplication.activityResumed();
    }

    @Override
    public void onPause(){
        super.onPause();
        MyApplication.activityPaused();
    }

    @Override
    public void onResume(){
        super.onResume();
        MyApplication.activityResumed();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FavoritesActivity.this.unregisterReceiver(connectivityReceiver);
        }
    }

}
