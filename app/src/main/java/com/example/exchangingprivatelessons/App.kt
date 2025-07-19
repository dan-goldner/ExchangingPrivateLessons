package com.example.exchangingprivatelessons

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

/**
 * נקודת-כניסה של האפליקציה.
 * - מאתחלת Firebase (אם צריך)
 * - משמשת Hilt כ-component root
 */
@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

    }
}
