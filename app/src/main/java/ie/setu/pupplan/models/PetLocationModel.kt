package ie.setu.pupplan.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@IgnoreExtraProperties
@Parcelize
data class PetLocationModel(var uid: String? = "",
                         var title: String = "",
                         var description: String = "",
                         var latitude: Double = 00.00,
                         var longitude: Double = 00.00,
                         var category: String = "",
                         var events: MutableList<NewEvent>? = null,
                         var image: String = "", 
                         var profilePic: String = "",
                         val email: String? = "user@pupplan.com",
): Parcelable

{
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "title" to title,
            "description" to description,
            "latitude" to latitude,
            "longitude" to longitude,
            "category" to category,
            "image" to image,
            "email" to email,
            "profilePic" to profilePic,
            "events" to events,
        )
    }
}
@IgnoreExtraProperties
@Parcelize
data class NewEvent(var eventId: String = "",
                     var petLocationId: String = "",
                    var lat: Double = 00.00,
                    var lng: Double = 00.00,
                    var zoom: Float = 0f,
                     var eventPetLocationName: String = "",
                     var eventTitle: String = "",
                     var eventDescription: String = "",
                    var eventStartDay: Int = 1,
                    var eventStartMonth: Int = 1,
                    var eventStartYear: Int = 2015,
                    var eventCost: String = "",
                     var eventImage: String = "",
                    var eventImage2: String = "",
                    var eventImage3: String = "",
                    var eventUserId: String = "",
                    var eventUserEmail: String = "",
                    var eventPetLocationCategory: String = "",
                    var eventFavourites: MutableList<String>? = null,

    ): Parcelable
{
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "eventId" to eventId,
            "lat" to lat,
            "lng" to lng,
            "zoom" to zoom,
            "petLocationId" to petLocationId,
            "eventDescription" to eventDescription,
            "eventStartDay" to eventStartDay,
            "eventStartMonth" to eventStartMonth,
            "eventStartYear" to eventStartYear,
            "eventImage" to eventImage,
            "eventImage2" to eventImage2,
            "eventImage2" to eventImage2,
            "eventCost" to eventCost,
            "eventUserId" to eventUserId,
            "eventUserEmail" to eventUserEmail,
            "eventFavourites" to eventFavourites,
            "eventPetLocationCategory" to eventPetLocationCategory
        )
    }
}
@IgnoreExtraProperties
@Parcelize
data class Location(var lat: Double = 0.0,
                    var lng: Double = 0.0,
                    var zoom: Float = 0f) : Parcelable
{
@Exclude
fun toMap(): Map<String, Any?> {
    "lat" to lat
    "lng" to lng
    "zoom" to zoom
    return mapOf()
}
}

@IgnoreExtraProperties
@Parcelize
data class Favourite    (var uid: String? = "",
                         var eventFavourite: NewEvent? = null) : Parcelable
{
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "evemtFavourite" to eventFavourite
        )
    }
}
