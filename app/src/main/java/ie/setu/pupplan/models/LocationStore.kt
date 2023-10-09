package ie.setu.pupplan.models

interface LocationStore {
    fun findAll(): List<LocationModel>
    fun create(location: LocationModel)
    fun update(location: LocationModel)
}