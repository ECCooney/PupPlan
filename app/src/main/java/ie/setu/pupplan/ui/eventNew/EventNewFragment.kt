package ie.setu.pupplan.ui.eventNew

import ie.setu.pupplan.R
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.squareup.picasso.Picasso
import ie.setu.pupplan.databinding.FragmentEventNewBinding
import ie.setu.pupplan.firebase.FirebaseImageManager
import ie.setu.pupplan.models.Location
import ie.setu.pupplan.models.NewEvent
import ie.setu.pupplan.models.PetLocationModel
import ie.setu.pupplan.ui.auth.LoggedInViewModel
import ie.setu.pupplan.ui.eventList.EventListViewModel
import ie.setu.pupplan.utils.checkLocationPermissions
import ie.setu.pupplan.utils.readImageUri
import ie.setu.pupplan.utils.showImagePicker
import timber.log.Timber
import java.util.Calendar
import java.util.Random


class EventNewFragment : Fragment(), OnMapReadyCallback {

    private var _fragBinding: FragmentEventNewBinding? = null
    private val fragBinding get() = _fragBinding!!
    private lateinit var eventViewModel: EventNewViewModel
    private val args by navArgs<EventNewFragmentArgs>()
    private val eventListViewModel: EventListViewModel by activityViewModels()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private lateinit var imageIntentLauncher : ActivityResultLauncher<Intent>
    private lateinit var image2IntentLauncher : ActivityResultLauncher<Intent>
    private lateinit var image3IntentLauncher : ActivityResultLauncher<Intent>
    private lateinit var mapIntentLauncher : ActivityResultLauncher<Intent>
    var eventCost = "€0-€50K" // Event cost initial selection
    var image: String = ""
    val eventCosts = arrayOf("€0-€50K", "€50K-€100K", "€100K-€250K", "€250K-€500K", "€500K-€1M", "€1M+") // Creating array of different event costs
    val today = Calendar.getInstance()
    var dateDay = today.get(Calendar.DAY_OF_MONTH)
    var dateMonth = today.get(Calendar.MONTH)
    var dateYear = today.get(Calendar.YEAR)
    var event = NewEvent()
    var currentPetLocation = PetLocationModel()
    var initialLocation = Location(52.245696, -7.139102, 15f)
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient //from https://www.tutorialspoint.com/how-to-show-current-location-on-a-google-map-on-android-using-kotlin
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    @SuppressLint("MissingPermission")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _fragBinding = FragmentEventNewBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        setupMenu()
        registerImagePickerCallback()
        eventViewModel = ViewModelProvider(this).get(EventNewViewModel::class.java)
        fusedLocationProviderClient = requireActivity().let{LocationServices.getFusedLocationProviderClient(it)}


        eventViewModel.observablePetLocation.observe(viewLifecycleOwner, Observer {
                petLocation ->
            petLocation?.let {
                currentPetLocation = petLocation
                getCurrentPetLocation(petLocation)
                render(petLocation)
            }
        })

        var test = eventViewModel.getPetLocation(loggedInViewModel.liveFirebaseUser.value?.uid!!,
            args.petLocationid)
        println("this is test $test")

