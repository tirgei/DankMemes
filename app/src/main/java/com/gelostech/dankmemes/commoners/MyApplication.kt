package com.gelostech.dankmemes.commoners

import android.support.multidex.MultiDexApplication
import com.google.firebase.database.FirebaseDatabase


class MyApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}