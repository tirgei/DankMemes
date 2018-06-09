package com.gelostech.dankmemes.commoners

import com.google.firebase.database.FirebaseDatabase
import android.support.multidex.MultiDexApplication


class MyApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}