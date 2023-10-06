package ie.setu.pupplan.main

import android.app.Application
import ie.setu.pupplan.Models.LocationModel
import timber.log.Timber
import timber.log.Timber.i

class MainApp : Application() {

    val locations = ArrayList<LocationModel>()
    override fun onCreate() {
        super.onCreate()
//        "plant" Timber tree here for logging
        Timber.plant(Timber.DebugTree())
        i("Pupplan started")
        locations.add(LocationModel("One", "About one..."))
        locations.add(LocationModel("Two", "About two..."))
        locations.add(LocationModel("Three", "About three..."))
    }
}