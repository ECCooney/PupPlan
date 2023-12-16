package ie.setu.pupplan.ui.petLocationDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ie.setu.pupplan.firebase.FirebaseDBManager
import ie.setu.pupplan.models.PetLocationModel
import timber.log.Timber

class PetLocationDetailViewModel : ViewModel() {

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

    fun deletePetLocation(userid: String, id: String) {
        try {
            FirebaseDBManager.delete(userid, id)
            Timber.i("Detail delete() Success : $petLocation")
        } catch (e: Exception) {
            Timber.i("Detail delete() Error : $e.message")
        }
    }

    fun removeFavourite(userid: String, eventId: String) {
        try {
            FirebaseDBManager.deleteFavourite(userid, eventId)
            Timber.i("Detail delete() Success : $eventId")
        } catch (e: Exception) {
            Timber.i("Detail delete() Error : $e.message")
        }
    }

}


