package ie.setu.pupplan.ui.eventList

import ie.setu.pupplan.R
import android.app.AlertDialog
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ie.setu.pupplan.adapters.EventAdapter
import ie.setu.pupplan.adapters.EventListener
import ie.setu.pupplan.databinding.FragmentEventListBinding
import ie.setu.pupplan.models.Location
import ie.setu.pupplan.models.NewEvent
import ie.setu.pupplan.models.PetLocationModel
import ie.setu.pupplan.ui.auth.LoggedInViewModel
import ie.setu.pupplan.utils.SwipeToDeleteEventCallback
import ie.setu.pupplan.utils.SwipeToEditEventCallback
import ie.setu.pupplan.utils.createLoader
import ie.setu.pupplan.utils.hideLoader
import ie.setu.pupplan.utils.showLoader
import java.util.Calendar

class EventListFragment : Fragment(), EventListener {

    //lateinit var app: Showcase2App
    private var _fragBinding: FragmentEventListBinding? = null
    private val fragBinding get() = _fragBinding!!

    lateinit var loader : AlertDialog
    private val eventListViewModel: EventListViewModel by activityViewModels()
    private val args by navArgs<EventListFragmentArgs>()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    var eventCost = "" // Selected petLocation category for filtering list
    val eventCosts = arrayOf("Show All", "€0-€50K", "€50K-€100K", "€100K-€250K", "€250K-€500K", "€500K-€1M", "€1M+") // Creating array of different event budgets
    var list = ArrayList<NewEvent>()
    var currentPetLocation = PetLocationModel()
    val today = Calendar.getInstance()
    var dateDay = today.get(Calendar.DAY_OF_MONTH)
    var dateMonth = today.get(Calendar.MONTH)
    var dateYear = today.get(Calendar.YEAR)
    var initialPetLocation = Location(0.0, -7.139102, 15f)





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentEventListBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        setupMenu()
        loader = createLoader(requireActivity())


        fragBinding.recyclerView.layoutManager = LinearLayoutManager(activity)
        eventListViewModel.load(args.petLocationid)

        //showLoader(loader,"Downloading Events")
        eventListViewModel.observableEventsList.observe(viewLifecycleOwner, Observer {
                events ->
            events?.let {
                render(ArrayList(events))
                hideLoader(loader)
                checkSwipeRefresh()
            }
        })

        eventListViewModel.observablePetLocation.observe(viewLifecycleOwner, Observer {
                petLocation ->
            petLocation?.let {
                currentPetLocation = petLocation
                getCurrentPetLocation(petLocation)

            }
        })
        var test = eventListViewModel.getPetLocation(loggedInViewModel.liveFirebaseUser.value?.uid!!,
            args.petLocationid)
        println("this is test $test")




        fragBinding.fab.setOnClickListener {

            val action = EventListFragmentDirections.actionEventListFragmentToEventNewFragment(args.petLocationid, initialPetLocation, NewEvent(eventStartDay = dateDay, eventStartMonth = dateMonth, eventStartYear = dateYear))
            findNavController().navigate(action)
        }

        setSwipeRefresh()

        val swipeDeleteHandler = object : SwipeToDeleteEventCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                showLoader(loader,"Deleting Event")
                val adapter = fragBinding.recyclerView.adapter as EventAdapter

                adapter.removeAt(viewHolder.adapterPosition)
                //var eventId = (viewHolder.itemView.tag as NewEvent).eventId
                //println("this is eventId: $eventId")
                //eventListViewModel.delete(eventListViewModel.liveFirebaseUser.value?.uid!!,
                // (viewHolder.itemView.tag as NewEvent).eventId, args.petLocationid)

                if (currentPetLocation.events != null) { // If the petLocation has events (as expected)
                    var eventIdList =
                        arrayListOf<String>() // Create a arrayList variable for storing event IDs
                    currentPetLocation.events!!.forEach { // For each event in the relevant petLocation, add the event ID to the list of event IDs
                        eventIdList += it.eventId
                    }
                    println("this is eventIdList: $eventIdList")
                    var eventId = (viewHolder.itemView.tag as NewEvent).eventId
                    println("this is eventId: $eventId")
                    val index =
                        eventIdList.indexOf(eventId) // Find the index position of the event ID that matches the ID of the event that was passed
                    println("this is index: $index")
                    var petLocationEvents1 =
                        currentPetLocation.events!! // Create a list of the events from the passed petLocation
                    var short =
                        petLocationEvents1.removeAt(index) // Remove the event at the previously found index position within the created event list
                    println("this is short: $short")

                    currentPetLocation.events =
                        java.util.ArrayList(petLocationEvents1) // Assign the new list of events to the found petLocation

                    println("this is updated petLocation events ${currentPetLocation.events}")
                }

