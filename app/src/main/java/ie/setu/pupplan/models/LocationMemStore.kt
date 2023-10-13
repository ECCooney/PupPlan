package ie.setu.pupplan.models

import timber.log.Timber.i

var lastId = 0L

internal fun getId(): Long {
    return lastId++
}

class LocationMemStore : LocationStore {

    val locations = ArrayList<LocationModel>()
    override fun findAll(): List<LocationModel> {
        return locations
    }
    override fun create(location: LocationModel) {
        location.id=getId()
        locations.add(location)
        logAll()
    }

    override fun update(location: LocationModel) {
        var foundLocation: LocationModel? = locations.find { l -> l.id == location.id}
        if (foundLocation != null) {
            foundLocation.title = location.title
            foundLocation.description = location.description
            foundLocation.image= location.image
            logAll()
        }
    }

    private fun logAll() {
        locations.forEach{ i("${it}") }
    }
}