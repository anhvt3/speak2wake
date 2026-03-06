package com.speak2wake

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class Speak2WakeApplication : Application() {

    @Inject lateinit var vocabSeeder: VocabSeeder

    override fun onCreate() {
        super.onCreate()
        // Seed vocabulary on first launch
        CoroutineScope(Dispatchers.IO).launch {
            vocabSeeder.seedIfEmpty()
        }
    }
}
