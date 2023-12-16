package ie.setu.pupplan.ui.petLocationList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import ie.setu.pupplan.firebase.FirebaseDBManager
import ie.setu.pupplan.models.PetLocationModel
import timber.log.Timber
import java.lang.Exception

class PetLocationListViewModel : ViewModel() {

    private val petLocationsList =
        MutableLiveData<List<PetLocationModel>>()

    val observablePetLocationsList: LiveData<List<PetLocationModel>>
        get() = petLocationsList

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    var readOnly = MutableLiveData(false)

    init {
        load()
    }

    fun load() {
        try {
            readOnly.value = false
            FirebaseDBManager.findUserAll(liveFirebaseUser.value?.uid!!,petLocationsList)
            Timber.i("Report Load Success : ${petLocationsList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Report Load Error : $e.message")
        }
    }

    fun loadAll() {
        try {
            readOnly.value = true
            FirebaseDBManager.findAll(petLocationsList)
            Timber.i("Report LoadAll Success : ${petLocationsList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Report LoadAll Error : $e.message")
        }
    }

    fun delete(userid: String, id: String) {
        try {
            FirebaseDBManager.delete(userid,id)
            Timber.i("Report Delete Success")
        }
        catch (e: Exception) {
            Timber.i("Report Delete Error : $e.message")
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