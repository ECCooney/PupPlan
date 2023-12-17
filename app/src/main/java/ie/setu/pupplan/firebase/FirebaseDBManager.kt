package ie.setu.pupplan.firebase

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import ie.setu.pupplan.models.Favourite
import ie.setu.pupplan.models.PetLocationModel
import ie.setu.pupplan.models.NewEvent
import ie.setu.pupplan.models.PetLocationStore
import timber.log.Timber
import java.util.HashMap
import java.util.Random

object FirebaseDBManager : PetLocationStore {

    //initialise Firebase database
    var database: DatabaseReference = FirebaseDatabase.getInstance().reference

    //function to find and return all petLocations in database
    override fun findAll(petLocationsList: MutableLiveData<List<PetLocationModel>>) {
        database.child("petLocations")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase PetLocation error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<PetLocationModel>()
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
    //function to find and return all petLocations belonging to a user
    override fun findUserAll(userid: String, petLocationsList: MutableLiveData<List<PetLocationModel>>) {
        database.child("user-petLocations").child(userid)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase PetLocation error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<PetLocationModel>()
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

    //function to find and return all events favourited by a user, include their and those of others
    override fun findAllFavourites(userid: String, favouritesList: MutableLiveData<List<Favourite>>) {
        database.child("user-favourites").child(userid)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase Favourite error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<Favourite>()
                    val children = snapshot.children
                    children.forEach {
                        val favourite = it.getValue(Favourite::class.java)
                        localList.add(favourite!!)
                    }
                    database.child("user-favourites").child(userid)
                        .removeEventListener(this)
                    println("findUserAllFavourites localList $localList")

                    favouritesList.value = localList
                }
            })
    }

    //function to find and return all events favourited by a user and belonging to them
    override fun findUserFavourites(userid: String, favouritesList: MutableLiveData<List<Favourite>>) {
        database.child("user-favourites").child(userid)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase Favourite error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<Favourite>()
                    val children = snapshot.children
                    children.forEach {
                        val favourite = it.getValue(Favourite::class.java)
                        if (favourite?.eventFavourite?.eventUserId == userid) {
                            localList.add(favourite!!)
                        }
                    }
                    database.child("user-favourites").child(userid)
                        .removeEventListener(this)
                    println("findUserAllFavourites localList $localList")

                    favouritesList.value = localList
                }
            })

    }

    // function to find and return all events within a particular petLocation belonging to a user
    override fun findEvents(userid: String, petLocationId: String, petLocation: MutableLiveData<PetLocationModel>, eventsList: MutableLiveData<List<NewEvent>>) {
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

    //function to find and return all events from all petLocations belonging to all users
    override fun findAllEvents(eventsList: MutableLiveData<List<NewEvent>>) {
        database.child("petLocations")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase Event error : ${error.message}")
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
                }
            })

    }


    // function for finding and returning individual event.
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



    // function for finding and returning individual petLocation belonging to a user.
    override fun findPetLocationById(
        userid: String,
        id: String,
        petLocation: MutableLiveData<PetLocationModel>
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

    // function for creating new petLocation in Firebase real-time database
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

    // function for creating new favourite in Firebase real-time database
    override fun createFavourite(firebaseUser: MutableLiveData<FirebaseUser>, favourite: Favourite) {
        Timber.i("Firebase DB Reference : $database")
        val uid = firebaseUser.value!!.uid
        val key = database.child("favourites").push().key
        if (key == null) {
            Timber.i("Firebase Error : Key Empty")
            return
        }
        favourite.uid = key
        val favouriteValues = favourite.toMap()

        val childAdd = HashMap<String, Any>()
        childAdd["/favourites/$key"] = favouriteValues
        childAdd["/user-favourites/$uid/$key"] = favouriteValues

        database.updateChildren(childAdd)
    }

    // function for updating existing petLocation in Firebase, using passed petLocation
    override fun update(userid: String, petLocationid: String, petLocation: PetLocationModel) {

        val petLocationValues = petLocation.toMap()

        val childUpdate : MutableMap<String, Any?> = HashMap()
        childUpdate["petLocations/$petLocationid"] = petLocationValues
        childUpdate["user-petLocations/$userid/$petLocationid"] = petLocationValues

        database.updateChildren(childUpdate)
    }

    // function for updating event in existing favourite in Firebase, using passed event
    override fun updateFavourite(userid: String, event: NewEvent) {
        database.child("favourites")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase Favourite error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<Favourite>()
                    val children = snapshot.children
                    children.forEach {
                        val favourite = it.getValue(Favourite::class.java)
                        localList.add(favourite!!)
                    }
                    database.child("favourites")
                        .removeEventListener(this)
                    println("findAllFavourites localList $localList")

                    localList.forEach {
                        if (it.eventFavourite?.eventId == event.eventId) {
                            val favouriteId = it?.uid
                            val favourite = Favourite(uid = favouriteId, eventFavourite = event)
                            val favouriteValues = favourite.toMap()

                            val childUpdate : MutableMap<String, Any?> = HashMap()
                            childUpdate["favourites/$favouriteId"] = favouriteValues
                            childUpdate["user-favourites/$userid/$favouriteId"] = favouriteValues

                            database.updateChildren(childUpdate)
                        }
                    }

                }
            })
    }



    // function for deleting petLocation on Firebase, using passed petLocation and user IDs
    override fun delete(userid: String, petLocationId: String) {
        val childDelete : MutableMap<String, Any?> = HashMap()
        childDelete["/petLocations/$petLocationId"] = null
        childDelete["/user-petLocations/$userid/$petLocationId"] = null

        database.updateChildren(childDelete)
    }

    // function for deleting favourite on Firebase, using passed petLocation and user IDs
    override fun deleteFavourite(userid: String, eventId: String) {
        database.child("favourites")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase Favourite error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<Favourite>()
                    val children = snapshot.children
                    children.forEach {
                        val favourite = it.getValue(Favourite::class.java)
                        localList.add(favourite!!)
                    }
                    database.child("favourites")
                        .removeEventListener(this)
                    println("findAllFavourites localList $localList")

                    //in case there are more than one favourites listed for a particular event, a forEach loop is used
                    localList.forEach {
                        if (it.eventFavourite?.eventId == eventId) {
                            val favouriteId = it?.uid
                            val childDelete : MutableMap<String, Any?> = HashMap()
                            childDelete["/favourites/$favouriteId"] = null
                            childDelete["/user-favourites/$userid/$favouriteId"] = null

                            database.updateChildren(childDelete)
                        }
                    }
                }
            })


    }

    //function to update the references in a profile image in case it was already uploaded but Firebase reassigned a reference
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

}