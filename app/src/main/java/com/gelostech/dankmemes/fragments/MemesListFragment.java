package com.gelostech.dankmemes.fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.gelostech.dankmemes.R;
import com.gelostech.dankmemes.activities.DailyMemesActivity;
import com.gelostech.dankmemes.activities.FavoritesActivity;
import com.gelostech.dankmemes.activities.SettingsActivity;
import com.gelostech.dankmemes.adapters.MemeListAdapter;
import com.gelostech.dankmemes.commoners.FirebaseConstants;
import com.gelostech.dankmemes.models.UploadModel;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wooplr.spotlight.SpotlightView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MemesListFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private List<Object> modelList;
    private RecyclerView listView;
    private MemeListAdapter memeAdapter;
    private LinearLayoutManager lm;
    private DatabaseReference df;
    private ProgressDialog pd;
    private SwipeRefreshLayout refreshLayout;
    private Toolbar toolbar;
    private int lastFirstVisiblePosition=0;
    private String SHOWCASE_ID1 = "1";
    private Parcelable state;

    public MemesListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_memes_list, container, false);
        setHasOptionsMenu(true);

        toolbar = (Toolbar) view.findViewById(R.id.main_activity_toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(getActivity().getSupportFragmentManager().getBackStackEntryCount() != 0) {
                    getActivity().getSupportFragmentManager().popBackStack();
                    Toast.makeText(getActivity(), "Exiting main activity..", Toast.LENGTH_SHORT).show();
                } else {
                    getActivity().finish();
                    Toast.makeText(getActivity(), "Exiting main activity..", Toast.LENGTH_SHORT).show();
                }
            }
        });

        View clickLogo = toolbar.getChildAt(1);
        clickLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(settingsIntent);
            }
        });

        new SpotlightView.Builder(getActivity())
                .introAnimationDuration(400)
                //.enableRevealAnimation(true)
                .performClick(true)
                .fadeinTextDuration(400)
                .headingTvColor(Color.parseColor("#eb273f"))
                .headingTvSize(32)
                .headingTvText("Settings")
                .subHeadingTvColor(Color.parseColor("#ffffff"))
                .subHeadingTvSize(16)
                .subHeadingTvText("Tap here to edit your preferences.")
                .maskColor(Color.parseColor("#dc000000"))
                .target(clickLogo)
                .lineAnimDuration(400)
                .lineAndArcColor(Color.parseColor("#eb273f"))
                .dismissOnTouch(true)
                .dismissOnBackPress(true)
                .enableDismissAfterShown(true)
                .usageId(SHOWCASE_ID1)
                .show();

        listView = (RecyclerView) view.findViewById(R.id.memes_list);
        modelList = new ArrayList<>();
        lm = new LinearLayoutManager(getActivity());

        listView.setHasFixedSize(true);
        listView.setLayoutManager(lm);
        memeAdapter = new MemeListAdapter(getActivity(), modelList);

        pd = new ProgressDialog(getActivity());
        pd.setMessage("Loading memes...");
        //pd.setCancelable(false);
        //pd.show();

        loadMemes();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater){
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.main_activity_toolbar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()){

            case R.id.select_option_faves:
                //Toast.makeText(this, "Take me to my faves!", Toast.LENGTH_SHORT).show();

                Intent favesIntent = new Intent(getActivity(), FavoritesActivity.class);
                startActivity(favesIntent);

                return true;

            /*case R.id.select_memes_date:
                //Toast.makeText(this, "Pick this memes date", Toast.LENGTH_SHORT).show();
                pickDate();
                return true;*/

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private Calendar minDate(){
        final int year = 2017, month = 05, day = 23;
        final Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);

        return cal;
    }

    private void pickDate(){
        Calendar cal = Calendar.getInstance();
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                this, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show(getActivity().getFragmentManager(), "Datepickerdialog");
        datePickerDialog.setMinDate(minDate());
        datePickerDialog.setThemeDark(true);
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

        String [] months = {"Jan", "Feb", "March", "April", "May", "June", "July", "Aug", "Sept", "Oct", "Nov", "Dec"};
        String date = dayOfMonth + "/" + months[monthOfYear] + "/" + year;

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
        
        Intent intent = new Intent(getActivity(), DailyMemesActivity.class);
        intent.putExtra("Date", String.valueOf(date));
        startActivity(intent);
    }


    private void loadMemes(){
        df = FirebaseDatabase.getInstance().getReference(FirebaseConstants.MEMES_PATH_UPLOADS +"/");

        df.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                pd.dismiss();

                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                  if(dataSnapshot.getChildren() != null){
                      //  for(DataSnapshot snapshot : postSnapshot.getChildren()){
                      UploadModel memes = postSnapshot.getValue(UploadModel.class);
                      modelList.add(memes);
                      listView.setAdapter(memeAdapter);
                      lm.onRestoreInstanceState(state);
                      //   }
                  } else {
                      pd.dismiss();
                      Toast.makeText(getContext(), "No posts available!", Toast.LENGTH_LONG).show();
                  }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                memeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                memeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                memeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                pd.dismiss();
                Log.d("Meme_list_error: ", databaseError.getMessage());
            }
        });


    }

    @Override
    public void onResume(){
        super.onResume();
        lm.onRestoreInstanceState(state);

    }

    @Override
    public void onPause(){
        super.onPause();
        state = lm.onSaveInstanceState();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        getActivity().finish();
    }



}
