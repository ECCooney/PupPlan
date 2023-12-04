package ie.setu.pupplan.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.squareup.picasso.Picasso

import ie.setu.pupplan.databinding.ActivityLocationMapsBinding
import ie.setu.pupplan.databinding.ContentLocationMapsBinding
import ie.setu.pupplan.main.MainApp

class LocationMapsActivity : AppCompatActivity(), GoogleMap.OnMarkerClickListener {

    private lateinit var binding: ActivityLocationMapsBinding
    private lateinit var contentBinding: ContentLocationMapsBinding
    lateinit var map: GoogleMap
    lateinit var app: MainApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as MainApp

        binding = ActivityLocationMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        contentBinding = ContentLocationMapsBinding.bind(binding.root)
        contentBinding.mapView.onCreate(savedInstanceState)

        contentBinding.mapView.getMapAsync {
            map = it
            configureMap()
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val tag = marker.tag as Long
        val placemark = app.locations.findById(tag)
        contentBinding.currentTitle.text = placemark!!.title
        contentBinding.currentDescription.text = placemark.description
        Picasso.get().load(placemark.image).into(contentBinding.currentImage)
        return false
    }

    private fun configureMap() {
        map.uiSettings.isZoomControlsEnabled = true
        app.locations.findAll().forEach {
            val loc = LatLng(it.lat, it.lng)
            val options = MarkerOptions().title(it.title).position(loc)
            map.addMarker(options)?.tag = it.id
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, it.zoom))
            map.setOnMarkerClickListener(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        contentBinding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        contentBinding.mapView.onLowMemory()
    }

    override fun onPause() {
        super.onPause()
        contentBinding.mapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        contentBinding.mapView.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        contentBinding.mapView.onSaveInstanceState(outState)
    }
}
