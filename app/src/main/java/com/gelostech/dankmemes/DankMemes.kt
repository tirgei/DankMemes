package com.gelostech.dankmemes

import androidx.multidex.MultiDexApplication
import com.google.firebase.database.FirebaseDatabase
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.gelostech.dankmemes.di.firebaseModule
import com.gelostech.dankmemes.di.repositoriesModule
import com.gelostech.dankmemes.di.sessionManagerModule
import com.gelostech.dankmemes.di.viewModelsModule
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
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

        // Initialize Koin
        startKoin {
            androidLogger()
            androidContext(this@DankMemes)
            modules(listOf(firebaseModule, repositoriesModule, viewModelsModule, sessionManagerModule))
        }
    }
}