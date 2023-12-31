package ie.setu.pupplan.ui.eventDetail

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.squareup.picasso.Picasso
import ie.setu.pupplan.R
import ie.setu.pupplan.databinding.FragmentEventDetailBinding
import ie.setu.pupplan.firebase.FirebaseImageManager
import ie.setu.pupplan.models.Favourite
import ie.setu.pupplan.models.Location
import ie.setu.pupplan.models.NewEvent
import ie.setu.pupplan.models.PetLocationModel
import ie.setu.pupplan.ui.auth.LoggedInViewModel
import ie.setu.pupplan.ui.eventList.EventListViewModel
import ie.setu.pupplan.utils.readImageUri
import ie.setu.pupplan.utils.showImagePicker
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class EventDetailFragment : Fragment(), OnMapReadyCallback {

    private var _fragBinding: FragmentEventDetailBinding? = null
    private val fragBinding get() = _fragBinding!!
    private lateinit var eventViewModel: EventDetailViewModel
    private val args by navArgs<EventDetailFragmentArgs>()
    private val eventListViewModel: EventListViewModel by activityViewModels()
    private val loggedInViewModel: LoggedInViewModel by activityViewModels()
    private lateinit var imageIntentLauncher: ActivityResultLauncher<Intent>
    private lateinit var image2IntentLauncher: ActivityResultLauncher<Intent>
    private lateinit var image3IntentLauncher: ActivityResultLauncher<Intent>
    private lateinit var mapIntentLauncher: ActivityResultLauncher<Intent>
    var eventCost = "Free" // Event cost initial selection
    var image: String = ""
    val eventCosts = arrayOf("Show All", "Free", "€0-€10", "€10-€25", "€25-€50", "€50-€100", "€100+")
    val today = Calendar.getInstance()
    var dateDay = today.get(Calendar.DAY_OF_MONTH)
    var dateMonth = today.get(Calendar.MONTH)
    var dateYear = today.get(Calendar.YEAR)
    var event = NewEvent()
    var currentPetLocation = PetLocationModel()
    var currentEvent = NewEvent()
    var eventImageUpdate: Boolean = false
    var eventImage2Update: Boolean = false
    var eventImage3Update: Boolean = false
    var eventFavouritesList: MutableList<String>? = null
    var eventFavouriteId: String? = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentEventDetailBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        setupMenu()
        registerImagePickerCallback()
        registerMapCallback()
        eventViewModel = ViewModelProvider(this).get(EventDetailViewModel::class.java)
        //eventViewModel.observableEvent.observe(viewLifecycleOwner, Observer { render() })

        eventViewModel.observablePetLocation.observe(viewLifecycleOwner, Observer { petLocation ->
            petLocation?.let {
                currentPetLocation = petLocation
                getCurrentPetLocation(petLocation)
                render(petLocation)
            }
        })

        eventViewModel.observableFavourite.observe(viewLifecycleOwner, Observer { favourite ->
            favourite?.let {
                println("this is my favourite")
            }
        })

        var test = eventViewModel.getPetLocation(
            loggedInViewModel.liveFirebaseUser.value?.uid!!,
            args.petLocationid
        )
        println("this is test $test")
        println("this is args.petLocationid ${args.petLocationid}")

        var location = args.location
        println("this is passed location $location")


        fragBinding.eventLocation.setOnClickListener {
            Timber.i("Set Location Pressed")
        }

        // Set the initial values for location if a new location is set, passing details of location and event to the map activity
        fragBinding.eventLocation.setOnClickListener {

            val location = Location(args.location.lat, args.location.lng, 15f)
            var tempEvent = NewEvent(
                eventId = args.event.eventId,
                eventTitle = fragBinding.eventTitle.text.toString(),
                eventDescription = fragBinding.eventDescription.text.toString(),
                eventCost = eventCost,
                eventImage = event.eventImage,
                eventImage2 = event.eventImage2,
                eventImage3 = event.eventImage3,
                eventPetLocationName = currentPetLocation!!.title,
                petLocationId = args.petLocationid,
                lat = args.location.lat,
                lng = args.location.lng,
                zoom = args.location.zoom,
                eventStartDay = dateDay,
                eventStartMonth = dateMonth,
                eventStartYear = dateYear,
                eventUserId = args.event.eventUserId,
                eventUserEmail = args.event.eventUserEmail,
                eventPetLocationCategory = currentPetLocation.category
            )


            val action =
                EventDetailFragmentDirections.actionEventDetailFragmentToEventMapFragment(
                    location,
                    args.petLocationid,
                    tempEvent
                )
            findNavController().navigate(action)
        }

        setAddFavouriteButtonListener(fragBinding)
        setRemoveFavouriteButtonListener(fragBinding)

        fragBinding.chooseImage.setOnClickListener {
            showImagePicker(imageIntentLauncher)
            fragBinding.chooseImage2.isVisible = true
            fragBinding.eventImage2.isVisible = true
        }

        fragBinding.chooseImage2.setOnClickListener {
            showImagePicker(image2IntentLauncher)
            fragBinding.chooseImage3.isVisible = true
            fragBinding.eventImage3.isVisible = true
        }

        fragBinding.chooseImage3.setOnClickListener {
            showImagePicker(image3IntentLauncher)
        }

        return root;
    }

    private fun getCurrentPetLocation(petLocation: PetLocationModel) {
        currentPetLocation = petLocation
        println("this is newCurrentPetLocation3 $currentPetLocation")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapView4) as SupportMapFragment
        mapFragment.getMapAsync {
            onMapReady(it) // Calling configure map function
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        eventViewModel.map = googleMap
        locationUpdate(args.location.lat, args.location.lng)
    }

    fun locationUpdate(lat: Double, lng: Double) {
        event.lat = lat
        event.lng = lng
        event.zoom = 15f
        eventViewModel.map.clear()
        eventViewModel.map.uiSettings?.setZoomControlsEnabled(true)
        val options =
            MarkerOptions().title(event.eventTitle).position(LatLng(event.lat, event.lng))
        eventViewModel.map.addMarker(options)
        eventViewModel.map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    event.lat,
                    event.lng
                ), event.zoom
            )
        )
    }

    private fun render(petLocation: PetLocationModel) {
        event = args.event
        fragBinding.eventName.setText(event.eventTitle)
        println("this is the currentEvent $event")

        fragBinding.eventTitle.setText(event.eventTitle)
        fragBinding.eventDescription.setText(event.eventDescription)
        fragBinding.eventTitleLocked.setText(event.eventTitle)
        fragBinding.eventDescriptionLocked.setText(event.eventDescription)
        fragBinding.eventCostLocked.setText(event.eventCost)
        val dateComplete = LocalDate.of(
            event.eventStartYear,
            event.eventStartMonth + 1,
            event.eventStartDay
        )
        var formatter = DateTimeFormatter.ofPattern("dd-MMMM-yyyy")
        fragBinding.dateView.setText(dateComplete.format(formatter))
        eventCost = event.eventCost
        eventFavouritesList = event.eventFavourites
        eventFavouriteId =
            eventFavouritesList?.find { p -> p == loggedInViewModel.liveFirebaseUser.value?.uid!! }
        if (eventFavouriteId == null) {
            fragBinding.favouriteAddButton.visibility = View.VISIBLE
            fragBinding.favouriteRemoveButton.visibility = View.GONE
        } else {
            fragBinding.favouriteAddButton.visibility = View.GONE
            fragBinding.favouriteRemoveButton.visibility = View.VISIBLE
        }
        image = event.eventImage
        var formattedLatitude =
            String.format("%.2f", args.location.lat); // Limit the decimal places to two
        fragBinding.eventLatitude.setText("Latitude: $formattedLatitude")
        var formattedLongitude =
            String.format("%.2f", args.location.lng); // Limit the decimal places to two
        fragBinding.eventLongitude.setText("Longitude: $formattedLongitude")

        val spinner = fragBinding.eventCostSpinner
        spinner.adapter = activity?.applicationContext?.let {
            ArrayAdapter(
                it,
                android.R.layout.simple_spinner_item,
                eventCosts
            )
        } as SpinnerAdapter
        val spinnerPosition = eventCosts.indexOf(eventCost)
        spinner.setSelection(spinnerPosition)
        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?, position: Int, id: Long
            ) {
                eventCost =
                    eventCosts[position] // Index of array and spinner position used to select event cost

                println("this is eventCost: $eventCost")
            }

            // No problem if nothing selected
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        // Set up DatePicker
        val datePicker = fragBinding.eventStartDatePicker
        // Set initial values if a start date already exists
        dateDay = event.eventStartDay
        dateMonth = event.eventStartMonth
        dateYear = event.eventStartYear
        datePicker.init(dateYear, dateMonth, dateDay) { view, year, month, day ->
            val month = month
            val msg = "You Selected: $day/$month/$year"
            var dateEventStart = "$day/$month/$year"
            dateDay = day
            dateMonth = month
            dateYear = year
            // Toast is turned off, but can be turned back on
            //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            println("this is dateDay: $dateDay")
            println("this is dateMonth: $dateMonth")
            println("this is dateYear: $dateYear")
            println("this is datePicker: $datePicker")
            println("this is dateEventStart: $dateEventStart")
        }

        if (event.eventImage.isNotEmpty()) {
            Picasso.get()
                .load(event.eventImage)
                .centerCrop()
                .resize(450, 420)
                .into(fragBinding.eventImage)
        }
        if (event.eventImage != "") {
            fragBinding.chooseImage.setText(R.string.button_changeImage)
        }

        if (event.eventImage2.isNotEmpty()) {
            Picasso.get()
                .load(event.eventImage2)
                .centerCrop()
                .resize(450, 420)
                .into(fragBinding.eventImage2)
        }

        if (event.eventImage2 != "") {
            fragBinding.chooseImage2.isVisible = true
            fragBinding.eventImage2.isVisible = true
            fragBinding.chooseImage2.setText(R.string.button_changeImage)
        }
        if (event.eventImage3.isNotEmpty()) {
            Picasso.get()
                .load(event.eventImage3)
                .centerCrop()
                .resize(450, 420)
                .into(fragBinding.eventImage3)
        }

        if (event.eventImage3 != "") {
            fragBinding.chooseImage3.isVisible = true
            fragBinding.eventImage3.isVisible = true
            fragBinding.chooseImage3.setText(R.string.button_changeImage)
        }

        //setting visibilities depending on whether a event belongs to a user or someone else
        if (args.event.eventUserId != loggedInViewModel.liveFirebaseUser.value!!.uid) {
            fragBinding.eventTitle.isVisible = false
            fragBinding.eventDescription.isVisible = false
            fragBinding.eventTitleLocked.isVisible = true
            fragBinding.eventDescriptionLocked.isVisible = true
            fragBinding.eventLocation.isVisible = false
            fragBinding.chooseImage.isVisible = false
            fragBinding.chooseImage2.isVisible = false
            fragBinding.chooseImage3.isVisible = false
            fragBinding.dateView.isVisible = true
            fragBinding.eventStartDatePicker.isVisible = false
            fragBinding.eventCostLocked.isVisible = true
            fragBinding.eventCostSpinner.isVisible = false
        } else {
            fragBinding.eventTitleLocked.isVisible = false
            fragBinding.eventDescriptionLocked.isVisible = false
            fragBinding.dateView.isVisible = false
            fragBinding.eventCostLocked.isVisible = false
        }
    }

    fun setAddFavouriteButtonListener(layout: FragmentEventDetailBinding) {
        layout.favouriteAddButton.setOnClickListener {
            if (eventFavouritesList != null) { // If the event has favourites
                eventFavouritesList!!.add(loggedInViewModel.liveFirebaseUser.value?.uid!!)
            } else {
                eventFavouritesList =
                    mutableListOf(loggedInViewModel.liveFirebaseUser.value?.uid!!)
            }
            if (eventImageUpdate) {
                event.eventImage = FirebaseImageManager.imageUriEvent.value.toString()
            }
            if (eventImage2Update) {
                event.eventImage2 = FirebaseImageManager.imageUriEvent2.value.toString()
            }
            if (eventImage3Update) {
                event.eventImage3 = FirebaseImageManager.imageUriEvent3.value.toString()
            }
            var updatedEvent = NewEvent(
                eventId = args.event.eventId,
                eventTitle = fragBinding.eventTitle.text.toString(),
                eventDescription = fragBinding.eventDescription.text.toString(),
                eventCost = eventCost,
                eventImage = event.eventImage,
                eventImage2 = event.eventImage2,
                eventImage3 = event.eventImage3,
                eventPetLocationName = args.event.eventPetLocationName,
                petLocationId = args.petLocationid,
                lat = args.location.lat,
                lng = args.location.lng,
                eventStartDay = dateDay,
                eventStartMonth = dateMonth,
                eventStartYear = dateYear,
                eventFavourites = eventFavouritesList,
                eventUserId = args.event.eventUserId,
                eventUserEmail = args.event.eventUserEmail,
                eventPetLocationCategory = currentPetLocation.category
            )
            //making sure any updating of events only happens if it is from correct user and related to correct petLocation
            if (args.event.eventUserId == loggedInViewModel.liveFirebaseUser.value!!.uid && args.event.petLocationId == args.petLocationid) {
                if (fragBinding.eventTitle.text.isEmpty()) {
                    Toast.makeText(context, R.string.enter_event_title, Toast.LENGTH_LONG).show()
                } else {
                    if (currentPetLocation.events != null) { // If the petLocation has events (as expected)
                        var eventIdList =
                            arrayListOf<String>() // Create a arrayList variable for storing event IDs
                        currentPetLocation.events!!.forEach { // For each event in the relevant petLocation, add the event ID to the list of event IDs
                            eventIdList += it.eventId
                        }
                        println("this is eventIdList: $eventIdList")
                        var eventId = updatedEvent.eventId
                        println("this is eventId: $eventId")
                        val index =
                            eventIdList.indexOf(updatedEvent.eventId) // Find the index position of the event ID that matches the ID of the event that was passed
                        println("this is index: $index")
                        var petLocationEvents1 =
                            currentPetLocation.events!! // Create a list of the events from the passed petLocation
                        var short =
                            petLocationEvents1.removeAt(index) // Remove the event at the previously found index position within the created event list
                        println("this is short: $short")
                        petLocationEvents1 =
                            petLocationEvents1.plus(updatedEvent) as MutableList<NewEvent> // Add the passed event to the shortened list of events
                        currentPetLocation.events =
                            ArrayList(petLocationEvents1) // Assign the new list of events to the found petLocation

                        println("this is updated petLocation events ${currentPetLocation.events}")
                    }
                    eventViewModel.updatePetLocation(
                        loggedInViewModel.liveFirebaseUser.value?.uid!!,
                        args.petLocationid,
                        currentPetLocation
                    )
                    eventViewModel.addFavourite(
                        loggedInViewModel.liveFirebaseUser,
                        Favourite(eventFavourite = updatedEvent)
                    )
                    val action =
                        EventDetailFragmentDirections.actionEventDetailFragmentToEventListFragment(
                            args.petLocationid
                        )
                    findNavController().navigate(action)
                }
            } else {
                eventViewModel.addFavourite(
                    loggedInViewModel.liveFirebaseUser,
                    Favourite(eventFavourite = updatedEvent)
                )
            }

            fragBinding.favouriteAddButton.visibility = View.GONE
            fragBinding.favouriteRemoveButton.visibility = View.VISIBLE
        }
    }

    fun setRemoveFavouriteButtonListener(layout: FragmentEventDetailBinding) {
        fragBinding.favouriteRemoveButton.setOnClickListener {
            if (eventFavouritesList != null) { // If the event has favourites
                var favouriteIdList =
                    arrayListOf<String>() // Create a arrayList variable for storing event IDs
                eventFavouritesList!!.forEach { // For each event in the relevant petLocation, add the event ID to the list of event IDs
                    favouriteIdList += it
                }
                println("this is favouriteIdList: $favouriteIdList")
                val index =
                    favouriteIdList.indexOf(loggedInViewModel.liveFirebaseUser.value?.uid!!) // Find the index position of the favourite event ID that matches the ID of the event that was passed
                println("this is index: $index")
                var favouriteEvents1 =
                    eventFavouritesList!! // Create a list of the events from the passed petLocation
                var short =
                    favouriteEvents1.removeAt(index) // Remove the event at the previously found index position within the created event list
                println("this is short: $short")
                // Add the passed event to the shortened list of events
                eventFavouritesList =
                    favouriteEvents1 // Assign the new list of events to the found petLocation

                println("this is updated event favourites ${eventFavouritesList}")
            }
            if (eventImageUpdate) {
                event.eventImage = FirebaseImageManager.imageUriEvent.value.toString()
            }
            if (eventImage2Update) {
                event.eventImage2 = FirebaseImageManager.imageUriEvent2.value.toString()
            }
            if (eventImage3Update) {
                event.eventImage3 = FirebaseImageManager.imageUriEvent3.value.toString()
            }
            println("this is updated event favourites again ${eventFavouritesList}")
            val updatedEvent = NewEvent(
                eventId = args.event.eventId,
                eventTitle = fragBinding.eventTitle.text.toString(),
                eventDescription = fragBinding.eventDescription.text.toString(),
                eventCost = eventCost,
                eventImage = event.eventImage,
                eventImage2 = event.eventImage2,
                eventImage3 = event.eventImage3,
                eventPetLocationName = args.event.eventPetLocationName,
                petLocationId = args.petLocationid,
                lat = args.location.lat,
                lng = args.location.lng,
                eventStartDay = dateDay,
                eventStartMonth = dateMonth,
                eventStartYear = dateYear,
                eventFavourites = eventFavouritesList,
                eventUserId = args.event.eventUserId,
                eventUserEmail = args.event.eventUserEmail,
                eventPetLocationCategory = currentPetLocation.category
            )
            //making sure any updating of events only happens if it is from correct user and related to correct petLocation
            if (args.event.eventUserId == loggedInViewModel.liveFirebaseUser.value!!.uid && args.event.petLocationId == args.petLocationid) {
                if (fragBinding.eventTitle.text.isEmpty()) {
                    Toast.makeText(context, R.string.enter_event_title, Toast.LENGTH_LONG).show()
                } else {
                    if (currentPetLocation.events != null) { // If the petLocation has events (as expected)
                        var eventIdList =
                            arrayListOf<String>() // Create a arrayList variable for storing event IDs
                        currentPetLocation.events!!.forEach { // For each event in the relevant petLocation, add the event ID to the list of event IDs
                            eventIdList += it.eventId
                        }
                        println("this is eventIdList: $eventIdList")
                        var eventId = updatedEvent.eventId
                        println("this is eventId: $eventId")
                        val index =
                            eventIdList.indexOf(updatedEvent.eventId) // Find the index position of the event ID that matches the ID of the event that was passed
                        println("this is index: $index")
                        var petLocationEvents1 =
                            currentPetLocation.events!! // Create a list of the events from the passed petLocation
                        var short =
                            petLocationEvents1.removeAt(index) // Remove the event at the previously found index position within the created event list
                        println("this is short: $short")
                        petLocationEvents1 =
                            petLocationEvents1.plus(updatedEvent) as MutableList<NewEvent> // Add the passed event to the shortened list of events
                        currentPetLocation.events =
                            ArrayList(petLocationEvents1) // Assign the new list of events to the found petLocation

                        println("this is updated petLocation events ${currentPetLocation.events}")
                    }
                    eventViewModel.updatePetLocation(
                        loggedInViewModel.liveFirebaseUser.value?.uid!!,
                        args.petLocationid,
                        currentPetLocation
                    )
                    eventViewModel.removeFavourite(
                        loggedInViewModel.liveFirebaseUser.value?.uid!!,
                        args.event.eventId
                    )
                    val action =
                        EventDetailFragmentDirections.actionEventDetailFragmentToEventListFragment(
                            args.petLocationid
                        )
                    findNavController().navigate(action)
                }
            } else {
                eventViewModel.removeFavourite(
                    loggedInViewModel.liveFirebaseUser.value?.uid!!,
                    args.event.eventId
                )
            }
            fragBinding.favouriteAddButton.visibility = View.VISIBLE
            fragBinding.favouriteRemoveButton.visibility = View.GONE
        }
    }

    // Image picker is setup for choosing event image
    private fun registerImagePickerCallback() {
        imageIntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result ->
                when (result.resultCode) {
                    AppCompatActivity.RESULT_OK -> {
                        if (result.data != null) {
                            Timber.i(
                                "Got Result ${
                                    readImageUri(
                                        result.resultCode,
                                        result.data
                                    ).toString()
                                }"
                            )
                            image = result.data!!.data!!.toString()
                            fragBinding.chooseImage.setText(R.string.button_changeImage)
                            FirebaseImageManager
                                .updateEventImage(
                                    loggedInViewModel.liveFirebaseUser.value!!.uid,
                                    readImageUri(result.resultCode, result.data),
                                    fragBinding.eventImage,
                                    false, "eventImage"
                                )
                            event.eventImage = result.data!!.data!!.toString()
                            println("this is event.eventImage ${event.eventImage}")
                            eventImageUpdate = true
                        } // end of if
                    }

                    AppCompatActivity.RESULT_CANCELED -> {}
                    else -> {}
                }
            }
        image2IntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result ->
                when (result.resultCode) {
                    AppCompatActivity.RESULT_OK -> {
                        if (result.data != null) {
                            Timber.i(
                                "Got Result ${
                                    readImageUri(
                                        result.resultCode,
                                        result.data
                                    ).toString()
                                }"
                            )
                            fragBinding.chooseImage.setText(R.string.button_changeImage)
                            FirebaseImageManager
                                .updateEventImage(
                                    loggedInViewModel.liveFirebaseUser.value!!.uid,
                                    readImageUri(result.resultCode, result.data),
                                    fragBinding.eventImage2,
                                    false, "eventImage2"
                                )
                            event.eventImage2 = result.data!!.data!!.toString()
                            eventImage2Update = true
                        } // end of if
                    }

                    AppCompatActivity.RESULT_CANCELED -> {}
                    else -> {}
                }
            }
        // Image launcher for 3rd event image
        image3IntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result ->
                when (result.resultCode) {
                    AppCompatActivity.RESULT_OK -> {
                        if (result.data != null) {
                            Timber.i(
                                "Got Result ${
                                    readImageUri(
                                        result.resultCode,
                                        result.data
                                    ).toString()
                                }"
                            )
                            fragBinding.chooseImage.setText(R.string.button_changeImage)
                            FirebaseImageManager
                                .updateEventImage(
                                    loggedInViewModel.liveFirebaseUser.value!!.uid,
                                    readImageUri(result.resultCode, result.data),
                                    fragBinding.eventImage3,
                                    false, "eventImage3"
                                )
                            event.eventImage3 = result.data!!.data!!.toString()
                            eventImage3Update = true
                        } // end of if
                    }

                    AppCompatActivity.RESULT_CANCELED -> {}
                    else -> {}
                }
            }
    }

    // Map is setup for selecting a location of the event
    private fun registerMapCallback() {
        mapIntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result ->
                when (result.resultCode) {
                    AppCompatActivity.RESULT_OK -> {
                        if (result.data != null) {
                            Timber.i("Got Location ${result.data.toString()}")
                            val location =
                                result.data!!.extras?.getParcelable<Location>("location")!!
                            Timber.i("Location == $location")
                            // Setting event co-ordinates based on location passed from map
                            event.lat = location.lat
                            event.lng = location.lng
                            event.zoom = location.zoom
                            // Set shown co-ordinates based on location passed from map
                            var formattedLatitude = String.format(
                                "%.2f",
                                location.lat
                            ); // Limit the decimal places to two
                            fragBinding.eventLatitude.setText("Latitude: $formattedLatitude")
                            var formattedLongitude = String.format(
                                "%.2f",
                                location.lng
                            ); // Limit the decimal places to two
                            fragBinding.eventLongitude.setText("Longitude: $formattedLongitude")
                        } // end of if
                    }

                    AppCompatActivity.RESULT_CANCELED -> {}
                    else -> {}
                }
            }
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
                (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(
                    false
                )
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_event_detail, menu)
                //preventing users saving if event doesn't belong to them or if it's related to the wrong petLocation.
                if (args.event.eventUserId != loggedInViewModel.liveFirebaseUser.value!!.uid || args.petLocationid != args.event.petLocationId) {
                    menu.getItem(1).isVisible = false
                    menu.getItem(2).isVisible = false
                    menu.getItem(3).isVisible = false
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                when (menuItem.itemId) {
                    R.id.item_home -> {
                        findNavController().navigate(R.id.action_eventDetailFragment_to_petLocationListFragment)
                    }

                    R.id.item_cancel -> {
                        val action =
                            EventDetailFragmentDirections.actionEventDetailFragmentToEventListFragment(
                                args.petLocationid
                            )
                        findNavController().navigate(action)
                    }

                    R.id.item_event_save -> {
                        if (fragBinding.eventTitle.text.isEmpty()) {
                            Toast.makeText(context, R.string.enter_event_title, Toast.LENGTH_LONG)
                                .show()
                        } else {
                            if (eventImageUpdate) {
                                event.eventImage =
                                    FirebaseImageManager.imageUriEvent.value.toString()
                            }
                            if (eventImage2Update) {
                                event.eventImage2 =
                                    FirebaseImageManager.imageUriEvent2.value.toString()
                            }
                            if (eventImage3Update) {
                                event.eventImage3 =
                                    FirebaseImageManager.imageUriEvent3.value.toString()
                            }
                            var updatedEvent = NewEvent(
                                eventId = args.event.eventId,
                                eventTitle = fragBinding.eventTitle.text.toString(),
                                eventDescription = fragBinding.eventDescription.text.toString(),
                                eventCost = eventCost,
                                eventImage = event.eventImage,
                                eventImage2 = event.eventImage2,
                                eventImage3 = event.eventImage3,
                                eventPetLocationName = args.event.eventPetLocationName,
                                petLocationId = args.petLocationid,
                                lat = args.location.lat,
                                lng = args.location.lng,
                                eventStartDay = dateDay,
                                eventStartMonth = dateMonth,
                                eventStartYear = dateYear,
                                eventFavourites = eventFavouritesList,
                                eventUserId = args.event.eventUserId,
                                eventUserEmail = args.event.eventUserEmail,
                                eventPetLocationCategory = currentPetLocation.category
                            )

                            if (currentPetLocation.events != null) { // If the petLocation has events (as expected)
                                var eventIdList =
                                    arrayListOf<String>() // Create a arrayList variable for storing event IDs
                                currentPetLocation.events!!.forEach { // For each event in the relevant petLocation, add the event ID to the list of event IDs
                                    eventIdList += it.eventId
                                }
                                println("this is eventIdList: $eventIdList")
                                var eventId = updatedEvent.eventId
                                println("this is eventId: $eventId")
                                val index =
                                    eventIdList.indexOf(updatedEvent.eventId) // Find the index position of the event ID that matches the ID of the event that was passed
                                println("this is index: $index")
                                var petLocationEvents1 =
                                    currentPetLocation.events!! // Create a list of the events from the passed petLocation
                                var short =
                                    petLocationEvents1.removeAt(index) // Remove the event at the previously found index position within the created event list
                                println("this is short: $short")
                                petLocationEvents1 =
                                    petLocationEvents1.plus(updatedEvent) as MutableList<NewEvent> // Add the passed event to the shortened list of events
                                currentPetLocation.events =
                                    ArrayList(petLocationEvents1) // Assign the new list of events to the found petLocation

                                println("this is updated petLocation events ${currentPetLocation.events}")
                            }

                            eventViewModel.updatePetLocation(
                                loggedInViewModel.liveFirebaseUser.value?.uid!!,
                                args.petLocationid,
                                currentPetLocation
                            )
                            eventViewModel.updateFavourite(
                                args.event.eventUserId,
                                updatedEvent
                            )
                        }
                        val action =
                            EventDetailFragmentDirections.actionEventDetailFragmentToEventListFragment(
                                args.petLocationid
                            )
                        findNavController().navigate(action)
                    }

                    R.id.item_event_delete -> {
                        if (currentPetLocation.events != null) { // If the petLocation has events (as expected)
                            var eventIdList =
                                arrayListOf<String>() // Create a arrayList variable for storing event IDs
                            currentPetLocation.events!!.forEach { // For each event in the relevant petLocation, add the event ID to the list of event IDs
                                eventIdList += it.eventId
                            }
                            println("this is eventIdList: $eventIdList")
                            var eventId = args.event.eventId
                            println("this is eventId: $eventId")
                            val index =
                                eventIdList.indexOf(args.event.eventId) // Find the index position of the event ID that matches the ID of the event that was passed
                            println("this is index: $index")
                            var petLocationEvents1 =
                                currentPetLocation.events!! // Create a list of the events from the passed petLocation
                            var short =
                                petLocationEvents1.removeAt(index) // Remove the event at the previously found index position within the created event list
                            println("this is short: $short")

                            currentPetLocation.events =
                                ArrayList(petLocationEvents1) // Assign the new list of events to the found petLocation

                            println("this is updated petLocation events ${currentPetLocation.events}")
                        }

                        eventViewModel.updatePetLocation(
                            args.event.eventUserId,
                            args.event.petLocationId,
                            currentPetLocation
                        )
                        eventViewModel.removeFavourite(
                            args.event.eventUserId,
                            args.event.eventId
                        )

                        val action =
                            EventDetailFragmentDirections.actionEventDetailFragmentToEventListFragment(
                                args.petLocationid
                            )
                        findNavController().navigate(action)
                    }
                }
                return NavigationUI.onNavDestinationSelected(
                    menuItem,
                    requireView().findNavController()
                )
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }
}