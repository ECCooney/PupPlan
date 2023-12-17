import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.auth.FirebaseUser
import ie.setu.pupplan.firebase.FirebaseDBManager
import ie.setu.pupplan.models.Favourite
import ie.setu.pupplan.models.PetLocationModel
import timber.log.Timber
import java.lang.Exception

class FavouritesMapViewModel : ViewModel() {
    lateinit var map : GoogleMap

    private val favouritesList =
        MutableLiveData<List<Favourite>>()

    val observableFavouritesList: LiveData<List<Favourite>>
        get() = favouritesList

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    private val petLocationsList =
        MutableLiveData<List<PetLocationModel>>()

    val observablePetLocationsList: LiveData<List<PetLocationModel>>
        get() = petLocationsList

    fun load() {
        try {
            FirebaseDBManager.findUserFavourites(liveFirebaseUser.value?.uid!!,favouritesList)
            FirebaseDBManager.findUserAll(liveFirebaseUser.value?.uid!!,petLocationsList)
            Timber.i("Report Load Success : ${favouritesList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Report Load Error : $e.message")
        }
    }

    fun loadAll() {
        try {

            FirebaseDBManager.findAllFavourites(liveFirebaseUser.value?.uid!!,favouritesList)
            Timber.i("Report LoadAll Success : ${favouritesList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Report LoadAll Error : $e.message")
        }
    }
}