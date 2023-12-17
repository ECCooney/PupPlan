package ie.setu.pupplan.ui.petLocationNew

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
import ie.setu.pupplan.R
import ie.setu.pupplan.firebase.FirebaseImageManager
import ie.setu.pupplan.models.PetLocationModel
import ie.setu.pupplan.databinding.FragmentPetlocationNewBinding
import ie.setu.pupplan.ui.auth.LoggedInViewModel
import ie.setu.pupplan.ui.maps.MapsViewModel
import ie.setu.pupplan.utils.readImageUri
import ie.setu.pupplan.utils.showImagePicker
import timber.log.Timber

class PetLocationNewFragment : Fragment() {

    //var totalDonated = 0
    private var _fragBinding: FragmentPetlocationNewBinding? = null
    private val fragBinding get() = _fragBinding!!
    private lateinit var petLocationViewModel: PetLocationlNewViewModel
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private val mapsViewModel: MapsViewModel by activityViewModels()
    private lateinit var imageIntentLauncher : ActivityResultLauncher<Intent>
    var petLocationCategory = "" // Current petLocation type
    var image: String = ""
    var imageLoad: Boolean = false
    val petLocationCategorys = arrayOf("Show All", "Hotel", "Pet Shop", "Outdoor Area", "Bar/Restaurant") // Creating array of different petLocation types

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _fragBinding = FragmentPetlocationNewBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        setupMenu()
        registerImagePickerCallback()
        petLocationViewModel = ViewModelProvider(this).get(PetLocationlNewViewModel::class.java)
        petLocationViewModel.observableStatus.observe(viewLifecycleOwner, Observer {
                status -> status?.let { render(status) }
        })

        val spinner = fragBinding.petLocationCategorySpinner
        spinner.adapter = activity?.applicationContext?.let { ArrayAdapter(it, android.R.layout.simple_spinner_item, petLocationCategorys) } as SpinnerAdapter
        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View?, position: Int, id: Long) {
                petLocationCategory = petLocationCategorys[position] // Index of array and spinner position used to select petLocation type

                println("this is petLocationCategory: $petLocationCategory")
            }
            // No problem if nothing selected
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
        fragBinding.chooseImage.setOnClickListener {
            showImagePicker(imageIntentLauncher)
        }

        return root;
    }

    private fun render(status: Boolean) {
        when (status) {
            true -> {
                view?.let {
                    //Uncomment this if you want to immediately return to PetLocation List
                    //findNavController().navigate(R.id.action_petLocationNewFragment_to_petLocationListFragment)
                }
            }
            false -> Toast.makeText(context,getString(R.string.petLocationError),Toast.LENGTH_LONG).show()
        }
    }

    // Image picker is setup for choosing petLocation image
    private fun registerImagePickerCallback() {
        imageIntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result ->
                when(result.resultCode){
                    AppCompatActivity.RESULT_OK -> {
                        if (result.data != null) {
                            Timber.i("Got Result ${readImageUri(result.resultCode, result.data).toString()}")
                            image = result.data!!.data!!.toString()
                            fragBinding.chooseImage.setText(R.string.button_changeImage)
                            FirebaseImageManager
                                .updatePetLocationImage(loggedInViewModel.liveFirebaseUser.value!!.uid,
                                    readImageUri(result.resultCode, result.data),
                                    fragBinding.petLocationImage,
                                    false)
                            imageLoad = true
                        } // end of if
                    }
                    AppCompatActivity.RESULT_CANCELED -> { } else -> { }
                }
            }
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_location_new, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                when (menuItem.itemId) {
                    ie.setu.pupplan.R.id.item_cancel -> {
                        findNavController().navigate(ie.setu.pupplan.R.id.action_petLocationNewFragment_to_petLocationListFragment)
                    }
                    R.id.item_petLocation_save -> {
                        if (fragBinding.petLocationTitle.text.isEmpty()) {
                            Toast.makeText(context,R.string.enter_petLocation_title, Toast.LENGTH_LONG).show()
                        } else {
                            if(imageLoad) {
                                image = FirebaseImageManager.imageUriPetLocation.value.toString()
                            }
                            println(loggedInViewModel.liveFirebaseUser)
                            petLocationViewModel.addPetLocation(loggedInViewModel.liveFirebaseUser, PetLocationModel(title = fragBinding.petLocationTitle.text.toString(), description = fragBinding.description.text.toString(), category = petLocationCategory,
                                email = loggedInViewModel.liveFirebaseUser.value?.email!!, profilePic = FirebaseImageManager.imageUri.value.toString(), image = image, latitude= mapsViewModel.currentLocation.value!!.latitude,
                                longitude = mapsViewModel.currentLocation.value!!.longitude))
                            println(petLocationCategory)
                        }
                        findNavController().navigate(R.id.action_petLocationNewFragment_to_petLocationListFragment)
                    }
                }
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

    }
}