        val spinner = fragBinding.eventCostSpinner
        spinner.adapter = activity?.applicationContext?.let { ArrayAdapter(it, android.R.layout.simple_spinner_item, eventCosts) } as SpinnerAdapter

        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View?, position: Int, id: Long) {
                eventCost = eventCosts[position] // Index of array and spinner position used to select event cost

                println("this is eventCost: $eventCost")
            }
            // No problem if nothing selected
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        fragBinding.eventLocation.setOnClickListener {
            Timber.i("Set Location Pressed")
        }

        // Set the initial values for location if a new location is set, passing details of location and event to the map activity
        fragBinding.eventLocation.setOnClickListener {
            var location = args.location
            if (args.location.lat.equals(0.0)) {
                val task = fusedLocationProviderClient.lastLocation
                task.addOnSuccessListener { myLocation ->
                    location.lat = myLocation.latitude
                    location.lng = myLocation.longitude
                }
            } else {
                location = args.location
            }

            val action = EventNewFragmentDirections.actionEventNewFragmentToEventMapFragment(location, args.petLocationid,NewEvent(eventTitle = fragBinding.eventTitle.text.toString(), eventDescription = fragBinding.eventDescription.text.toString(),
                eventCost = eventCost, eventImage = event.eventImage, eventImage2 = event.eventImage2, eventImage3 = event.eventImage3,
                petLocationId = args.petLocationid, lat = location.lat, lng = location.lng, zoom = 15f, eventUserId = loggedInViewModel.liveFirebaseUser.value?.uid!!, eventUserEmail = loggedInViewModel.liveFirebaseUser.value?.email!!,
                eventStartDay = dateDay, eventStartMonth = dateMonth, eventStartYear = dateYear, eventPetLocationName = currentPetLocation.title, eventPetLocationCategory = currentPetLocation.category))
            findNavController().navigate(action)
        }

        // Set up DatePicker
        val datePicker = fragBinding.eventStartDatePicker
        // Set initial values if a start date already exists

        datePicker.init(dateYear, dateMonth, dateDay) { view, year, month, day ->
            val month = month + 1
            val msg = "You Selected: $day/$month/$year"
            var dateEventStart = "$day/$month/$year"
            dateDay = day
            dateMonth = month
            dateYear = year

            println ("this is dateDay: $dateDay")
            println ("this is dateMonth: $dateMonth")
            println ("this is dateYear: $dateYear")
            println("this is datePicker: $datePicker")
            println("this is dateEventStart: $dateEventStart")
        }

        setButtonListener(fragBinding)

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

        fragBinding.chooseImage2.isVisible = false // Initially hidden until previous image set
        fragBinding.eventImage2.isVisible = false // Initially hidden until previous image set
        fragBinding.chooseImage3.isVisible = false // Initially hidden until previous image set
        fragBinding.eventImage3.isVisible = false // Initially hidden until previous image set

        doPermissionLauncher()

        return root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapView3) as SupportMapFragment
        mapFragment.getMapAsync {
            onMapReady(it) // Calling configure map function
        }
    }
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        eventViewModel.map = googleMap
        if (args.location.lat.equals(0.0) ) {
            if (activity?.let { checkLocationPermissions(it) } == true) {
                val task = fusedLocationProviderClient.lastLocation
                task.addOnSuccessListener { myLocation ->
                    locationUpdate(myLocation.latitude, myLocation.longitude)
                    println("this is myLocation $myLocation")
                }

                println("permission is true")
            } else {
                locationUpdate(52.245696, -7.139102)
                println("permission is false")
            }
        } else {
            locationUpdate(args.location.lat, args.location.lng)
        }
    }

    fun locationUpdate(lat: Double, lng: Double) {
        println("this is lat $lat and lng $lng")
        event.lat = lat
        event.lng = lng
        event.zoom = 15f
        eventViewModel.map.clear()
        eventViewModel.map.uiSettings?.setZoomControlsEnabled(true)
        val options = MarkerOptions().title(event.eventTitle).position(LatLng(event.lat, event.lng))
        eventViewModel.map.addMarker(options)
        eventViewModel.map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(event.lat, event.lng), event.zoom))
    }

    @SuppressLint("MissingPermission")
    private fun render(petLocation: PetLocationModel) {
        event = args.event
        println("this is the currentEvent $event")
        fragBinding.eventTitle.setText(event.eventTitle)
        fragBinding.eventDescription.setText(event.eventDescription)
        eventCost = event.eventCost
        image = event.eventImage
        if (args.location.lat.equals(0.0)) {
            val task = fusedLocationProviderClient.lastLocation
            task.addOnSuccessListener { myLocation ->
                locationUpdate(myLocation.latitude, myLocation.longitude)
                println("this is myLocation $myLocation")
                var formattedLatitude = String.format("%.2f", myLocation.latitude); // Limit the decimal places to two
                fragBinding.eventLatitude.setText("Latitude: $formattedLatitude")
                var formattedLongitude = String.format("%.2f", myLocation.longitude); // Limit the decimal places to two
                fragBinding.eventLongitude.setText("Longitude: $formattedLongitude")
            }
        } else {
            var formattedLatitude = String.format("%.2f", args.location.lat); // Limit the decimal places to two
            fragBinding.eventLatitude.setText("Latitude: $formattedLatitude")
            var formattedLongitude = String.format("%.2f", args.location.lng); // Limit the decimal places to two
            fragBinding.eventLongitude.setText("Longitude: $formattedLongitude")
        }

        val spinner = fragBinding.eventCostSpinner
        spinner.adapter = activity?.applicationContext?.let { ArrayAdapter(it, android.R.layout.simple_spinner_item, eventCosts) } as SpinnerAdapter
        val spinnerPosition = eventCosts.indexOf(eventCost)
        spinner.setSelection(spinnerPosition)

        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View?, position: Int, id: Long) {
                eventCost = eventCosts[position] // Index of array and spinner position used to select event cost

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
            var dateEventStart = "$day/${month+1}/$year"
            dateDay = day
            dateMonth = month
            dateYear = year
            // Toast is turned off, but can be turned back on
            //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            println ("this is dateDay: $dateDay")
            println ("this is dateMonth: $dateMonth")
            println ("this is dateYear: $dateYear")
            println("this is datePicker: $datePicker")
            println("this is dateEventStart: $dateEventStart")
        }

        if (event.eventImage.isNotEmpty()) {
            Picasso.get()
                .load(event.eventImage)
                .centerCrop()
                .resize(450, 420)
                .into(fragBinding.eventImage) }
        if (event.eventImage != "") {
            fragBinding.chooseImage.setText(R.string.button_changeImage)
        }

        if (event.eventImage2.isNotEmpty()) {
            Picasso.get()
                .load(event.eventImage2)
                .centerCrop()
                .resize(450, 420)
                .into(fragBinding.eventImage2)}

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
                .into(fragBinding.eventImage3)}

        if (event.eventImage3 != "") {
            fragBinding.chooseImage3.isVisible = true
            fragBinding.eventImage3.isVisible = true
            fragBinding.chooseImage3.setText(R.string.button_changeImage)
        }
    }

    private fun getCurrentPetLocation(petLocation: PetLocationModel) {
        currentPetLocation = petLocation
        println("this is newCurrentPetLocation3 $currentPetLocation")
    }
    @SuppressLint("MissingPermission")
    fun setButtonListener(layout: FragmentEventNewBinding) {
        if (args.location.lat.equals(0.0)) {
            println("this was the path")
            val task = fusedLocationProviderClient.lastLocation
            task.addOnSuccessListener { myLocation ->
                initialLocation.lat = myLocation.latitude
                initialLocation.lng = myLocation.longitude
            }
        } else {
            println("that was the path")
            initialLocation = args.location
        }
        println("the updated location saved $initialLocation")
    }

    // Image picker is setup for choosing event image
    private fun registerImagePickerCallback() {
        imageIntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result ->
                when(result.resultCode){
                    AppCompatActivity.RESULT_OK -> {
                        if (result.data != null) {
                            Timber.i("Got Result ${readImageUri(result.resultCode, result.data).toString()}")
                            image = result.data!!.data!!.toString()
                            fragBinding.chooseImage.setText(R.string.button_changeImage)
                            FirebaseImageManager
                                .updateEventImage(loggedInViewModel.liveFirebaseUser.value!!.uid,
                                    readImageUri(result.resultCode, result.data),
                                    fragBinding.eventImage,
                                    false, "eventImage")
                            event.eventImage = result.data!!.data!!.toString()
                            println("this is event.eventImage ${event.eventImage}")
                        } // end of if
                    }
                    AppCompatActivity.RESULT_CANCELED -> { } else -> { }
                }
            }
        image2IntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result ->
                when(result.resultCode){
                    AppCompatActivity.RESULT_OK -> {
                        if (result.data != null) {
                            Timber.i("Got Result ${readImageUri(result.resultCode, result.data).toString()}")
                            fragBinding.chooseImage2.setText(R.string.button_changeImage)
                            FirebaseImageManager
                                .updateEventImage(loggedInViewModel.liveFirebaseUser.value!!.uid,
                                    readImageUri(result.resultCode, result.data),
                                    fragBinding.eventImage2,
                                    false, "eventImage2")
                            event.eventImage2 = result.data!!.data!!.toString()
                        } // end of if
                    }
                    AppCompatActivity.RESULT_CANCELED -> { } else -> { }
                }
            }
        // Image launcher for 3rd event image
        image3IntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result ->
                when(result.resultCode){
                    AppCompatActivity.RESULT_OK -> {
                        if (result.data != null) {
                            Timber.i("Got Result ${readImageUri(result.resultCode, result.data).toString()}")
                            fragBinding.chooseImage3.setText(R.string.button_changeImage)
                            FirebaseImageManager
                                .updateEventImage(loggedInViewModel.liveFirebaseUser.value!!.uid,
                                    readImageUri(result.resultCode, result.data),
                                    fragBinding.eventImage3,
                                    false, "eventImage3")
                            event.eventImage3 = result.data!!.data!!.toString()
                        } // end of if
                    }
                    AppCompatActivity.RESULT_CANCELED -> { } else -> { }
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
                            val location = result.data!!.extras?.getParcelable<Location>("location")!!
                            Timber.i("Location == $location")
                            // Setting event co-ordinates based on location passed from map
                            event.lat = location.lat
                            event.lng = location.lng
                            event.zoom = location.zoom
                            // Set shown co-ordinates based on location passed from map
                            var formattedLatitude = String.format("%.2f", location.lat); // Limit the decimal places to two
                            fragBinding.eventLatitude.setText("Latitude: $formattedLatitude")
                            var formattedLongitude = String.format("%.2f", location.lng); // Limit the decimal places to two
                            fragBinding.eventLongitude.setText("Longitude: $formattedLongitude")
                        } // end of if
                    }
                    AppCompatActivity.RESULT_CANCELED -> { } else -> { }
                }
            }
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
                (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_event_new, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                when (menuItem.itemId) {
                    R.id.item_home -> {
                        findNavController().navigate(R.id.action_eventNewFragment_to_petLocationListFragment)
                    }
                    R.id.item_cancel -> {
                        val action = EventNewFragmentDirections.actionEventNewFragmentToEventListFragment(args.petLocationid)
                        findNavController().navigate(action)
                    }
                    R.id.item_event_save -> {
                        if (fragBinding.eventTitle.text.isEmpty()) {
                            Toast.makeText(context,R.string.enter_event_title, Toast.LENGTH_LONG).show()
                        } else {
                            if (event.eventImage.isNotEmpty()) {
                                event.eventImage = FirebaseImageManager.imageUriEvent.value.toString()
                            }
                            if (event.eventImage2.isNotEmpty()) {
                                event.eventImage2 = FirebaseImageManager.imageUriEvent2.value.toString()
                            }
                            if (event.eventImage3.isNotEmpty()) {
                                event.eventImage3 = FirebaseImageManager.imageUriEvent3.value.toString()
                            }

                            val updatedEvent = NewEvent(eventId = generateRandomId().toString(), eventTitle = fragBinding.eventTitle.text.toString(), eventDescription = fragBinding.eventDescription.text.toString(),
                                eventCost = eventCost, eventImage = event.eventImage, eventImage2 = event.eventImage2, eventImage3 = event.eventImage3,
                                petLocationId = args.petLocationid, lat = initialLocation.lat, lng = initialLocation.lng, zoom = 15f, eventPetLocationCategory = currentPetLocation.category,
                                eventStartDay = dateDay, eventStartMonth = dateMonth, eventStartYear = dateYear, eventPetLocationName = currentPetLocation.title, eventUserId = loggedInViewModel.liveFirebaseUser.value?.uid!!.toString(), eventUserEmail = loggedInViewModel.liveFirebaseUser.value?.email!!)
                            if (currentPetLocation.events == null) {
                                currentPetLocation.events = listOf(updatedEvent).toMutableList()
                            } else {
                                currentPetLocation.events = currentPetLocation.events?.plus(updatedEvent)?.toMutableList()
                            }

                            println("this is updatedEvent $updatedEvent")
                            println("this is updated currentevents ${currentPetLocation.events}")

                            println("this is updated currentPetLocation $currentPetLocation")
                            eventViewModel.updatePetLocation(loggedInViewModel.liveFirebaseUser.value?.uid!!, args.petLocationid, currentPetLocation)
                        }
                        val action = EventNewFragmentDirections.actionEventNewFragmentToEventListFragment(args.petLocationid)
                        findNavController().navigate(action)
                    }
                }
                return NavigationUI.onNavDestinationSelected(menuItem,
                    requireView().findNavController())
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }

    override fun onResume() {
        super.onResume()

    }

    internal fun generateRandomId(): Long {
        return Random().nextLong()
    }

    @SuppressLint("MissingPermission")
    private fun doPermissionLauncher() {
        Timber.i("permission check called")
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission())
            { isGranted: Boolean ->
                if (isGranted) {
                    println("permission granted")
                } else {
                    initialLocation = Location(52.245696, -7.139102, 15f)
                    println("permission not granted")
                }
            }
    }



}