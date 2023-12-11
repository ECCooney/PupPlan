package ie.setu.pupplan.ui.eventDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import ie.setu.pupplan.firebase.FirebaseDBManager
import ie.setu.pupplan.models.NewEvent
import ie.setu.pupplan.models.PetLocationManager
import ie.setu.pupplan.models.PetLocationModel
import timber.log.Timber

class EventDetailViewModel : ViewModel() {
    private val event = MutableLiveData<NewEvent>()

    lateinit var map : GoogleMap

    var observableEvent: LiveData<NewEvent>
        get() = event
        set(value) {event.value = value.value}

    private val petLocation = MutableLiveData<PetLocationModel>()

    var observablePetLocation: LiveData<PetLocationModel>
        get() = petLocation
        set(value) {
            petLocation.value = value.value
        }

    private lateinit var currentPetLocation : PetLocationModel

    fun getEvent(email: String, petLocationId: String, eventId: String): NewEvent? {
        return PetLocationManager.findEventById(eventId, petLocationId)
    }

    fun getPetLocation(userid: String, id: String) {
        //var currentPetLocation = FirebaseDBManager.findPetLocationById(userid, id, petLocation)
        //println("this is currentpetLocation $currentPetLocation")
        try {
            FirebaseDBManager.findPetLocationById(userid, id, petLocation)
            Timber.i(
                "Detail getPetLocation() Success : ${
                    petLocation.value.toString()
                }"
            )
        } catch (e: Exception) {
            Timber.i("Detail getPetLocation() Error : $e.message")
        }
    }

    fun updatePetLocation(userid: String, id: String, petLocation: PetLocationModel) {
        try {
            FirebaseDBManager.update(userid, id, petLocation)
            Timber.i("Detail update() Success : $petLocation")
        } catch (e: Exception) {
            Timber.i("Detail update() Error : $e.message")
        }
    }

    /*fun updateEvent(userid:String, event: NewEvent, petLocationId: String) {
        var updatedPetLocation = PetLocationManager.findPetLocationById(userid, petLocationId, petLocation)
        PetLocationManager.updateEvent(event, updatedPetLocation!!)
    }

    fun deleteEvent(userid:String, eventId: String, petLocationId: String) {
        var deletedEvent = PetLocationManager.findEventById(eventId, petLocationId)
        println("this is deleted event $deletedEvent")
        var deletedPetLocation = PetLocationManager.findPetLocationById(userid, petLocationId, petLocation)
        println("this is deleted event petLocation $deletedPetLocation")
        PetLocationManager.deleteEvent(deletedEvent!!, deletedPetLocation!!)
    }*/
}