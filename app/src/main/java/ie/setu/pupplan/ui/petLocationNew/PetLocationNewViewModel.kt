package ie.setu.pupplan.ui.petLocationNew

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import ie.setu.pupplan.firebase.FirebaseDBManager
import ie.setu.pupplan.models.PetLocationModel


class PetLocationNewViewModel : ViewModel() {

    private val status = MutableLiveData<Boolean>()

    val observableStatus: LiveData<Boolean>
        get() = status

    fun addPetLocation(firebaseUser: MutableLiveData<FirebaseUser>, petLocation: PetLocationModel) {
        //PetLocationManager.create(petLocation)
        status.value = try {
            //petLocation.image = FirebaseImageManager.imageUriPetLocation.value.toString()
            FirebaseDBManager.create(firebaseUser, petLocation)

            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

}