package ie.setu.pupplan.models

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import timber.log.Timber
import java.util.*

// Function for generating random ID numbers
internal fun generateRandomId(): Long {
    return Random().nextLong()
}

object PetLocationManager : PetLocationStore {

    // Create list of petLocations
    var petLocations = mutableListOf <PetLocationModel>()

    // Function for finding all petLocations in JSON file
    override fun findUserAll(userid: String, petLocationList: MutableLiveData<List <PetLocationModel>>) {
        //logAll()

    }

    // Function for finding all petLocations on petLocation JSON file
    override fun findAll(petLocationList: MutableLiveData<List <PetLocationModel>>) {
        //logAll()

    }

    // Function for finding all events on petLocation JSON file
    override fun findEvents(userid: String, petLocationID: String, petLocation: MutableLiveData <PetLocationModel>, eventList: MutableLiveData<List<NewEvent>>) {
        logEvents()

    }

    override fun findUserEvents(userid: String, petLocationsList: MutableLiveData<List <PetLocationModel>>, eventsList: MutableLiveData<List<NewEvent>>) {
        logEvents()

    }

    // Function for finding individual event on petLocation JSON file, using passed event ID
    override fun findEvent(eventsList: MutableLiveData<List<NewEvent>>, eventId: String, event: MutableLiveData<NewEvent>) {
        logEvents()

    }

    override fun findUserEvent(userid: String, petLocationsList: MutableLiveData<List <PetLocationModel>>, eventsList: MutableLiveData<List<NewEvent>>, eventId: String, event: MutableLiveData<NewEvent>) {
        logEvents()

    }

    // Function for finding individual petLocation on petLocation JSON file, using passed petLocation
    override fun findPetLocation(petLocation: PetLocationModel): PetLocationModel? {
        logAll()
        return petLocations.find { p -> p.uid == petLocation.uid }
    }

    // Function for finding individual petLocation on petLocation JSON file, using passed petLocation
    override fun findPetLocationById(userid: String, id: String, petLocation: MutableLiveData <PetLocationModel>) {
        logAll()
        //return petLocations.find { p -> p.uid == id }

    }

    // Function for finding individual petLocation on petLocation JSON file, using passed petLocation
    override fun findPetLocationById2(userid: String, id: String, petLocation: MutableLiveData <PetLocationModel>): PetLocationModel? {
        logAll()
        //return petLocations.find { p -> p.uid == id }
        return PetLocationModel()

    }

    // Function for creating new petLocation on petLocation JSON file
    override fun create(firebaseUser: MutableLiveData<FirebaseUser>, petLocation: PetLocationModel) {
        //petLocation.id = generateRandomId() // Generation of random id for petLocation
        petLocations.add(petLocation)
        //serialize()
        logAll()
    }

    // Function for updating existing petLocation on petLocation JSON file, using passed petLocation
    override fun update(userid: String, petLocationId: String, petLocation: PetLocationModel) {
        // Find petLocation based on ID
        var foundPetLocation: PetLocationModel? = petLocations.find { p -> p.uid == petLocation.uid }
        // Update values and store
        if (foundPetLocation != null) {
            foundPetLocation.title = petLocation.title
            foundPetLocation.description = petLocation.description
            foundPetLocation.image = petLocation.image
            foundPetLocation.events = petLocation.events
            foundPetLocation.category = petLocation.category
            //serialize()
            logAll()
        }
    }

    // Function for deleting petLocation on petLocation JSON file, using passed petLocation
    override fun delete(userid: String, petLocationId: String) {
        //println("this is the removed petLocation: $petLocation")
        //petLocations.remove(petLocation)
        //serialize()
        logAll()
    }

    // Function for using Timber to log each petLocation in petLocation list
    private fun logAll() {
        petLocations.forEach { Timber.i("$it") }
    }

    // Function for using Timber to log each event associated to petLocations in petLocation list
    private fun logEvents() {
        petLocations.forEach {
            var petLocationEvents = it.events?.toMutableList()
            if (petLocationEvents != null) {
                events += petLocationEvents.toMutableList()
            }
        }
    }

    // Creation of events list
    var events = mutableListOf<NewEvent>()


    // Function to find specific petLocations based on passed petLocation category
    override fun findSpecificPetLocations(category: String): MutableList <PetLocationModel> {
        var list =
            petLocations.filter { l -> l.category == category } // Create a list based on matching/filtering petLocation categorys
        return list.toMutableList() // Return mutable list and log
        println("this is list: $list")
        logAll()
        return petLocations
    }

    // Function to find events that come from petLocations of specific category, using data on passed category
    override fun findSpecificTypeEvents(category: String): MutableList<NewEvent> {
        var list =
            petLocations.filter { l -> l.category == category } // Create a list based on matching/filtering petLocation categorys
        println("this is list: $list")
        var categoryEventsOverall: MutableList<NewEvent> =
            arrayListOf() // Create a mutable list for following
        if (list.isNotEmpty()) { // If there are at least some petLocations (i.e. selection wasn't made on empty list)
            list.forEach { // For each petLocation in the list, make a list of the petLocation's events. If there is a previous list of events from other petLocations, add the current petLocation events to that list
                println("event item: " + it.events?.toMutableList())
                var petLocationCategoryEvents = it.events?.toMutableList()
                println("this is petLocationCategoryEvent: $petLocationCategoryEvents")
                if (petLocationCategoryEvents != null) {
                    categoryEventsOverall += petLocationCategoryEvents.toMutableList()
                    events = categoryEventsOverall.toMutableList()
                }
            }
        } else { // Otherwise return an empty array
            events = arrayListOf()
        }
        println("this is final returned events: $events")
        return events
    }

    // Function for creating a new event using passed data for event and petLocation
    override fun createEvent(event: NewEvent, petLocation: PetLocationModel) {
        //event.eventId = generateRandomId()
        var foundPetLocation: PetLocationModel? =
            petLocations.find { p -> p.uid == petLocation.uid } // Finding matching petLocation
        if (foundPetLocation != null) {
            if (foundPetLocation.events != null) { // If there are already events in the petLocation, add this event to the list
                var petLocationEvents = foundPetLocation.events
                petLocationEvents = petLocationEvents?.plus(event)?.toMutableList()
                foundPetLocation.events = petLocationEvents
            } else {
                foundPetLocation.events =
                    listOf(event).toMutableList() // Otherwise initiate a new array of events
            }
            //serialize() // Add event to petLocation JSON file
            logAll()
        }
    }

    // Function for updating a event using passed data for event and related petLocation
    override fun updateEvent(event: NewEvent, petLocation: PetLocationModel) {

}


    override fun deleteEvent(event: NewEvent, petLocation: PetLocationModel) {

    }

    override fun findEventById(eventId: String, petLocationId: String): NewEvent? {
        var foundEvent: NewEvent? = null
        // Process for updating petLocation JSON file
        var foundPetLocation: PetLocationModel? =
            petLocations.find { p -> p.uid == petLocationId } // Find the relevant petLocation from the petLocations list based on matching id of passed petLocation
        if (foundPetLocation != null) { // If the petLocation is found...
            if (foundPetLocation.events != null) { // And the petLocation has events (as expected)

                foundPetLocation.events!!.forEach { // For each event in the relevant petLocation, add the event ID to the list of event IDs
                    if (it.eventId == eventId) {
                        foundEvent = it
                    }
                }
            }
        }
        return foundEvent
    }
}

