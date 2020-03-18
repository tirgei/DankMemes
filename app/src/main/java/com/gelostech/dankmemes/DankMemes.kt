package com.gelostech.dankmemes

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.gelostech.dankmemes.di.*
import com.gelostech.dankmemes.utils.Constants
import com.gelostech.dankmemes.utils.SessionManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.iid.FirebaseInstanceId
import io.reactivex.plugins.RxJavaPlugins
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber


class DankMemes : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        // Set theme
        AppCompatDelegate.setDefaultNightMode(SessionManager(this).themeMode())

        // Initialize app
        if (FirebaseApp.getApps(this).isNullOrEmpty())
            FirebaseApp.initializeApp(this)

        // Set Timber
        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())

        // Initialize crash activity
        CaocConfig.Builder.create()
                .enabled(true)
                .showErrorDetails(false)
                .errorDrawable(R.mipmap.ic_launcher)
                .apply()

        // Enable Firebase persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        FirebaseFirestore.getInstance().firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        updateNotificationToken(this)

        // Initialize Koin
        startKoin {
            androidLogger()
            androidContext(this@DankMemes)
            modules(listOf(
                    firebaseModule,
                    repositoriesModule,
                    viewModelsModule,
                    sessionManagerModule,
                    googleSignClientModule)
            )
        }

        // Set RxJava handler
        RxJavaPlugins.setErrorHandler {
            Timber.e("RxJava error: ${it.localizedMessage}")
        }

    }

    companion object {
        @JvmStatic
        fun updateNotificationToken(context: Context) {
            val sessionManager = SessionManager(context)

            if (sessionManager.isLoggedIn()) {
                FirebaseInstanceId.getInstance().instanceId
                        .addOnCompleteListener(OnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                Timber.e("Error initiating new token")
                                return@OnCompleteListener
                            }

                            // Get new Instance ID token
                            val token = task.result?.token
                            token?.let {
                                val dbRef = FirebaseFirestore.getInstance()
                                dbRef.collection(Constants.USERS).document(sessionManager.getUserId()).update(Constants.USER_TOKEN, it)
                            }
                        })
            }
        }
    }
}