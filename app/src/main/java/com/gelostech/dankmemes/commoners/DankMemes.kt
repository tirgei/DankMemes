package com.gelostech.dankmemes.commoners

import androidx.multidex.MultiDexApplication
import com.google.firebase.database.FirebaseDatabase
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.gelostech.dankmemes.R
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import timber.log.Timber


class DankMemes : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        // Set Timber
        Timber.plant(Timber.DebugTree())

        // Enable Firebase persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        FirebaseFirestore.getInstance().firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()

        // Initialize crash activity
        CaocConfig.Builder.create()
                .enabled(false)
                .showErrorDetails(false)
                .errorDrawable(R.mipmap.ic_launcher)
                .apply()
    }
}