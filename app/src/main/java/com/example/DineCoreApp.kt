package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class DineCoreApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.d("DineCoreApp", "FirebaseApp initialized successfully on launch")
            }
        } catch (e: Throwable) {
            Log.e("DineCoreApp", "FirebaseApp initialization handled safely: ${e.message}", e)
        }
    }
}
