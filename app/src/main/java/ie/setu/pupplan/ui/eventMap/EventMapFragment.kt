package ie.setu.pupplan.ui.eventMap

import EventMapViewModel
import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import ie.setu.pupplan.R
import ie.setu.pupplan.databinding.FragmentPetlocationNewBinding
import ie.setu.pupplan.databinding.FragmentEventMapBinding

import ie.setu.pupplan.firebase.FirebaseImageManager
import ie.setu.pupplan.models.Location
import ie.setu.pupplan.models.NewEvent
import ie.setu.pupplan.models.PetLocationModel
import ie.setu.pupplan.ui.petLocationDetail.PetLocationDetailFragmentDirections
import ie.setu.pupplan.ui.eventDetail.EventDetailFragmentArgs
import ie.setu.pupplan.ui.eventNew.EventNewViewModel


class EventMapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerDragListener,
    GoogleMap.OnMarkerClickListener {
    private lateinit var eventMapViewModel: EventMapViewModel
    private val args by navArgs<EventDetailFragmentArgs>()
    private var _fragBinding: FragmentEventMapBinding? = null
    private val fragBinding get() = _fragBinding!!

    //lateinit var map : GoogleMap
    var location = Location()
    var event = NewEvent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        location = args.location
        _fragBinding = FragmentEventMapBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        eventMapViewModel = ViewModelProvider(this).get(EventMapViewModel::class.java)
        setButtonListener(fragBinding)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapView2) as SupportMapFragment
        mapFragment.getMapAsync{
            onMapReady(it)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        eventMapViewModel.map = googleMap
        val loc = LatLng(location.lat, location.lng)
        // Setting up the map marker to allow dragging, start in the right position, and show the co-ordinates
        val options = MarkerOptions()
            .title("Event")
            .snippet("GPS : $loc")
            .draggable(true)
            .position(loc)
        eventMapViewModel.map.addMarker(options)
        eventMapViewModel.map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, location.zoom))
        eventMapViewModel.map.setOnMarkerDragListener(this)
        eventMapViewModel.map.setOnMarkerClickListener(this)
    }

    override fun onMarkerDragStart(marker: Marker) {

    }

    override fun onMarkerDrag(marker: Marker)  {
        fragBinding.lat.setText("Lat: " + "%.6f".format(marker.position.latitude))
        fragBinding.lng.setText("Lng: " + "%.6f".format(marker.position.longitude))
    }

    override fun onMarkerDragEnd(marker: Marker) {
        // Updating co-ordinates after dragging
        location.lat = marker.position.latitude
        location.lng = marker.position.longitude
        location.zoom = eventMapViewModel.map.cameraPosition.zoom
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        // Showing co-ordinates on marker click
        val loc = LatLng(location.lat, location.lng)
        println(loc)
        marker.snippet = "GPS : $loc"
        return false
    }

    fun setButtonListener(layout: FragmentEventMapBinding) {
        layout.fab.setOnClickListener {
            val resultIntent = Intent()
            // Passing the new location upon back click
            resultIntent.putExtra("location", location)
            if (args.event.eventId.isEmpty()) {
                val action = EventMapFragmentDirections.actionEventMapFragmentToEventNewFragment(
                    args.petLocationid,
                    location,
                    args.event
                )
                findNavController().navigate(action)
            } else {
                val action = EventMapFragmentDirections.actionEventMapFragmentToEventDetailFragment(
                    args.event,
                    args.petLocationid,
                    location
                )
                findNavController().navigate(action)
            }
        }
    }


}



