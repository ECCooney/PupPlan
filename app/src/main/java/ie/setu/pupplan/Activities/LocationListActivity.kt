package ie.setu.pupplan.Activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ie.setu.pupplan.Models.LocationModel
import ie.setu.pupplan.R
import ie.setu.pupplan.databinding.ActivityLocationsListBinding
import ie.setu.pupplan.databinding.CardLocationBinding
import ie.setu.pupplan.main.MainApp

class LocationsListActivity : AppCompatActivity(){
//retrieving a reference to mainapp
    lateinit var app: MainApp
    private lateinit var binding: ActivityLocationsListBinding

//override rules are available here for clarity https://www.geeksforgeeks.org/overriding-rules-in-kotlin/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationsListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //enabling the action bar for the top menu
        binding.toolbar.title = title
        setSupportActionBar(binding.toolbar)

//The Application class in Android is the base class within an Android app that contains all other components such as activities and services. The Application class, or any subclass of the Application class, is instantiated before any other class when the process for your application/package is created.
        app = application as MainApp

        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = LocationAdapter(app.locations)

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
                notifyItemRangeChanged(0,app.locations.size)
            }
        }
}
// below class is designed to work with a RecyclerView to display a
// list of LocationModel objects in a user interface.
// It handles creating view holders, binding data to them, and determining the number of items in the list.
// To use this adapter, you would typically set it as
// the adapter for a RecyclerView and provide a list of LocationModel objects to display.
class LocationAdapter constructor(private var locations: List<LocationModel>) :
    RecyclerView.Adapter<LocationAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardLocationBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val location = locations[holder.adapterPosition]
        holder.bind(location)
    }

    override fun getItemCount(): Int = locations.size

    class MainHolder(private val binding : CardLocationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(location: LocationModel) {
            binding.locationTitle.text = location.title
            binding.description.text = location.description
        }
    }
}