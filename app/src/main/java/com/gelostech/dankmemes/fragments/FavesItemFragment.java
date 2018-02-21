package com.gelostech.dankmemes.fragments;


import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gelostech.dankmemes.R;
import com.gelostech.dankmemes.models.FaveListModel;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavesItemFragment extends Fragment {
    private Toolbar toolbar;
    private ImageView image;
    private String key;
    private DatabaseReference dr;
    private FaveListModel model;
    private String url;
    private String userFaveId;

    public FavesItemFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_faves_item, container, false);
        image = (ImageView) view.findViewById(R.id.fave_image_item);

        key = getArguments().getString("faveId");

        toolbar = (Toolbar) getActivity().findViewById(R.id.faves_activity_toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.faves));
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(getActivity().getSupportFragmentManager().getBackStackEntryCount() != 0) {
                    getActivity().getSupportFragmentManager().popBackStack();
                    //Toast.makeText(getActivity(), "Clicked!!!!!!!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        final SharedPreferences pref = getActivity().getSharedPreferences("DankMemes", Context.MODE_PRIVATE);
        userFaveId = pref.getString("userID", null);

        dr = FirebaseDatabase.getInstance().getReference("favorites").child(userFaveId);

        dr.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                model = dataSnapshot.getValue(FaveListModel.class);

                Glide.with(getContext()).load(model.getPicUrl()).thumbnail(0.1f).into(image);
                url = model.getPicUrl();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Fave item error: ", databaseError.getMessage());
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, menuInflater);

        /*menuInflater = getActivity().getMenuInflater();

        menu.add(Menu.NONE, 0, Menu.NONE, "Save item").setIcon(R.drawable.ic_save)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);*/


        menuInflater.inflate(R.menu.faves_toolbar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()){

            case R.id.save_meme_fave:
                checkVersion();
                return true;

            case R.id.unfave_meme:
                dr = FirebaseDatabase.getInstance().getReference("favorites").child(userFaveId);
                dr.child(key).removeValue();

                DatabaseReference d = FirebaseDatabase.getInstance().getReference("memes").child(model.getUploadDay()).child(model.getFaveKey()).child("hasFaved");
                d.child(userFaveId).removeValue();

                Toast.makeText(getContext(), "Meme removed from Favorites!", Toast.LENGTH_SHORT).show();
                return true;

            default:
                break;
        }

        return super.onOptionsItemSelected(menuItem);
    }


    private void checkVersion(){
        if (Build.VERSION.SDK_INT >= 23)
        {
            if (checkPermission())
            {
                downloadPic(url);
            } else {
                requestPermission(); // Code for permission
            }
        }
        else
        {
            downloadPic(url);
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(getContext(), "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                    //downloadPic(imageUrl);
                    Toast.makeText(getContext(), "Permission granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }

    private void downloadPic(String url){
        Toast.makeText(getContext(), "Downloading...", Toast.LENGTH_SHORT).show();
        File direct = new File(Environment.getExternalStorageDirectory() + "/DankMemes");

        if(!direct.exists()){
            direct.mkdirs();
        }

        DownloadManager mgr = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        Uri downloadLink = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(downloadLink);

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false).setTitle(getActivity().getResources().getString(R.string.app_name))
                .setDestinationInExternalPublicDir("/DankMemes", "DankMeme-" + System.currentTimeMillis() + ".jpg" );

        mgr.enqueue(request);

    }

}
