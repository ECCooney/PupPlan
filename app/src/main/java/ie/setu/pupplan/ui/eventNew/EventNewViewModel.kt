package ie.setu.pupplan.ui.eventNew

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import ie.setu.pupplan.firebase.FirebaseDBManager
import ie.setu.pupplan.models.NewEvent
import ie.setu.pupplan.models.PetLocationModel
import timber.log.Timber

class EventNewViewModel : ViewModel() {

    private val status = MutableLiveData<Boolean>()

    lateinit var map : GoogleMap

    val observableStatus: LiveData<Boolean>
        get() = status

    private val petLocation = MutableLiveData <PetLocationModel>()

    var observablePetLocation: LiveData <PetLocationModel>
        get() = petLocation
        set(value) {
            petLocation.value = value.value
        }

    fun addEvent(userid: String, event: NewEvent) {

        //PetLocationManager.create(petLocation)
        status.value = try {
            //DonationManager.create(donation)
            //FirebaseDBManager.create(firebaseUser, event)

            true
        } catch (e: IllegalArgumentException) {
            false
        }
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
}