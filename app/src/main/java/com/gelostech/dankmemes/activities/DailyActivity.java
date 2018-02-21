package com.gelostech.dankmemes.activities;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gelostech.dankmemes.R;
import com.gelostech.dankmemes.adapters.DailyAdapter;
import com.gelostech.dankmemes.commoners.ConnectivityReceiver;
import com.gelostech.dankmemes.commoners.FirebaseConstants;
import com.gelostech.dankmemes.commoners.MyApplication;
import com.gelostech.dankmemes.models.UploadModel;
import com.github.captain_miao.optroundcardview.OptRoundCardView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DailyActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener{
    private Toolbar toolbar;
    private RecyclerView rv;
    private LinearLayoutManager lm;
    private List<Object> modelList;
    private DailyAdapter adapter;
    private Parcelable state;
    private ProgressBar pb;
    private String date, title;
    private RelativeLayout noMemes;
    private TextView noMemesText;
    private ConnectivityReceiver receiver;
    private ValueEventListener eventListener;
    private ChildEventListener childListener;
    public static final int ITEMS_PER_AD = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily);
        date = DailyActivity.this.getIntent().getExtras().getString("Date");
        title = DailyActivity.this.getIntent().getExtras().getString("Title");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            DailyActivity.this.registerReceiver(new ConnectivityReceiver(),
                    new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }

        toolbar = (Toolbar) findViewById(R.id.daily_activity_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(title);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
            }
        });

        receiver = new ConnectivityReceiver();
        receiver.setConnectivityReceiverListener(DailyActivity.this);

        rv = (RecyclerView) DailyActivity.this.findViewById(R.id.daily_memes_rv);
        rv.setHasFixedSize(true);
        lm = new LinearLayoutManager(DailyActivity.this);
        rv.setLayoutManager(lm);
        modelList = new ArrayList<>();
        adapter = new DailyAdapter(DailyActivity.this, modelList);
        rv.setAdapter(adapter);
        pb = (ProgressBar) DailyActivity.this.findViewById(R.id.loading_daily_memes);
        noMemes = (RelativeLayout) findViewById(R.id.daily_post_not_found);
        noMemesText = (TextView) findViewById(R.id.no_daily_memes_text);

        checkIfMemes();
        fetchMemes();

    }

    private void checkIfMemes(){
        DatabaseReference dr = FirebaseDatabase.getInstance().getReference(FirebaseConstants.MEMES_PATH_UPLOADS + "/").child(date);

        eventListener = dr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    pb.setVisibility(View.GONE);
                    noMemes.setVisibility(View.VISIBLE);
                    String text = "No memes found!";
                    noMemesText.setText(text);
                    //Toast.makeText(DailyActivity.this, "No memes added today!", Toast.LENGTH_LONG).show();
                } else {
                    //fillComments();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Meme List comments: ", "Error checking if comments available");
            }
        });

    }

    private void loadMemes(){
        DatabaseReference dr = FirebaseDatabase.getInstance().getReference(FirebaseConstants.MEMES_PATH_UPLOADS + "/");
        dr.child(date).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                UploadModel model = dataSnapshot.getValue(UploadModel.class);
                modelList.add(0, model);
                rv.setVisibility(View.VISIBLE);
                pb.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();

                Toast.makeText(DailyActivity.this, "" + modelList.size(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
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
                Log.d("Daily memes error: ", databaseError.getMessage());
            }


        });

    }

    private void fetchMemes(){
        DatabaseReference dr = FirebaseDatabase.getInstance().getReference(FirebaseConstants.MEMES_PATH_UPLOADS + "/");
        dr.child(date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    UploadModel model = ds.getValue(UploadModel.class);
                    modelList.add(0, model);
                    rv.setVisibility(View.VISIBLE);
                    pb.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }

                //Toast.makeText(DailyActivity.this, "" + modelList.size(), Toast.LENGTH_SHORT).show();
                addNativeAds();
                setUpAndLoadNativeExpressAds();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Daily memes error: ", databaseError.getMessage());
            }
        });

    }

    @Override
    protected void onStart(){
        super.onStart();
        MyApplication.activityResumed();
        if(state!= null){
            lm.onRestoreInstanceState(state);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        lm.onRestoreInstanceState(state);
        MyApplication.activityResumed();

    }

    @Override
    protected void onPause(){
        super.onPause();
        state = lm.onSaveInstanceState();
        MyApplication.activityPaused();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        if(isConnected){
            if(noMemes.isShown()){
                noMemes.setVisibility(View.GONE);
            }

        } else{
            pb.setVisibility(View.GONE);
            if(modelList.isEmpty()){
                noMemes.setVisibility(View.VISIBLE);
                noMemesText.setText("No internet connection!");
            } else
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

    }

    private void addNativeAds(){
        //Toast.makeText(this, " " + modelList.size() , Toast.LENGTH_SHORT).show();
        for(int i=0; i<modelList.size(); i+=ITEMS_PER_AD){
            final NativeExpressAdView ad = new NativeExpressAdView(DailyActivity.this);
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
                final float scale = DailyActivity.this.getResources().getDisplayMetrics().density;
                // Set the ad size and ad unit ID for each Native Express ad in the items list.
                for (int i = ITEMS_PER_AD; i < modelList.size(); i += ITEMS_PER_AD) {
                    final NativeExpressAdView adView =
                            (NativeExpressAdView) modelList.get(i);
                    final OptRoundCardView cardView = (OptRoundCardView) findViewById(R.id.memes_activity_ads);
                    cardView.setVisibility(View.VISIBLE);
                    final int adWidth = cardView.getWidth() - cardView.getPaddingLeft()
                            - cardView.getPaddingRight();
                    AdSize adSize = new AdSize((int) (adWidth/scale), 320);
                    adView.setAdSize(adSize);
                    adView.setAdUnitId(getResources().getString(R.string.real_native_ad_id));
                }

                // Load the first Native Express ad in the items list.
                loadNativeExpressAd(ITEMS_PER_AD);
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
                loadNativeExpressAd(index + ITEMS_PER_AD);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // The previous Native Express ad failed to load. Call this method again to load
                // the next ad in the items list.
                Log.e("MainActivity", "The previous Native Express ad failed to load. Attempting to"
                        + " load the next Native Express ad in the items list.");
                loadNativeExpressAd(index + ITEMS_PER_AD);
            }
        });

        // Load the Native Express ad.
        adView.loadAd(new AdRequest.Builder().build());
    }
}
