package com.gelostech.dankmemes.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gelostech.dankmemes.R;
import com.gelostech.dankmemes.commoners.FontManager;
import com.gelostech.dankmemes.models.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SplashActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 2000;
    private FirebaseAuth userAuth;
    private FirebaseAuth.AuthStateListener authListener;
    private DatabaseReference dr;
    private Boolean authFlag = false;
    private TextView dank, memes;
    private ProgressBar settingUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        dank = (TextView) findViewById(R.id.splash_dank);
        memes = (TextView) findViewById(R.id.splash_memes);
        settingUp = (ProgressBar) findViewById(R.id.setting_up_acc);

        dank.setTypeface(FontManager.getTypeFace(SplashActivity.this, FontManager.brooke));
        memes.setTypeface(FontManager.getTypeFace(SplashActivity.this, FontManager.naughty));

        userAuth = FirebaseAuth.getInstance();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    if(authFlag == false){
                        updateUI(user);
                        authFlag = true;
                        //FirebaseCrash.log("User logged in");
                    }

                } else {
                    signIn();
                }
            }
        };

    }

    @Override
    public void onStart() {
        super.onStart();
        if(userAuth == null){
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }

        userAuth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop(){
        super.onStop();
        if(authListener != null)
            userAuth.removeAuthStateListener(authListener);
    }

    private void signIn(){
        settingUp.setVisibility(View.VISIBLE);
        userAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = userAuth.getCurrentUser();
                            if(authFlag == false){
                                updateUI(user);
                                authFlag = true;
                            }

                            final SharedPreferences pref = getSharedPreferences("DankMemes", Context.MODE_PRIVATE);
                            SharedPreferences.Editor edit = pref.edit();
                            edit.putString("userID", user.getUid().toString());
                            edit.commit();

                            dr = FirebaseDatabase.getInstance().getReference("users");
                            UserModel model = new UserModel();
                            model.setUserId(user.getUid());
                            model.setUserName("");

                            dr.child(model.getUserId()).setValue(model);
                            FirebaseCrash.log("New user created: " + model.getUserId() );

                        } else {
                            Log.d("Authentication Error: ", task.getException().getMessage());
                            FirebaseCrash.log("Error creating new user: " + task.getException().getMessage());
                            updateUI(null);
                            Toast.makeText(SplashActivity.this, "Error logging in!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MemesActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.enter_favorites, R.anim.exit_favorites);
            }
        }, SPLASH_TIME_OUT);

        //Toast.makeText(this, user.getUid().toString(), Toast.LENGTH_LONG).show();


    }

}
