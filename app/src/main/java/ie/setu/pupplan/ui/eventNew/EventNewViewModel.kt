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

    lateinit var map : GoogleMap

    private val petLocation = MutableLiveData<PetLocationModel>()

    var observablePetLocation: LiveData<PetLocationModel>
        get() = petLocation
        set(value) {
            petLocation.value = value.value
        }

    fun getPetLocation(userid: String, id: String) {
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