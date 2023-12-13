package ie.setu.pupplan.main

import android.app.Application
import timber.log.Timber

class PupPlanApp : Application() {

    override fun onCreate() {
        super.onCreate()
//        "plant" Timber tree here for logging
        Timber.plant(Timber.DebugTree())
        Timber.i("PupPlan started")
    }
}