package com.example.exchangingprivatelessons

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED
import com.google.firebase.firestore.firestore
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.firestore.setFirestoreSettings(
            FirebaseFirestoreSettings
                .Builder()
                .setPersistenceEnabled(true)     // disk cache
                .setCacheSizeBytes(CACHE_SIZE_UNLIMITED)
                .build()
        )
    }
}
