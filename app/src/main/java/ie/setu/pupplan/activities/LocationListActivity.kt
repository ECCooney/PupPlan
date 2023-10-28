package ie.setu.pupplan.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.GridLayoutManager
import android.widget.SearchView
import androidx.activity.result.ActivityResultLauncher
import ie.setu.pupplan.R
import ie.setu.pupplan.adapters.LocationAdapter
import ie.setu.pupplan.adapters.LocationListener
import ie.setu.pupplan.databinding.ActivityLocationListBinding
import ie.setu.pupplan.main.MainApp
import ie.setu.pupplan.models.LocationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LocationListActivity : AppCompatActivity(), LocationListener {
//retrieving a reference to mainapp
    lateinit var app: MainApp
    private lateinit var binding: ActivityLocationListBinding
    private lateinit var refreshIntentLauncher : ActivityResultLauncher<Intent>

//override rules are available here for clarity https://www.geeksforgeeks.org/overriding-rules-in-kotlin/
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityLocationListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //enabling the action bar for the top menu
        binding.toolbar.title = title
        setSupportActionBar(binding.toolbar)
//The Application class in Android is the base class within an Android app that contains all other components such as activities and services. The Application class, or any subclass of the Application class, is instantiated before any other class when the process for your application/package is created.
        app = application as MainApp


        val layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = LocationAdapter(app.locations.findAll(), this)
        updateRecyclerView()
        registerRefreshCallback()

        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Filter the list based on search query
                filterLocationList(newText)
                return true
            }
    })

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

    private fun registerRefreshCallback() {
        refreshIntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { binding.recyclerView.adapter!!.notifyDataSetChanged() }
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

    private fun updateRecyclerView() {
        GlobalScope.launch(Dispatchers.Main) {
            val locations = app.locations.findAll()
            binding.recyclerView.adapter = LocationAdapter(locations, this@LocationListActivity)
        }
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

    private fun filterLocationList(query: String?) {
        GlobalScope.launch(Dispatchers.Main) {
            val filteredList = app.locations.findAll()
                .filter { it.locationCategory.contains(query ?: "", true) }
            (binding.recyclerView.adapter as LocationAdapter).updateList(filteredList)
        }
    }

}
