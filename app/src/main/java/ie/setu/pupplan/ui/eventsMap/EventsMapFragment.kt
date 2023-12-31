package ie.setu.pupplan.ui.eventsMap

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
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.squareup.picasso.Picasso
import ie.setu.pupplan.R
import ie.setu.pupplan.databinding.FragmentEventsMapBinding
import ie.setu.pupplan.models.Location
import ie.setu.pupplan.models.NewEvent
import ie.setu.pupplan.models.PetLocationModel
import ie.setu.pupplan.ui.auth.LoggedInViewModel

class EventsMapFragment : Fragment(), GoogleMap.OnMarkerClickListener, OnMapReadyCallback {

    private lateinit var eventsMapViewModel: EventsMapViewModel
    var enabler: String = ""
    var enablerSwitch: Boolean = true
    val petLocationCategorys = arrayOf("Show All", "Hotel", "Pet Shop", "Outdoor Area", "Bar/Restaurant")
    var petLocationCategory = "Show All" // Selected petLocation type for filtering list
    var userEvents = ArrayList<NewEvent>()
    var petLocationList = ArrayList<PetLocationModel>()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private var _fragBinding: FragmentEventsMapBinding? = null
    private val fragBinding get() = _fragBinding!!
    var location = Location()
    var event = NewEvent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _fragBinding = FragmentEventsMapBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        println("testing testing")
        setupMenu()

        eventsMapViewModel = ViewModelProvider(this).get(EventsMapViewModel::class.java)
        //eventsMapViewModel.load()

        var test = eventsMapViewModel.load()
        println("this is test $test")

        eventsMapViewModel.observablePetLocationsList.observe(viewLifecycleOwner, Observer {
                petLocations ->
            petLocations?.let {
                render(petLocations as ArrayList<PetLocationModel>)
                println("this is the petLocations on the map $petLocations")
                configureEnabler(petLocations)
            }
        })

        val spinner = fragBinding.eventCategorySpinner
        val adapter = activity?.applicationContext?.let { ArrayAdapter(it, android.R.layout.simple_spinner_item, petLocationCategorys) } as SpinnerAdapter
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View?, position: Int, id: Long) {
                petLocationCategory = petLocationCategorys[position]
                println("this is petLocationCategory: $petLocationCategory")
                userEvents.clear()
                eventsMapViewModel.observablePetLocationsList.observe(viewLifecycleOwner, Observer {
                        petLocations ->
                    petLocations?.let {
                        render(petLocations as ArrayList<PetLocationModel>)
                        println("testing this is working")
                    }
                })
            }
            // No problem if nothing selected
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
        return root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun configureEnabler(petLocationsList: ArrayList<PetLocationModel>) {
        if (petLocationsList.isNotEmpty() && enablerSwitch) {
            val userPetLocations = petLocationsList.filter { p -> p.email == loggedInViewModel.liveFirebaseUser.value!!.email }
            val firstPetLocation = userPetLocations[0]
            enabler = firstPetLocation.uid!!
        }
        println("this is enabler $enabler")
        enablerSwitch = false
    }

    private fun render(petLocationsList: ArrayList<PetLocationModel>) {
        if (petLocationCategory == "Show All") {
            petLocationList = petLocationsList
        } else {
            petLocationList = ArrayList(petLocationsList.filter { p -> p.category == petLocationCategory })
        }
        println("this is petLocationList $petLocationList")
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync {
            onMapReady(it) // Calling configure map function
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        eventsMapViewModel.map = googleMap
        println("test  petLocationList $petLocationList")
        petLocationList.forEach {
            val petLocationEvents = it.events?.toMutableList()
            if (petLocationEvents != null) {
                userEvents += petLocationEvents.toMutableList()
            }
        }
        eventsMapViewModel.map.setOnMarkerClickListener(this)
        eventsMapViewModel.map.uiSettings.setZoomControlsEnabled(true)
        println("this is userEvents: $userEvents")
        eventsMapViewModel.map.clear()
        userEvents.forEach { // If show all selected, use function for finding all events from JSON file
            val loc = LatLng(it.lat, it.lng)
            val options = MarkerOptions().title(it.eventTitle).position(loc)
            eventsMapViewModel.map.addMarker(options)?.tag = it.eventId
            eventsMapViewModel.map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15f))
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val tag =marker.tag as String
        val event = userEvents.find { p -> p.eventId == tag }
        println("this is event: $event")
        // Display information about a event upon clicking on tag, based on event ID
        if (event != null) {
            fragBinding.currentTitle.text = event.eventTitle
            fragBinding.currentDescription.text = event.eventDescription
            fragBinding.currentEmail.text = event.eventUserEmail
            if (event.eventImage.isNotEmpty()) {
                Picasso.get().load(event.eventImage).resize(200, 200)
                    .into(fragBinding.currentImage)
            }
            //user can only access other users' petLocations if they have their own petLocation to begin with
            if (enabler != "") {
                //enabler is related to petLocation id, if the event belongs to the user then the petLocation id passed is that of the event
                if (event.eventUserId == loggedInViewModel.liveFirebaseUser.value!!.uid) {
                    enabler = event.petLocationId
                }
                //otherwise, the enabler remains a random petLocation id related to the current user because if it related to another user's petLocation they wouldn't have authorisation
                fragBinding.cardView.setOnClickListener {
                    val action = EventsMapFragmentDirections.actionEventsMapFragmentToEventDetailFragment(
                        event,
                        enabler,
                        Location(lat = event.lat, lng = event.lng, zoom = 15f)
                    )
                    findNavController().navigate(action)
                }
            }

        }
        return false
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_events_map, menu)

                val item = menu.findItem(R.id.toggleEvents) as MenuItem
                item.setActionView(R.layout.togglebutton_layout)
                val toggleEvents: SwitchCompat = item.actionView!!.findViewById(R.id.toggleButton)
                toggleEvents.isChecked = false

                toggleEvents.setOnCheckedChangeListener { _, isChecked ->
                    userEvents.clear()
                    if (isChecked) {
                        eventsMapViewModel.loadAll()
                        fragBinding.mapTitle.setText("All Events")

                    }
                    else {
                        eventsMapViewModel.load()
                        fragBinding.mapTitle.setText("My Events")

                    }
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
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
        //fragBinding.mapView.onResume()
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner, Observer { firebaseUser ->
            if (firebaseUser != null) {
                eventsMapViewModel.liveFirebaseUser.value = firebaseUser
                eventsMapViewModel.load()
            }
        })
    }
}