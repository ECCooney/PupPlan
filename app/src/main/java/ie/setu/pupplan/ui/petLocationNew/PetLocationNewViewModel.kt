package ie.setu.pupplan.ui.petLocationNew

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import ie.setu.pupplan.firebase.FirebaseDBManager
import ie.setu.pupplan.models.PetLocationModel


class PetLocationlNewViewModel : ViewModel() {

    private val status = MutableLiveData<Boolean>()

    val observableStatus: LiveData<Boolean>
        get() = status

    fun addPetLocation(firebaseUser: MutableLiveData<FirebaseUser>, petLocation: PetLocationModel) {

        status.value = try {
            FirebaseDBManager.create(firebaseUser, petLocation)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

}