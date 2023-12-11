package ie.setu.pupplan.ui.petLocationList

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
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ie.setu.pupplan.adapters.PetLocationAdapter
import ie.setu.pupplan.adapters.PetLocationClickListener
import ie.setu.pupplan.databinding.FragmentPetlocationListBinding
import ie.setu.pupplan.main.MainApp
import ie.setu.pupplan.models.PetLocationModel
import ie.setu.pupplan.ui.auth.LoggedInViewModel
import ie.setu.pupplan.utils.SwipeToDeleteCallback
import ie.setu.pupplan.utils.SwipeToEditCallback
import ie.setu.pupplan.utils.createLoader
import ie.setu.pupplan.utils.hideLoader
import ie.setu.pupplan.utils.showLoader
import java.util.*

class PetLocationListFragment : Fragment(), PetLocationClickListener {

    lateinit var app: MainApp
    private var _fragBinding: FragmentPetlocationListBinding? = null
    private val fragBinding get() = _fragBinding!!
    val petLocationCategorys = arrayOf("Show All", "Hotel", "Pet Shop", "Outdoor Area", "Bar")
    lateinit var loader : AlertDialog
    private val petLocationListViewModel: PetLocationListViewModel by activityViewModels()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    var petLocationCategory = "" // Selected petLocation category for filtering list
    var list = ArrayList <PetLocationModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentPetlocationListBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        setupMenu()
        loader = createLoader(requireActivity())

        fragBinding.recyclerView.layoutManager = LinearLayoutManager(activity)
        //petLocationListViewModel = ViewModelProvider(this).get(PetLocationListViewModel::class.java)
        showLoader(loader,"Downloading PetLocations")
        petLocationListViewModel.observablePetLocationsList.observe(viewLifecycleOwner, Observer {
                petLocations ->
            petLocations?.let {
                render(petLocations as ArrayList <PetLocationModel>)
                hideLoader(loader)
                checkSwipeRefresh()
            }
        })

        fragBinding.fab.setOnClickListener {
            val action = PetLocationListFragmentDirections.actionPetLocationListFragmentToPetLocationNewFragment()
            findNavController().navigate(action)
        }

        setSwipeRefresh()

        val swipeDeleteHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                showLoader(loader,"Deleting PetLocation")
                val adapter = fragBinding.recyclerView.adapter as PetLocationAdapter
                adapter.removeAt(viewHolder.adapterPosition)
                petLocationListViewModel.delete(petLocationListViewModel.liveFirebaseUser.value?.uid!!,
                    (viewHolder.itemView.tag as PetLocationModel).uid!!)
                hideLoader(loader)
            }
        }
        val itemTouchDeleteHelper = ItemTouchHelper(swipeDeleteHandler)
        itemTouchDeleteHelper.attachToRecyclerView(fragBinding.recyclerView)

        val swipeEditHandler = object : SwipeToEditCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                onPetLocationClick(viewHolder.itemView.tag as PetLocationModel)
            }
        }
        val itemTouchEditHelper = ItemTouchHelper(swipeEditHandler)
        itemTouchEditHelper.attachToRecyclerView(fragBinding.recyclerView)

        val spinner = fragBinding.petLocationCategorySpinner
        val adapter = activity?.applicationContext?.let { ArrayAdapter(it, R.layout.simple_spinner_item, petLocationCategorys) } as SpinnerAdapter
        spinner.adapter = adapter
        //val spinnerPosition = petLocationCategorys.indexOf(petLocationCategory)
        //spinner.setSelection(spinnerPosition)
        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View?, position: Int, id: Long) {
                petLocationCategory = petLocationCategorys[position] // Index of array and spinner position used to select petLocation category
                // The toast message was taken out because it was annoying, but can be reinstated if wanted
                /*Toast.makeText(this@PetLocationActivity,
                    getString(R.string.selected_item) + " " +
                            "" + petLocationCategorys[position], Toast.LENGTH_SHORT).show()*/
                println("this is petLocationCategory: $petLocationCategory")
                petLocationListViewModel.observablePetLocationsList.observe(viewLifecycleOwner, Observer {
                        petLocations ->
                    petLocations?.let {
                        render(petLocations as ArrayList <PetLocationModel>)
                        println("testing this is working")
                    }
                })

            }
            // No problem if nothing selected
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        return root
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_petlocation_list, menu)

                val item = menu.findItem(R.id.togglePetLocations) as MenuItem
                item.setActionView(R.layout.togglebutton_layout)
                val togglePetLocations: SwitchCompat = item.actionView!!.findViewById(R.id.toggleButton)
                togglePetLocations.isChecked = false

                togglePetLocations.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) petLocationListViewModel.loadAll()
                    else petLocationListViewModel.load()
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                return NavigationUI.onNavDestinationSelected(menuItem,
                    requireView().findNavController())
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun render(petLocationsList: ArrayList <PetLocationModel>) {
        if (petLocationCategory != "Show All") {
            list = ArrayList(petLocationsList.filter { p -> p.category == petLocationCategory })
            println("this is internal list $list")
            fragBinding.recyclerView.adapter = PetLocationAdapter(list,this, petLocationListViewModel.readOnly.value!!)
        } else {
            list = petLocationsList
        }
        println("this is petLocationsList $petLocationsList")
        println("this is list $list")
        fragBinding.recyclerView.adapter = PetLocationAdapter(list,this, petLocationListViewModel.readOnly.value!!)
        if (petLocationsList.isEmpty()) {
            fragBinding.recyclerView.visibility = View.GONE
            fragBinding.petLocationsNotFound.visibility = View.VISIBLE
        } else {
            fragBinding.recyclerView.visibility = View.VISIBLE
            fragBinding.petLocationsNotFound.visibility = View.GONE
        }
    }

    override fun onPetLocationClick(petLocation: PetLocationModel) {
        val action = PetLocationListFragmentDirections.actionPetLocationListFragmentToPetLocationDetailFragment(
            petLocation.uid!!
        )
        if(!petLocationListViewModel.readOnly.value!!) {
            findNavController().navigate(action)
        }
    }

    fun setSwipeRefresh() {
        fragBinding.swiperefresh.setOnRefreshListener {
            fragBinding.swiperefresh.isRefreshing = true
            showLoader(loader,"Downloading PetLocations")
            if (petLocationListViewModel.readOnly.value!!) {
                petLocationListViewModel.loadAll()
            } else {
                petLocationListViewModel.load()
            }
        }
    }

    fun checkSwipeRefresh() {
        if (fragBinding.swiperefresh.isRefreshing)
            fragBinding.swiperefresh.isRefreshing = false
    }

    override fun onResume() {
        super.onResume()
        showLoader(loader,"Downloading PetLocations")
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner, Observer { firebaseUser ->
            if (firebaseUser != null) {
                petLocationListViewModel.liveFirebaseUser.value = firebaseUser
                petLocationListViewModel.load()
            }
        })
        //hideLoader(loader)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }
}