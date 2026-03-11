package com.speak2wake

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.google.android.gms.ads.MobileAds

@HiltAndroidApp
class Speak2WakeApplication : Application() {

    @Inject lateinit var vocabSeeder: VocabSeeder

    override fun onCreate() {
        super.onCreate()
        
        // Initialize the Google Mobile Ads SDK
        MobileAds.initialize(this) {}
        
        // Seed vocabulary on first launch
        CoroutineScope(Dispatchers.IO).launch {
            vocabSeeder.seedIfEmpty()
        }
    }
}
