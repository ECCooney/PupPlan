package ie.setu.pupplan.ui.eventDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.auth.FirebaseUser
import ie.setu.pupplan.firebase.FirebaseDBManager
import ie.setu.pupplan.models.Favourite
import ie.setu.pupplan.models.NewEvent
import ie.setu.pupplan.models.PetLocationModel
import timber.log.Timber

class EventDetailViewModel : ViewModel() {

    lateinit var map : GoogleMap


    private val petLocation = MutableLiveData<PetLocationModel>()

    var observablePetLocation: LiveData<PetLocationModel>
        get() = petLocation
        set(value) {
            petLocation.value = value.value
        }

    private val favourite = MutableLiveData<Favourite>()

    var observableFavourite: LiveData<Favourite>
        get() = favourite
        set(value) {
            favourite.value = value.value
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

    fun addFavourite(firebaseUser: MutableLiveData<FirebaseUser>, favourite: Favourite) {

        try {
            FirebaseDBManager.createFavourite(firebaseUser, favourite)
            Timber.i("Detail update() Success : $favourite")
        } catch (e: Exception) {
            Timber.i("Detail update() Error : $e.message")
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

    fun updateFavourite(userid: String, event: NewEvent) {

        try {
            FirebaseDBManager.updateFavourite(userid, event)
            Timber.i("Detail delete() Success : $event")
        } catch (e: Exception) {
            Timber.i("Detail delete() Error : $e.message")
        }
    }
}