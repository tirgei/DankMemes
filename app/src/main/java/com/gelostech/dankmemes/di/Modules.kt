package com.gelostech.dankmemes.di

import com.gelostech.dankmemes.R
import com.gelostech.dankmemes.data.repositories.CommentsRepository
import com.gelostech.dankmemes.data.repositories.MemesRepository
import com.gelostech.dankmemes.data.repositories.NotificationsRepository
import com.gelostech.dankmemes.data.repositories.UsersRepository
import com.gelostech.dankmemes.ui.viewmodels.CommentsViewModel
import com.gelostech.dankmemes.ui.viewmodels.MemesViewModel
import com.gelostech.dankmemes.ui.viewmodels.NotificationsViewModel
import com.gelostech.dankmemes.ui.viewmodels.UsersViewModel
import com.gelostech.dankmemes.utils.SessionManager
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val firebaseModule = module {
    single { FirebaseDatabase.getInstance().reference }
    single { FirebaseFirestore.getInstance() }
    single { FirebaseStorage.getInstance().reference }
    single { FirebaseAuth.getInstance() }
}

val repositoriesModule = module {
    single { UsersRepository(get(), get(), get()) }
    single { MemesRepository(get(), get()) }
    single { NotificationsRepository(get()) }
    single { CommentsRepository(get()) }
}

val viewModelsModule = module {
    viewModel { UsersViewModel(get()) }
    viewModel { MemesViewModel(get()) }
    viewModel { NotificationsViewModel(get()) }
    viewModel { CommentsViewModel(get()) }
}

val sessionManagerModule = module {
    single { SessionManager(androidApplication()) }
}

val googleSignClientModule = module {
    single {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(androidApplication().getString(R.string.google_signin_key))
                .requestEmail()
                .build()
    }
}

val adBuilderModule = module {
    single { AdLoader.Builder(androidApplication(), androidApplication().getString(R.string.admob_native_test_ad)) }
}