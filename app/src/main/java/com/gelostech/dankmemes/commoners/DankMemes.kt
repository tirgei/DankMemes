package com.gelostech.dankmemes.commoners

import com.google.firebase.database.FirebaseDatabase
import android.support.multidex.MultiDexApplication
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.gelostech.dankmemes.R
import timber.log.Timber


class DankMemes : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        Timber.plant(Timber.DebugTree())

        CaocConfig.Builder.create()
                .enabled(false)
                .showErrorDetails(false)
                .errorDrawable(R.mipmap.ic_launcher)
                .apply()
    }
}