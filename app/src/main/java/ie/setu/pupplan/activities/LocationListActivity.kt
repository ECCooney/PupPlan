package ie.setu.pupplan.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import ie.setu.pupplan.R
import ie.setu.pupplan.adapters.LocationAdapter
import ie.setu.pupplan.adapters.LocationListener
import ie.setu.pupplan.databinding.ActivityLocationListBinding
import ie.setu.pupplan.main.MainApp
import ie.setu.pupplan.models.LocationModel

class LocationListActivity : AppCompatActivity(), LocationListener {
//retrieving a reference to mainapp
    lateinit var app: MainApp
    private lateinit var binding: ActivityLocationListBinding

//override rules are available here for clarity https://www.geeksforgeeks.org/overriding-rules-in-kotlin/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //enabling the action bar for the top menu
        binding.toolbar.title = title
        setSupportActionBar(binding.toolbar)

//The Application class in Android is the base class within an Android app that contains all other components such as activities and services. The Application class, or any subclass of the Application class, is instantiated before any other class when the process for your application/package is created.
        app = application as MainApp

        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = LocationAdapter(app.locations.findAll(), this)

    }
//    override the method to load the menu resource:
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //The following two methods implement the menu event handler - and if the event is item_add, we start (launch) the LocationActivity.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_add -> {
                val launcherIntent = Intent(this, LocationActivity::class.java)
                getResult.launch(launcherIntent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                (binding.recyclerView.adapter)?.
                notifyItemRangeChanged(0,app.locations.findAll().size)
            }
        }

    override fun onLocationClick(location: LocationModel) {
        val launcherIntent = Intent(this, LocationActivity::class.java)
        //pass the selected location to the activity (this is enabled by parcelable)
        launcherIntent.putExtra("location_edit", location)
        getClickResult.launch(launcherIntent)
    }

    private val getClickResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                (binding.recyclerView.adapter)?.
                notifyItemRangeChanged(0,app.locations.findAll().size)
            }
        }

}
