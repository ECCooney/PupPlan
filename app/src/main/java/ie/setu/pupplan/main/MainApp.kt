package ie.setu.pupplan.main

import android.app.Application
import ie.setu.pupplan.models.LocationJSONStore
import ie.setu.pupplan.models.LocationMemStore
import ie.setu.pupplan.models.LocationStore
import timber.log.Timber
import timber.log.Timber.i

class MainApp : Application() {

    lateinit var locations: LocationStore

    override fun onCreate() {
        super.onCreate()
//        "plant" Timber tree here for logging
        Timber.plant(Timber.DebugTree())
        locations = LocationJSONStore(applicationContext)
        i("PupPlan started")
    }
}