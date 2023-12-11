package ie.setu.pupplan.ui.map

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import ie.setu.pupplan.R
import ie.setu.pupplan.models.Location
import ie.setu.pupplan.models.NewEvent

class MapEvent : AppCompatActivity(),
    OnMapReadyCallback,  GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    var location = Location()
    var event = NewEvent()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map_event)
        // Passing the details of the location
        location = intent.extras?.getParcelable<Location>("location")!!
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val loc = LatLng(location.lat, location.lng)
        // Setting up the map marker to allow dragging, start in the right position, and show the co-ordinates
        val options = MarkerOptions()
            .title("Event")
            .snippet("GPS : $loc")
            .draggable(true)
            .position(loc)
        map.addMarker(options)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, location.zoom))
        map.setOnMarkerDragListener(this)
        map.setOnMarkerClickListener(this)
    }

    override fun onMarkerDragStart(marker: Marker) {

    }

    override fun onMarkerDrag(marker: Marker)  {

    }

    override fun onMarkerDragEnd(marker: Marker) {
        // Updating co-ordinates after dragging
        location.lat = marker.position.latitude
        location.lng = marker.position.longitude
        location.zoom = map.cameraPosition.zoom
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        // Showing co-ordinates on marker click
        val loc = LatLng(location.lat, location.lng)
        println(loc)
        marker.snippet = "GPS : $loc"
        return false
    }

    override fun onBackPressed() {
        val resultIntent = Intent()
        // Passing the new location upon back click
        resultIntent.putExtra("location", location)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
        super.onBackPressed()
    }


}