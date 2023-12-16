
package ie.setu.pupplan.models

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import ie.setu.pupplan.models.Favourite
import ie.setu.pupplan.models.NewEvent
import ie.setu.pupplan.models.PetLocationModel

interface PetLocationStore {
    fun findUserAll(userid: String, petLocationsList: MutableLiveData<List<PetLocationModel>>)
    fun create(firebaseUser: MutableLiveData<FirebaseUser>, petLocation: PetLocationModel)
    fun update(userid:String, petLocationId: String, petLocation: PetLocationModel)
    fun delete(userid:String, petLocationId: String)
    fun findEvents(userid: String, petLocationId: String, petLocation: MutableLiveData<PetLocationModel>, eventList: MutableLiveData<List<NewEvent>>)
    fun findEvent(eventsList: MutableLiveData<List<NewEvent>>, eventId: String, event: MutableLiveData<NewEvent>)
    fun findPetLocationById(userid:String, id: String, petLocation: MutableLiveData<PetLocationModel>)
    fun findAll(petLocationsList: MutableLiveData<List<PetLocationModel>>)
    fun findUserEvents(userid: String, petLocationsList: MutableLiveData<List<PetLocationModel>>, eventsList: MutableLiveData<List<NewEvent>>)
    fun findUserEvent(userid: String, petLocationsList: MutableLiveData<List<PetLocationModel>>, eventsList: MutableLiveData<List<NewEvent>>, eventId: String, event: MutableLiveData<NewEvent>)
    fun findAllEvents(eventsList: MutableLiveData<List<NewEvent>>)
    fun createFavourite(firebaseUser: MutableLiveData<FirebaseUser>, favourite: Favourite)
    fun deleteFavourite(userid: String, favouriteId: String)
    fun findAllFavourites(favouritesList: MutableLiveData<List<Favourite>>)
    fun findUserAllFavourites(userid: String, favouritesList: MutableLiveData<List<Favourite>>)
    fun updateFavourite(userid: String, event: NewEvent)
    fun findUserUserFavourites(userid: String, favouritesList: MutableLiveData<List<Favourite>>)
}