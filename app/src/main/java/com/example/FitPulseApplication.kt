package com.example

import android.app.Application
import com.example.ads.AdManager

class FitPulseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        try {
            AdManager.initialize(this)
        } catch (t: Throwable) {
            android.util.Log.e("FitPulseApplication", "Error initializing AdManager", t)
        }
    }
}
