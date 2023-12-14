package ie.setu.pupplan.firebase

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import ie.setu.pupplan.models.PetLocationModel
import ie.setu.pupplan.models.PetLocationStore
import ie.setu.pupplan.models.PetLocationManager
import ie.setu.pupplan.models.NewEvent
import timber.log.Timber
import java.util.HashMap
import java.util.Random

internal fun generateRandomId(): Long {
    return Random().nextLong()
}

object FirebaseDBManager : PetLocationStore {

    var database: DatabaseReference = FirebaseDatabase.getInstance().reference

    var petLocations = mutableListOf <PetLocationModel>()


    override fun findAll(petLocationsList: MutableLiveData<List <PetLocationModel>>) {
        database.child("petLocations")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase PetLocation error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList <PetLocationModel>()
                    val children = snapshot.children
                    children.forEach {
                        val petLocation = it.getValue(PetLocationModel::class.java)
                        localList.add(petLocation!!)
                    }
                    database.child("petLocations")
                        .removeEventListener(this)
                    println("findAll localList $localList")

                    petLocationsList.value = localList
                }
            })
    }

    override fun findUserAll(userid: String, petLocationsList: MutableLiveData<List <PetLocationModel>>) {
        database.child("user-petLocations").child(userid)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase PetLocation error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList <PetLocationModel>()
                    val children = snapshot.children
                    children.forEach {
                        val petLocation = it.getValue(PetLocationModel::class.java)
                        localList.add(petLocation!!)
                    }
                    database.child("user-petLocations").child(userid)
                        .removeEventListener(this)
                    println("findUserAll localList $localList")

                    petLocationsList.value = localList
                }
            })

    }

    // Function for finding all events on petLocation JSON file
    override fun findEvents(userid: String, petLocationId: String, petLocation: MutableLiveData <PetLocationModel>, eventsList: MutableLiveData<List<NewEvent>>) {
        database.child("user-petLocations").child(userid).child(petLocationId)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase Event error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localPetLocation = snapshot.getValue(PetLocationModel::class.java)
                    val localList = localPetLocation?.events?.toList()

                    database.child("user-petLocations").child(userid).child(petLocationId)
                        .removeEventListener(this)
                    eventsList.value = localList
                }
            })

    }

    override fun findUserEvents(userid: String, petLocationsList: MutableLiveData<List <PetLocationModel>>, eventsList: MutableLiveData<List<NewEvent>>) {
        database.child("user-petLocations").child(userid)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase PetLocation error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localEventList = mutableListOf<NewEvent>()
                    val children = snapshot.children
                    children.forEach {
                        val petLocation = it.getValue(PetLocationModel::class.java)
                        val petLocationEvents = petLocation?.events?.toMutableList()
                        if (petLocationEvents != null) {
                            localEventList += petLocationEvents.toMutableList()
                        }
                    }
                    database.child("user-petLocations").child(userid)
                        .removeEventListener(this)

                    eventsList.value = localEventList
                }
            })

    }

    // Function for finding individual event on petLocation JSON file, using passed event ID
    override fun findUserEvent(userid: String, petLocationsList: MutableLiveData<List <PetLocationModel>>, eventsList: MutableLiveData<List<NewEvent>>, eventId: String, event: MutableLiveData<NewEvent>) {
        database.child("user-petLocations").child(userid)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase PetLocation error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localEventList = mutableListOf<NewEvent>()
                    val children = snapshot.children
                    children.forEach {
                        val petLocation = it.getValue(PetLocationModel::class.java)
                        val petLocationEvents = petLocation?.events?.toMutableList()
                        if (petLocationEvents != null) {
                            localEventList += petLocationEvents.toMutableList()
                        }
                    }
                    database.child("user-petLocations").child(userid)
                        .removeEventListener(this)

                    eventsList.value = localEventList
                    event.value = localEventList.find { p -> p.eventId == eventId }
                }
            })
    }

    // Function for finding individual event on petLocation JSON file, using passed event ID
    override fun findEvent(eventsList: MutableLiveData<List<NewEvent>>, eventId: String, event: MutableLiveData<NewEvent>) {
        database.child("petLocations")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase PetLocation error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localEventList = mutableListOf<NewEvent>()
                    val children = snapshot.children
                    children.forEach {
                        val petLocation = it.getValue(PetLocationModel::class.java)
                        val petLocationEvents = petLocation?.events?.toMutableList()
                        if (petLocationEvents != null) {
                            localEventList += petLocationEvents.toMutableList()
                        }
                    }
                    database.child("petLocations")
                        .removeEventListener(this)

                    eventsList.value = localEventList
                    event.value = localEventList.find { p -> p.eventId == eventId }
                }
            })
    }

    // Function for finding individual petLocation on petLocation JSON file, using passed petLocation
    override fun findPetLocation(petLocation: PetLocationModel): PetLocationModel? {
        logAll()
        return PetLocationManager.petLocations.find { p -> p.uid == petLocation.uid }
    }

    // Function for finding individual petLocation on petLocation JSON file, using passed petLocation
    override fun findPetLocationById(
        userid: String,
        id: String,
        petLocation: MutableLiveData <PetLocationModel>
    ) {

        database.child("user-petLocations").child(userid)
            .child(id.toString()).get().addOnSuccessListener {
                petLocation.value = it.getValue(PetLocationModel::class.java)!!
                println("this is foundpetLocation ${petLocation.value}")
                Timber.i("firebase Got value ${it.value}")
            }.addOnFailureListener{
                Timber.e("firebase Error getting data $it")
            }

    }



    // Function for finding individual petLocation on petLocation JSON file, using passed petLocation
    override fun findPetLocationById2(
        userid: String,
        id: String,
        petLocation: MutableLiveData <PetLocationModel>
    ): PetLocationModel? {
        var currentPetLocation = PetLocationModel()
        val ref = database.child("user-petLocations").child(userid).child(id.toString())
        val menuListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                currentPetLocation = dataSnapshot.getValue(PetLocationModel::class.java)!!
                println("this is the currentPort inside $currentPetLocation")

            }
            override fun onCancelled(databaseError: DatabaseError) {
                // handle error
            }
        }
        ref.addListenerForSingleValueEvent(menuListener)
        println("this is the currentPort outside $currentPetLocation")

        return currentPetLocation

    }

    // Function for creating new petLocation on petLocation JSON file
    override fun create(firebaseUser: MutableLiveData<FirebaseUser>, petLocation: PetLocationModel) {
        Timber.i("Firebase DB Reference : $database")
        //petLocation.id = generateRandomId() // Generation of random id for petLocation

        val uid = firebaseUser.value!!.uid
        val key = database.child("petLocations").push().key
        if (key == null) {
            Timber.i("Firebase Error : Key Empty")
            return
        }
        petLocation.uid = key
        val petLocationValues = petLocation.toMap()

        val childAdd = HashMap<String, Any>()
        childAdd["/petLocations/$key"] = petLocationValues
        childAdd["/user-petLocations/$uid/$key"] = petLocationValues

        database.updateChildren(childAdd)
    }

    // Function for updating existing petLocation on petLocation JSON file, using passed petLocation
    override fun update(userid: String, petLocationid: String, petLocation: PetLocationModel) {

        val petLocationValues = petLocation.toMap()

        println("this is userid in update $userid")
        println("this is petLocationid in update $petLocationid")
        println("this is petLocation in update $petLocation")


        val childUpdate : MutableMap<String, Any?> = HashMap()
        childUpdate["petLocations/$petLocationid"] = petLocationValues
        childUpdate["user-petLocations/$userid/$petLocationid"] = petLocationValues

        database.updateChildren(childUpdate)
    }

    // Function for deleting on JSON file
    override fun delete(userid: String, petLocationId: String) {
        val childDelete : MutableMap<String, Any?> = HashMap()
        childDelete["/petLocations/$petLocationId"] = null
        childDelete["/user-petLocations/$userid/$petLocationId"] = null

        database.updateChildren(childDelete)
    }

    fun updateImageRef(userid: String,imageUri: String, path: String) {

        val userPetLocations = database.child("user-petLocations").child(userid)
        val allPetLocations = database.child("petLocations")

        userPetLocations.addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach {
                        //Update Users imageUri
                        it.ref.child("$path").setValue(imageUri)
                        //Update all petLocations that match 'it'
                        val petLocation = it.getValue(PetLocationModel::class.java)
                        allPetLocations.child(petLocation!!.uid!!)
                            .child("$path").setValue(imageUri)
                    }
                }
            })
    }

    // Function for using Timber to log each petLocation in petLocation list
    private fun logAll() {
        PetLocationManager.petLocations.forEach { Timber.i("$it") }
    }

    // Function for using Timber to log each event associated to petLocations in petLocation list
    private fun logEvents() {
        PetLocationManager.petLocations.forEach {
            var petLocationEvents = it.events?.toMutableList()
            if (petLocationEvents != null) {
                events += petLocationEvents.toMutableList()
            }
        }
    }

    // Creation of events list
    var events = mutableListOf<NewEvent>()


    // Function to find specific petLocations based on passed petLocation category
    override fun findSpecificPetLocations(petLocationCategory: String): MutableList <PetLocationModel> {
        var list =
            PetLocationManager.petLocations.filter { p -> p.category == petLocationCategory } // Create a list based on matching/filtering petLocation categorys
        return list.toMutableList() // Return mutable list and log
        println("this is list: $list")
        logAll()
        return PetLocationManager.petLocations
    }

    // Function to find events that come from petLocations of specific category, using data on passed category
    override fun findSpecificTypeEvents(petLocationCategory: String): MutableList<NewEvent> {
        var list =
            PetLocationManager.petLocations.filter { p -> p.category == petLocationCategory } // Create a list based on matching/filtering petLocation categorys
        println("this is list: $list")
        var petLocationCategoryEventsOverall: MutableList<NewEvent> =
            arrayListOf() // Create a mutable list for following
        if (list.isNotEmpty()) { // If there are at least some petLocations (i.e. selection wasn't made on empty list)
            list.forEach { // For each petLocation in the list, make a list of the petLocation's events. If there is a previous list of events from other petLocations, add the current petLocation events to that list
                println("event item: " + it.events?.toMutableList())
                var petLocationCategoryEvents = it.events?.toMutableList()
                println("this is petLocationCategoryEvent: $petLocationCategoryEvents")
                if (petLocationCategoryEvents != null) {
                    petLocationCategoryEventsOverall += petLocationCategoryEvents.toMutableList()
                    events = petLocationCategoryEventsOverall.toMutableList()
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
        //event.eventId = ie.setu.pupplan.models.generateRandomId()
        var foundPetLocation: PetLocationModel? =
            PetLocationManager.petLocations.find { p -> p.uid == petLocation.uid } // Finding matching petLocation
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

        // Process for updating petLocation JSON file
        /* var foundPetLocation: PetLocationModel? =
             PetLocationManager.petLocations.find { p -> p.id == petLocation.id } // Find the relevant petLocation from the petLocations list based on matching id of passed petLocation
         if (foundPetLocation != null) { // If the petLocation is found...
             if (foundPetLocation.events != null) { // And the petLocation has events (as expected)
                 var eventIdList =
                     arrayListOf<Long>() // Create a arrayList variable for storing event IDs
                 foundPetLocation.events!!.forEach { // For each event in the relevant petLocation, add the event ID to the list of event IDs
                     eventIdList += it.eventId
                 }
                 println("this is eventIdList: $eventIdList")
                 var eventId = event.eventId
                 println("this is eventId: $eventId")
                 val index =
                     eventIdList.indexOf(event.eventId) // Find the index position of the event ID that matches the ID of the event that was passed
                 println("this is index: $index")
                 var petLocationEvents1 =
                     foundPetLocation.events!!.toMutableList() // Create a list of the events from the passed petLocation
                 var short =
                     petLocationEvents1.removeAt(index) // Remove the event at the previously found index position within the created event list
                 println("this is short: $short")
                 petLocationEvents1 =
                     petLocationEvents1.plus(event) as MutableList<NewEvent> // Add the passed event to the shortened list of events
                 foundPetLocation.events =
                     ArrayList(petLocationEvents1).toTypedArray() // Assign the new list of events to the found petLocation
             }
             //serialize() // Update the petLocation JSON file
             logAll()
         }*/
    }

    // Function to delete a event based on passed data for event and petLocation
    override fun deleteEvent(event: NewEvent, petLocation: PetLocationModel) {
        /*var foundPetLocation: PetLocationModel? = PetLocationManager.petLocations.find { p -> p.id == petLocation.id }
        if (foundPetLocation != null) { // If the petLocation is found...
            if (foundPetLocation.events != null) { // And the petLocation has events (as expected)
                var eventIdList =
                    arrayListOf<Long>() // Create a arrayList variable for storing event IDs
                foundPetLocation.events!!.forEach { // For each event in the relevant petLocation, add the event ID to the list of event IDs
                    eventIdList += it.eventId
                }
                println("this is eventIdList: $eventIdList")
                val index =
                    eventIdList.indexOf(event.eventId) // Find the index position of the event ID that matches the ID of the event that was passed
                println("this is index: $index")
                var petLocationEvents1 =
                    foundPetLocation.events!!.toMutableList() // Create a list of the events from the passed petLocation
                var short =
                    petLocationEvents1.removeAt(index) // Remove the event at the previously found index position within the created event list
                println("this is short: $short")
                foundPetLocation.events =
                    ArrayList(petLocationEvents1).toTypedArray() // Assign the new list of events to the found petLocation
            }
            //serialize() // Update the petLocation JSON file
            logAll()
        }*/
    }

    override fun findEventById(eventId: String, petLocationId: String): NewEvent? {
        var foundEvent: NewEvent? = null
        // Process for updating petLocation JSON file
        var foundPetLocation: PetLocationModel? =
            PetLocationManager.petLocations.find { p -> p.uid == petLocationId } // Find the relevant petLocation from the petLocations list based on matching id of passed petLocation
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