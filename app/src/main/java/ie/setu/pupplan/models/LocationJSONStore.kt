package ie.setu.pupplan.models

import android.content.Context
import android.net.Uri
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import ie.setu.pupplan.helpers.*
import timber.log.Timber
import java.lang.reflect.Type
import java.util.*

const val JSON_FILE = "locations.json"
val gsonBuilder: Gson = GsonBuilder().setPrettyPrinting()
    .registerTypeAdapter(Uri::class.java, UriParser())
    .create()
val listType: Type = object : TypeToken<ArrayList<LocationModel>>() {}.type

fun generateRandomId(): Long {
    return Random().nextLong()
}

class LocationJSONStore(private val context: Context) : LocationStore {

    var locations = mutableListOf<LocationModel>()

    init {
        if (exists(context, JSON_FILE)) {
            deserialize()
        }
    }

    override fun findAll(): MutableList<LocationModel> {
        logAll()
        return locations
    }

    override fun create(location: LocationModel) {
        location.id = generateRandomId()
        locations.add(location)
        serialize()
    }


    override fun update(location: LocationModel) {
        var foundLocation: LocationModel? = locations.find { l -> l.id == location.id}
        if (foundLocation != null) {
            foundLocation.title = location.title
            foundLocation.description = location.description
            foundLocation.image= location.image
            foundLocation.locationCategory = location.locationCategory
            foundLocation.openingTime = location.openingTime
            foundLocation.closingTime = location.closingTime
            foundLocation.lat = location.lat
            foundLocation.lng = location.lng
            foundLocation.zoom = location.zoom
        }
        serialize()
    }
    override fun delete(location: LocationModel) {
        locations.remove(location)
        serialize()
    }

    private fun serialize() {
        val jsonString = gsonBuilder.toJson(locations, listType)
        write(context, JSON_FILE, jsonString)
    }

    private fun deserialize() {
        val jsonString = read(context, JSON_FILE)
        locations = gsonBuilder.fromJson(jsonString, listType)
    }

    private fun logAll() {
        locations.forEach { Timber.i("$it") }
    }
}

class UriParser : JsonDeserializer<Uri>,JsonSerializer<Uri> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Uri {
        return Uri.parse(json?.asString)
    }

    override fun serialize(
        src: Uri?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(src.toString())
    }
}