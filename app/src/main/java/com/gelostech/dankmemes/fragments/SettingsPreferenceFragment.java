package com.gelostech.dankmemes.fragments;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.gelostech.dankmemes.R;
import com.gelostech.dankmemes.models.UserModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.marcoscg.easylicensesdialog.EasyLicensesDialogCompat;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsPreferenceFragment extends android.preference.PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    private Preference emailPref;
    private SharedPreferences pref;
    private String userId;
    private DatabaseReference dr;
    private Vibrator v;

    public SettingsPreferenceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);

        for(int i=0; i<getPreferenceScreen().getPreferenceCount(); i++){
            pickPreferenceObject(getPreferenceScreen().getPreference(i));
        }
    }

    private void pickPreferenceObject(Preference p){
        if(p instanceof PreferenceCategory){
            PreferenceCategory cat = (PreferenceCategory) p;
            for(int i=0; i<cat.getPreferenceCount();i++){
                pickPreferenceObject(cat.getPreference(i));
            }
        } else {
            initSummary(p);
        }
    }

    private void initSummary(Preference p){
        if(p instanceof EditTextPreference){
            EditTextPreference userEdit = (EditTextPreference) p;
            p.setSummary(userEdit.getText());
        }

        if(p instanceof RingtonePreference){
            final RingtonePreference ring = (RingtonePreference) p;
            ring.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), Uri.parse((String)newValue));
                    ring.setSummary(ringtone.getTitle(getActivity()));
                    return true;
                }
            });

            String rintonePref = p.getSharedPreferences().getString(p.getKey(), "default_sound");
            Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), Uri.parse(rintonePref));
            ring.setSummary(ringtone.getTitle(getActivity()));
        }



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_preference, container, false);

        pref = getActivity().getSharedPreferences("DankMemes", Context.MODE_PRIVATE);
        userId = pref.getString("userID", null);

        Preference getDev = findPreference("contact_dev");
        getDev.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto: devtirgei@gmail.com"));
                startActivity(Intent.createChooser(emailIntent, "Send feedback"));

                return true;
            }
        });

        Preference notificationSoundPref = findPreference("notification_ringtone");
        notificationSoundPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // do what you need here
                updateRingtone((RingtonePreference) preference, Uri.parse((String)newValue));
                return true;
            }
        });

        v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        final CheckBoxPreference vibrate = (CheckBoxPreference) getPreferenceManager().findPreference("notification_vibrate");
        vibrate.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(newValue.toString().equals("true")){
                    v.vibrate(500);
                }
                return true;
            }
        });

        Preference invites = findPreference("send_link");
        invites.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, getActivity().getString(R.string.app_name));
                String message = getResources().getString(R.string.invite_body) + "\n\n" + getResources().getString(R.string.play_store_link) + getActivity().getPackageName();
                //Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                intent.putExtra(Intent.EXTRA_TEXT, message);
                startActivity(Intent.createChooser(intent, "Invite pals..."));

                return true;
            }
        });

        Preference rate = findPreference("rate_app");
        rate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //Toast.makeText(getActivity(), "Rate app", Toast.LENGTH_SHORT).show();
                Uri uri = Uri.parse(getResources().getString(R.string.play_store_link) + getActivity().getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(getResources().getString(R.string.play_store_link) + getActivity().getPackageName())));
                }

                return true;
            }
        });

        Preference tou = findPreference("terms_of_use");
        tou.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new EasyLicensesDialogCompat(getActivity())
                        .setTitle("Disclaimer!")
                        .setPositiveButton(android.R.string.ok, null)
                        .show();

                return false;
            }
        });

        return view;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals("user_name")){
            Preference pref = findPreference(key);
            pref.setSummary(sharedPreferences.getString(key, ""));

            dr = FirebaseDatabase.getInstance().getReference("users").child(userId);
            UserModel model = new UserModel(sharedPreferences.getString(key, ""), userId);
            dr.setValue(model);

        }

    }

    @Override
    public void onPause(){
        super.onPause();
        getPreferenceScreen()
                .getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        getPreferenceScreen()
                .getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    private void updateRingtone(RingtonePreference preference, Uri ringtoneUri){
        Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), ringtoneUri);
        if(ringtone != null){
            preference.setSummary(ringtone.getTitle(getActivity()));
        } else {
            preference.setSummary("Silent");
        }
    }

}
