package ie.setu.pupplan.models

import timber.log.Timber.i

class LocationMemStore : LocationStore {

    val locations = ArrayList<LocationModel>()
    override fun findAll(): List<LocationModel> {
        return locations
    }
    override fun create(location: LocationModel) {
        locations.add(location)
        logAll()
    }

    fun logAll() {
        locations.forEach{ i("${it}") }
    }
}