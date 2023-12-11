package ie.setu.pupplan.models

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser

interface PetLocationStore {
    fun findUserAll(userid: String, petLocationsList: MutableLiveData<List <PetLocationModel>>)
    fun findPetLocationById2(userid:String, id: String, petLocation: MutableLiveData <PetLocationModel>): PetLocationModel?
    fun create(firebaseUser: MutableLiveData<FirebaseUser>, petLocation: PetLocationModel)
    fun update(userid:String, petLocationId: String, petLocation: PetLocationModel)
    fun delete(userid:String, petLocationId: String)
    fun createEvent(event: NewEvent, petLocation: PetLocationModel)
    fun updateEvent(event: NewEvent, petLocation: PetLocationModel)
    fun deleteEvent(event: NewEvent, petLocation: PetLocationModel)
    fun findEvents(userid: String, petLocationId: String, petLocation: MutableLiveData <PetLocationModel>, eventList: MutableLiveData<List<NewEvent>>)
    fun findEvent(eventsList: MutableLiveData<List<NewEvent>>, eventId: String, event: MutableLiveData<NewEvent>)
    fun findPetLocation(petLocation: PetLocationModel): PetLocationModel?
    fun findSpecificPetLocations(petLocationCategory: String): List <PetLocationModel>
    fun findSpecificTypeEvents(petLocationCategory: String): MutableList<NewEvent>
    fun findPetLocationById(userid:String, id: String, petLocation: MutableLiveData <PetLocationModel>)
    fun findEventById(eventId: String, petLocationId: String): NewEvent?
    fun findAll(petLocationsList: MutableLiveData<List <PetLocationModel>>)
    fun findUserEvents(userid: String, petLocationsList: MutableLiveData<List <PetLocationModel>>, eventsList: MutableLiveData<List<NewEvent>>)
    fun findUserEvent(userid: String, petLocationsList: MutableLiveData<List <PetLocationModel>>, eventsList: MutableLiveData<List<NewEvent>>, eventId: String, event: MutableLiveData<NewEvent>)
}