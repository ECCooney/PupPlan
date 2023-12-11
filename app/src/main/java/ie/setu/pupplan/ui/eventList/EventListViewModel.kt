package ie.setu.pupplan.ui.eventList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import ie.setu.pupplan.firebase.FirebaseDBManager
import ie.setu.pupplan.models.NewEvent
import ie.setu.pupplan.models.PetLocationModel
import timber.log.Timber
import java.lang.Exception

class EventListViewModel : ViewModel() {

    private val eventsList =
        MutableLiveData<List<NewEvent>>()

    val observableEventsList: LiveData<List<NewEvent>>
        get() = eventsList

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    private val petLocation = MutableLiveData <PetLocationModel>()

    var observablePetLocation: LiveData <PetLocationModel>
        get() = petLocation
        set(value) {petLocation.value = value.value}

    fun load(petLocationid: String) {
        try {
            //DonationManager.findAll(liveFirebaseUser.value?.email!!, donationsList)
            FirebaseDBManager.findEvents(liveFirebaseUser.value?.uid!!,petLocationid, petLocation, eventsList)
            Timber.i("Report Load Success : ${eventsList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Report Load Error : $e.message")
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



    /*init {
        load(state["petLocationid"]!!)
    }*/

    /*fun load(petLocationid: String) {
        try {
            //DonationManager.findAll(liveFirebaseUser.value?.email!!, donationsList)
            FirebaseDBManager.findEvents(liveFirebaseUser.value?.uid!!,eventsList)
            Timber.i("Report Load Success : ${eventsList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Report Load Error : $e.message")
        }
    }*/

    fun delete(userid: String, eventid: String, petLocationid: String) {
        try {
            //DonationManager.delete(userid,id)
            FirebaseDBManager.delete(userid,eventid)
            Timber.i("Report Delete Success")
        }
        catch (e: Exception) {
            Timber.i("Report Delete Error : $e.message")
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