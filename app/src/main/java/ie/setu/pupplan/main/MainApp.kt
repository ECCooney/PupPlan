package ie.setu.pupplan.main

import android.app.Application
import timber.log.Timber
import timber.log.Timber.i

class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()
//        "plant" Timber tree here for logging
        Timber.plant(Timber.DebugTree())
        i("PupPlan started")
    }
}