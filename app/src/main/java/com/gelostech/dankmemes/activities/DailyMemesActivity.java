package com.gelostech.dankmemes.activities;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.gelostech.dankmemes.R;
import com.gelostech.dankmemes.fragments.DailyMemesFragment;
import com.gelostech.dankmemes.fragments.FavesItemFragment;

public class DailyMemesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_memes);

        FragmentManager fm = this.getSupportFragmentManager();
        FragmentTransaction ft =fm.beginTransaction();

        DailyMemesFragment dmf = new DailyMemesFragment();
        ft.add(R.id.daily_memes_holder, dmf);
        ft.commit();

    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
            finish();
        }
        else {
            getSupportFragmentManager().popBackStack();
        }
    }

}
