package ie.setu.pupplan.ui.eventsMap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
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



    var userEvents = ArrayList<NewEvent>()
    var petLocationList = ArrayList<PetLocationModel>()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()

    private var _fragBinding: FragmentEventsMapBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val fragBinding get() = _fragBinding!!

    //lateinit var map : GoogleMap
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
                //configureMap(petLocations)
            }
        })
        return root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    private fun render(petLocationsList: ArrayList<PetLocationModel>) {

        petLocationList = petLocationsList
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
            eventsMapViewModel.map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, it.zoom))
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
            fragBinding.currentPetLocation.text = "PetLocation: ${event.eventPetLocationName}"
            if (event.eventImage.isNotEmpty()) {
                Picasso.get().load(event.eventImage).resize(200, 200)
                    .into(fragBinding.currentImage)
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

                    }
                    else {
                        eventsMapViewModel.load()

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

    override fun onLowMemory() {
        super.onLowMemory()
        //fragBinding.mapView.onLowMemory()
    }

    override fun onPause() {
        super.onPause()
        //fragBinding.mapView.onPause()
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //fragBinding.mapView.onSaveInstanceState(outState)
    }
}