                eventListViewModel.updatePetLocation(loggedInViewModel.liveFirebaseUser.value?.uid!!, args.petLocationid, currentPetLocation)

                hideLoader(loader)
            }
        }
        val itemTouchDeleteHelper = ItemTouchHelper(swipeDeleteHandler)
        itemTouchDeleteHelper.attachToRecyclerView(fragBinding.recyclerView)

        val swipeEditHandler = object : SwipeToEditEventCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                onEventClick(viewHolder.itemView.tag as NewEvent)
            }
        }
        val itemTouchEditHelper = ItemTouchHelper(swipeEditHandler)
        itemTouchEditHelper.attachToRecyclerView(fragBinding.recyclerView)

        val spinner = fragBinding.eventCostSpinner
        spinner.adapter = activity?.applicationContext?.let { ArrayAdapter(it, android.R.layout.simple_spinner_item, eventCosts) } as SpinnerAdapter

        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View?, position: Int, id: Long) {
                eventCost = eventCosts[position] // Index of array and spinner position used to select event budget

                println("this is eventCost: $eventCost")
                eventListViewModel.observableEventsList.observe(viewLifecycleOwner, Observer {
                        events ->
                    events?.let {
                        render(ArrayList(events))
                    }
                })
            }
            // No problem if nothing selected
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }




        return root
    }

    private fun getCurrentPetLocation(petLocation: PetLocationModel) {
        currentPetLocation = petLocation
        println("this is newCurrentPetLocation3 $currentPetLocation")
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
                (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
                //getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
                //getActivity().getActionBar().setHomeButtonEnabled(false);
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_event_list, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                when (menuItem.itemId) {
                    R.id.item_home -> {
                        val action = EventListFragmentDirections.actionEventListFragmentToPetLocationListFragment()
                        findNavController().navigate(action)
                    }
                    R.id.item_cancel -> {
                        val action = EventListFragmentDirections.actionEventListFragmentToPetLocationDetailFragment(args.petLocationid)
                        findNavController().navigate(action)
                    }
                }
                return NavigationUI.onNavDestinationSelected(menuItem,
                    requireView().findNavController())
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun render(eventsList: ArrayList<NewEvent>) {
        if (eventCost != "Show All") {
            list = ArrayList(eventsList.filter { p -> p.eventCost == eventCost })
            println("this is internal list $list")
            fragBinding.recyclerView.adapter = EventAdapter(list,this)
        } else {
            list = eventsList
        }
        println("this is petLocationsList $eventsList")
        println("this is list $list")
        fragBinding.recyclerView.adapter = EventAdapter(list,this)
        if (eventsList.isEmpty()) {
            fragBinding.recyclerView.visibility = View.GONE
            fragBinding.eventsNotFound.visibility = View.VISIBLE
        } else {
            fragBinding.recyclerView.visibility = View.VISIBLE
            fragBinding.eventsNotFound.visibility = View.GONE
        }
    }

    override fun onEventClick(event: NewEvent) {
        val action = EventListFragmentDirections.actionEventListFragmentToEventDetailFragment(
            event,
            args.petLocationid,
            Location(lat = event.lat, lng = event.lng, zoom = event.zoom)
        )
        findNavController().navigate(action)
    }

    fun setSwipeRefresh() {
        fragBinding.swiperefresh.setOnRefreshListener {

            if (currentPetLocation.events?.isNotEmpty()!!) {
                fragBinding.swiperefresh.isRefreshing = true
                showLoader(loader,"Downloading Events")
            } else {
                fragBinding.swiperefresh.isRefreshing = false
                fragBinding.recyclerView.visibility = View.GONE
                fragBinding.eventsNotFound.visibility = View.VISIBLE
            }
            eventListViewModel.load(args.petLocationid)


            //eventListViewModel.load(args.petLocationid)
        }
    }

    fun checkSwipeRefresh() {
        if (fragBinding.swiperefresh.isRefreshing)
            fragBinding.swiperefresh.isRefreshing = false
    }

    override fun onResume() {
        super.onResume()
        //showLoader(loader,"Downloading Events")
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner, Observer { firebaseUser ->
            if (firebaseUser != null) {
                eventListViewModel.liveFirebaseUser.value = firebaseUser
                eventListViewModel.load(args.petLocationid)
            }
        })
        //hideLoader(loader)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }


}