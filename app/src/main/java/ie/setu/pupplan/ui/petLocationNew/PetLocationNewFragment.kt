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
import ie.setu.pupplan.helpers.showImagePicker
import ie.setu.pupplan.models.PetLocationModel
import ie.setu.pupplan.databinding.FragmentPetlocationNewBinding
import ie.setu.pupplan.ui.auth.LoggedInViewModel
import ie.setu.pupplan.ui.petLocationList.PetLocationListViewModel
import ie.setu.pupplan.utils.readImageUri
import ie.setu.pupplan.utils.showImagePicker
import timber.log.Timber

class PetLocationNewFragment : Fragment() {

    //var totalDonated = 0
    private var _fragBinding: FragmentPetlocationNewBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val fragBinding get() = _fragBinding!!
    private lateinit var petLocationViewModel: PetLocationNewViewModel
    private val petLocationListViewModel: PetLocationListViewModel by activityViewModels()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private lateinit var imageIntentLauncher : ActivityResultLauncher<Intent>
    var petLocationCategory = "" // Current petLocation category
    var image: String = ""
    var imageLoad: Boolean = false
    val petLocationCategorys = arrayOf("Hotel", "Outdoor Area", "DayCare", "Hotel", "Restaurant") // Creating array of different petLocation categorys

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _fragBinding = FragmentPetlocationNewBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        setupMenu()
        registerImagePickerCallback()
        petLocationViewModel = ViewModelProvider(this).get(PetLocationNewViewModel::class.java)
        petLocationViewModel.observableStatus.observe(viewLifecycleOwner, Observer {
                status -> status?.let { render(status) }
        })

        val spinner = fragBinding.petLocationCategorySpinner
        spinner.adapter = activity?.applicationContext?.let { ArrayAdapter(it, android.R.layout.simple_spinner_item, petLocationCategorys) } as SpinnerAdapter

        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View?, position: Int, id: Long) {
                petLocationCategory = petLocationCategorys[position] // Index of array and spinner position used to select petLocation category

                println("this is petLocationCategory: $petLocationCategory")
            }
            // No problem if nothing selected
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        //fragBinding.amountPicker.setOnValueChangedListener { _, _, newVal ->
        //Display the newly selected number to paymentAmount
        //    fragBinding.paymentAmount.setText("$newVal")
        //}
        setButtonListener(fragBinding)

        fragBinding.chooseImage.setOnClickListener {
            showImagePicker(imageIntentLauncher)
        }

        return root;
    }

    private fun render(status: Boolean) {
        when (status) {
            true -> {
                view?.let {
                    //Uncomment this if you want to immediately return to Report
                    //findNavController().navigate(R.id.action_petLocationNewFragment_to_petLocationListFragment)
                }
            }
            false -> Toast.makeText(context,getString(R.string.donationError),Toast.LENGTH_LONG).show()
        }
    }

    fun setButtonListener(layout: FragmentPetlocationNewBinding) {
        layout.btnAdd.setOnClickListener {
            if (layout.petLocationTitle.text.isEmpty()) {
                Toast.makeText(context,R.string.enter_petLocation_title, Toast.LENGTH_LONG).show()
            } else {
                if(imageLoad) {
                    image = FirebaseImageManager.imageUriPetLocation.value.toString()
                }
                println(loggedInViewModel.liveFirebaseUser)
                petLocationViewModel.addPetLocation(loggedInViewModel.liveFirebaseUser, PetLocationModel(title = layout.petLocationTitle.text.toString(), description = layout.description.text.toString(), category = petLocationCategory,
                    email = loggedInViewModel.liveFirebaseUser.value?.email!!, profilePic = FirebaseImageManager.imageUri.value.toString(), image = image))
                println(petLocationCategory)
            }
            findNavController().navigate(R.id.action_petLocationNewFragment_to_petLocationListFragment)
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
                            // Picasso used to get images, as well as standardising sizes and cropping as necessary
                            /*Picasso.get()
                                .load(image)
                                .centerCrop()
                                .resize(450, 420)
                                .into(fragBinding.image)*/
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
                menuInflater.inflate(R.menu.menu_petlocation_new, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                when (menuItem.itemId) {
                    R.id.item_home -> {
                        findNavController().navigate(R.id.action_petLocationNewFragment_to_petLocationListFragment)
                    }
                    R.id.item_petLocation_save -> {
                        if (fragBinding.petLocationTitle.text.isEmpty()) {
                            Toast.makeText(context,R.string.enter_petLocation_title, Toast.LENGTH_LONG).show()
                        } else {
                            petLocationViewModel.addPetLocation(loggedInViewModel.liveFirebaseUser, PetLocationModel(title = fragBinding.petLocationTitle.text.toString(), description = fragBinding.description.text.toString(), category = petLocationCategory, image = image,
                                email = loggedInViewModel.liveFirebaseUser.value?.email!!))
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