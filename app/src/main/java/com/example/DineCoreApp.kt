package com.example

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class DineCoreApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initFirebase(this)
    }

    companion object {
        private const val TAG = "DineCoreApp"

        @Synchronized
        fun initFirebase(context: Context): FirebaseApp? {
            return try {
                if (FirebaseApp.getApps(context).isNotEmpty()) {
                    return FirebaseApp.getInstance()
                }

                // First attempt default initialization via resources
                try {
                    val app = FirebaseApp.initializeApp(context)
                    if (app != null) {
                        Log.d(TAG, "FirebaseApp initialized via default resources")
                        return app
                    }
                } catch (resEx: Throwable) {
                    Log.w(TAG, "Default Firebase resource init failed, falling back to explicit options: ${resEx.message}")
                }

                // Explicit fallback options using google-services.json values
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:418573393556:android:dbb3ae81803e8da4121e81")
                    .setApiKey("AIzaSyDS6q4Y8Bhp1WOmlE8Sm9Lmkm-nQznCKNE")
                    .setProjectId("gen-lang-client-0204027648")
                    .setGcmSenderId("418573393556")
                    .setStorageBucket("gen-lang-client-0204027648.firebasestorage.app")
                    .setDatabaseUrl("https://gen-lang-client-0204027648.firebaseio.com")
                    .build()

                val app = FirebaseApp.initializeApp(context, options)
                Log.d(TAG, "FirebaseApp initialized successfully with explicit options")
                app
            } catch (e: Throwable) {
                Log.e(TAG, "Error initializing FirebaseApp: ${e.message}", e)
                try {
                    FirebaseApp.getInstance()
                } catch (_: Throwable) {
                    null
                }
            }
        }
    }
}
