package ie.setu.pupplan.ui.eventsMap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.auth.FirebaseUser
import ie.setu.pupplan.firebase.FirebaseDBManager
import ie.setu.pupplan.models.NewEvent
import ie.setu.pupplan.models.PetLocationModel
import timber.log.Timber
import java.lang.Exception

class EventsMapViewModel : ViewModel() {

    lateinit var map: GoogleMap

    private val eventsList =
        MutableLiveData<List<NewEvent>>()

    val observableEventsList: LiveData<List<NewEvent>>
        get() = eventsList

    private val petLocationsList =
        MutableLiveData<List<PetLocationModel>>()

    val observablePetLocationsList: LiveData<List<PetLocationModel>>
        get() = petLocationsList

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()


    fun load() {
        try {
            //DonationManager.findAll(liveFirebaseUser.value?.email!!, donationsList)

            FirebaseDBManager.findUserAll(liveFirebaseUser.value?.uid!!, petLocationsList)
            Timber.i("Report Load Success : ${petLocationsList.value.toString()}")
        } catch (e: Exception) {
            Timber.i("Report Load Error : $e.message")
        }
    }

    fun loadAll() {
        try {

            FirebaseDBManager.findAll(petLocationsList)
            Timber.i("Report LoadAll Success : ${petLocationsList.value.toString()}")
        } catch (e: Exception) {
            Timber.i("Report LoadAll Error : $e.message")
        }
    }
}