package com.gelostech.dankmemes.activities;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gelostech.dankmemes.R;
import com.gelostech.dankmemes.adapters.MemeListAdapter;
import com.gelostech.dankmemes.commoners.ConnectivityReceiver;
import com.gelostech.dankmemes.commoners.FirebaseConstants;
import com.gelostech.dankmemes.commoners.MyApplication;
import com.gelostech.dankmemes.models.UploadModel;
import com.github.captain_miao.optroundcardview.OptRoundCardView;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MemesActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, ConnectivityReceiver.ConnectivityReceiverListener {
    private Toolbar toolbar;
    private RecyclerView rv;
    private LinearLayoutManager lm;
    private List<Object> modelList;
    private MemeListAdapter adapter;
    private Parcelable state;
    private ProgressBar pb;
    private ConnectivityReceiver connectivityReceiver;
    private RelativeLayout noInternet;
    private TextView noInternetText1;
    private SwipeRefreshLayout refreshLayout;
    private FloatingActionButton fab;
    public static final int ITEMS_PER_AD = 6;
    private int counter = 0;
    private InterstitialAd interstitialAd;
    private OptRoundCardView cardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memes);

        if (!isTaskRoot()) {
            finish();
            return;
        }

        if(Build.VERSION.SDK_INT >= 23){
            checkPermission();
        }

        toolbar = (Toolbar) findViewById(R.id.memes_activity_toolbar);
        setSupportActionBar(toolbar);

        connectivityReceiver = new ConnectivityReceiver();
        connectivityReceiver.setConnectivityReceiverListener(MemesActivity.this);

        rv = (RecyclerView) MemesActivity.this.findViewById(R.id.memes);
        rv.setHasFixedSize(true);
        RecyclerView.ItemAnimator animator = rv.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        lm = new LinearLayoutManager(MemesActivity.this);
        rv.setLayoutManager(lm);
        modelList = new ArrayList<>();

        pb = (ProgressBar) MemesActivity.this.findViewById(R.id.loading_memes);
        noInternet = (RelativeLayout) findViewById(R.id.memes_no_internet);
        noInternetText1 = (TextView) findViewById(R.id.memes_null_text);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.main_memes_refresh);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setRefreshing(false);
                    }
                }, 2500);
            }
        });

        if(fab.isShown()){
            rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    if(newState == RecyclerView.SCROLL_STATE_IDLE){
                        fab.show(true);
                    }

                    super.onScrollStateChanged(recyclerView, newState);

                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    if(dy > 0 || dy < 0 && fab.isShown())
                        fab.hide(true);
                }
            });
        }

        loadDummyData();
        numUsers();
        //loadMemes();

        adapter = new MemeListAdapter(MemesActivity.this, modelList);
        rv.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MemesActivity.this, UploadMemeActivity.class);
                startActivity(intent);
            }
        });

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad_id));
        interstitialAd.loadAd(new AdRequest.Builder().build());
        interstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                interstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_activity_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()){

            case R.id.select_option_faves:
                //Toast.makeText(this, "Take me to my faves!", Toast.LENGTH_SHORT).show();

                Intent favesIntent = new Intent(MemesActivity.this, FavoritesActivity.class);
                startActivity(favesIntent);
                overridePendingTransition(R.anim.enter_favorites, R.anim.exit_favorites);

                return true;

            case R.id.select_memes_date:
                //Toast.makeText(this, "Pick this memes date", Toast.LENGTH_SHORT).show();
                pickDate();
                return true;

            case R.id.select_settings:
                Intent settingsIntent = new Intent(MemesActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
                return true;

            case R.id.more_apps:
                Uri uri = Uri.parse(getResources().getString(R.string.play_store_link) + "com.gelostech.zoomsta");
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(getResources().getString(R.string.play_store_link) + "com.gelostech.zoomsta")));
                }
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private Calendar minDate(){
        final int year = 2017, month = 8, day = 21;
        final Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);

        return cal;
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

        String [] months = {"Jan", "Feb", "March", "April", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec"};
        String date = dayOfMonth + "/" + months[monthOfYear] + "/" + year;
        int month = monthOfYear + 1;
        String title;

        if(month<10){
            title = dayOfMonth + ".0" + month + "." + year;
        } else {
            title = dayOfMonth + "." + month + "." + year;
        }

        //Toast.makeText(getContext(), date, Toast.LENGTH_SHORT).show();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy");
        Date d = null;
        try {
            d = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long millis = d.getTime();

        date = year + "_" + String.valueOf(millis);

        Intent intent = new Intent(MemesActivity.this, DailyActivity.class);
        intent.putExtra("Date", String.valueOf(date));
        intent.putExtra("Title", title);
        startActivity(intent);
        overridePendingTransition(R.anim.enter_favorites, R.anim.exit_favorites);

        if(interstitialAd.isLoaded()){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    interstitialAd.show();
                }
            }, 700);
        }

    }

    private void pickDate(){
        Calendar cal = Calendar.getInstance();
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show(MemesActivity.this.getFragmentManager(), "Datepickerdialog");
        datePickerDialog.setMinDate(minDate());
        datePickerDialog.setThemeDark(true);

    }

    private void loadMemes(){
        pb.setVisibility(View.VISIBLE);
        DatabaseReference df = FirebaseDatabase.getInstance().getReference(FirebaseConstants.MEMES_PATH_UPLOADS +"/");
        //df.keepSynced(true);
        df.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                /*for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    if(postSnapshot.getChildren() != null){
                        try{

                            UploadModel memes = postSnapshot.getValue(UploadModel.class);
                            modelList.add(0, memes);


                            if(counter >= postSnapshot.getChildrenCount()){
                                //Toast.makeText(MemesActivity.this, "" + counter, Toast.LENGTH_SHORT).show();
                                *//*addNativeAds();
                                setUpAndLoadNativeExpressAds();*//*
                            }

                            if(!refreshLayout.isShown())
                                refreshLayout.setVisibility(View.VISIBLE);
                            if(pb.isShown())
                                pb.setVisibility(View.GONE);
                            adapter.notifyDataSetChanged();
                        } catch (Exception e){
                            e.printStackTrace();
                        }

                    } else {
                        Toast.makeText(MemesActivity.this, "No posts available!", Toast.LENGTH_LONG).show();
                    }

                }*/


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                /*UploadModel model = dataSnapshot.getValue(UploadModel.class);
                int index = modelList.indexOf(s);
                int newPos = index + 1;
                modelList.set(newPos, model);*/

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Meme_list_error: ", databaseError.getMessage());
            }
        });

        //Toast.makeText(this, "" + modelList.size(), Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onStart(){
        super.onStart();
        MyApplication.activityResumed();
        if(state!= null){
            lm.onRestoreInstanceState(state);
            adapter.notifyDataSetChanged();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            MemesActivity.this.registerReceiver(connectivityReceiver,
                    new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        MyApplication.activityResumed();
        //lm.onRestoreInstanceState(state);
        adapter.notifyDataSetChanged();
        //MemesActivity.this.registerReceiver(new ConnectivityReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            MemesActivity.this.registerReceiver(connectivityReceiver,
                    new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }

    }

    @Override
    protected void onPause(){
        super.onPause();
        MyApplication.activityPaused();
        state = lm.onSaveInstanceState();
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            MemesActivity.this.unregisterReceiver(new ConnectivityReceiver());
        }*/
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        MyApplication.activityPaused();
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            MemesActivity.this.unregisterReceiver(connectivityReceiver);
        }*/
    }

    @Override
    public void onBackPressed(){
        new AlertDialog.Builder(this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    private void checkPermission() {
        int result = ContextCompat.checkSelfPermission(MemesActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result != PackageManager.PERMISSION_GRANTED)
            requestPermission();
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(MemesActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(MemesActivity.this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MemesActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        if(isConnected){
            if(noInternet.isShown()){
                noInternet.setVisibility(View.GONE);
            }
            if(!modelList.isEmpty()){
                /*Snackbar s = Snackbar.make(rv, "Internet connection established", Snackbar.LENGTH_LONG);
                View view = s.getView();
                TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                tv.setTextColor(ContextCompat.getColor(MemesActivity.this, R.color.colorAccent));
                s.show();*/
            } else {
                pb.setVisibility(View.VISIBLE);
            }

        } else {
            pb.setVisibility(View.GONE);
            if(modelList.isEmpty()){
                noInternet.setVisibility(View.VISIBLE);
                noInternetText1.setText("No internet connection!");
            } else {
                Snackbar s = Snackbar.make(rv, "No internet connection!", Snackbar.LENGTH_LONG);
                View view = s.getView();
                TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                tv.setTextColor(ContextCompat.getColor(MemesActivity.this, R.color.colorAccent));
                s.show();
                //Toast.makeText(this, "No internet connection!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void addNativeAds(){
        //Toast.makeText(this, " " + modelList.size() , Toast.LENGTH_SHORT).show();
        for(int i=0; i<modelList.size(); i+=ITEMS_PER_AD){
            final NativeExpressAdView ad = new NativeExpressAdView(MemesActivity.this);
            modelList.add(i, ad);
            ad.setVisibility(View.GONE);
            //Toast.makeText(this, "Ad added at: " + i, Toast.LENGTH_SHORT).show();
        }
    }

    private void setUpAndLoadNativeExpressAds() {
        // Use a Runnable to ensure that the RecyclerView has been laid out before setting the
        // ad size for the Native Express ad. This allows us to set the Native Express ad's
        // width to match the full width of the RecyclerView.
        rv.post(new Runnable() {
            @Override
            public void run() {
                final float scale = MemesActivity.this.getResources().getDisplayMetrics().density;
                // Set the ad size and ad unit ID for each Native Express ad in the items list.
                for (int i = ITEMS_PER_AD; i < modelList.size(); i += ITEMS_PER_AD) {
                    final NativeExpressAdView adView = (NativeExpressAdView) modelList.get(i);
                    cardView = (OptRoundCardView) findViewById(R.id.memes_activity_ads);
                    //cardView.setVisibility(View.VISIBLE);
                    if(cardView != null){
                        final int adWidth = cardView.getWidth() - cardView.getPaddingLeft() - cardView.getPaddingRight();

                        AdSize adSize = new AdSize((int) (adWidth/scale), 320);
                        adView.setAdSize(adSize);
                        adView.setAdUnitId(getResources().getString(R.string.real_native_ad_id));

                        loadNativeExpressAd(ITEMS_PER_AD);
                    }

                }
            }
        });
    }

    private void loadNativeExpressAd(final int index) {

        if (index >= modelList.size()) {
            return;
        }

        Object item = modelList.get(index);
        if (!(item instanceof NativeExpressAdView)) {
            throw new ClassCastException("Expected item at index " + index + " to be a Native"
                    + " Express ad.");
        }

        final NativeExpressAdView adView = (NativeExpressAdView) item;

        // Set an AdListener on the NativeExpressAdView to wait for the previous Native Express ad
        // to finish loading before loading the next ad in the items list.
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                // The previous Native Express ad loaded successfully, call this method again to
                // load the next ad in the items list.
                adView.setVisibility(View.VISIBLE);
                cardView.setVisibility(View.VISIBLE);
                loadNativeExpressAd(index + ITEMS_PER_AD);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // The previous Native Express ad failed to load. Call this method again to load
                // the next ad in the items list.
                Log.e("MainActivity", "The previous Native Express ad failed to load. Attempting to"
                        + " load the next Native Express ad in the items list.");
                adView.setVisibility(View.GONE);
                loadNativeExpressAd(index + ITEMS_PER_AD);
            }
        });

        // Load the Native Express ad.
        AdRequest nativeAd = new AdRequest.Builder().build();
        adView.loadAd(nativeAd);

    }

    private void loadDummyData(){
        pb.setVisibility(View.VISIBLE);
        DatabaseReference df = FirebaseDatabase.getInstance().getReference(FirebaseConstants.MEMES_PATH_UPLOADS +"/");

        df.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    for(DataSnapshot s : postSnapshot.getChildren()){
                        if(s.getChildren() != null){
                            try{

                                UploadModel memes = s.getValue(UploadModel.class);
                                modelList.add(0, memes);
                                adapter.notifyDataSetChanged();

                                if(!refreshLayout.isShown())
                                    refreshLayout.setVisibility(View.VISIBLE);
                                if(pb.isShown())
                                    pb.setVisibility(View.GONE);
                            } catch (Exception e){
                                e.printStackTrace();
                            }

                        } else {
                            Toast.makeText(MemesActivity.this, "No posts available!", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                //Toast.makeText(MemesActivity.this, "" + modelList.size(), Toast.LENGTH_SHORT).show();
                addNativeAds();
                setUpAndLoadNativeExpressAds();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void numUsers(){
        DatabaseReference dr = FirebaseDatabase.getInstance().getReference("users");
        dr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Toast.makeText(MemesActivity.this, "" + dataSnapshot.getChildrenCount(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
