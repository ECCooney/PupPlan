package ie.setu.pupplan.ui.petLocationDetail

import ie.setu.pupplan.R
import android.content.Intent
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
import android.widget.Toast
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
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import com.squareup.picasso.Picasso
import ie.setu.pupplan.databinding.FragmentPetlocationDetailBinding
import ie.setu.pupplan.firebase.FirebaseImageManager
import ie.setu.pupplan.models.NewEvent
import ie.setu.pupplan.models.PetLocationModel
import ie.setu.pupplan.ui.auth.LoggedInViewModel
import ie.setu.pupplan.ui.petLocationList.PetLocationListViewModel
import ie.setu.pupplan.utils.readImageUri
import ie.setu.pupplan.utils.showImagePicker
import timber.log.Timber

class PetLocationDetailFragment : Fragment() {

    private lateinit var detailViewModel: PetLocationDetailViewModel
    private val args by navArgs<PetLocationDetailFragmentArgs>()
    private var _fragBinding: FragmentPetlocationDetailBinding? = null
    private val fragBinding get() = _fragBinding!!
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    private val petLocationListViewModel : PetLocationListViewModel by activityViewModels()
    private lateinit var imageIntentLauncher : ActivityResultLauncher<Intent>
    var currentPetLocation = PetLocationModel()
    var imageLoad: Boolean = false
    var events: MutableList<NewEvent>? = null
    var testing: Boolean = false
    var petLocationCategory = "" // Current petLocation type
    var image: String = ""
    val petLocationCategorys = arrayOf("Show All", "Hotel", "Pet Shop", "Outdoor Area", "Bar/Restaurant") // Creating array of different petLocation types

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentPetlocationDetailBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        registerImagePickerCallback()

        detailViewModel = ViewModelProvider(this).get(PetLocationDetailViewModel::class.java)

        detailViewModel.observablePetLocation.observe(viewLifecycleOwner, Observer {
                petLocation ->
            petLocation?.let {
                render(petLocation)
                getCurrentPetLocation(petLocation)
                currentPetLocation = petLocation
                println("this is currentPetLocation $currentPetLocation")
            }
        })

        println("this is currentPetLocation2 $currentPetLocation")

        var test = detailViewModel.getPetLocation(loggedInViewModel.liveFirebaseUser.value?.uid!!,
            args.petLocationid)
        println("this is test $test")

        setupMenu()

        fragBinding.btnGoToEvents.setOnClickListener {
            val action = PetLocationDetailFragmentDirections.actionPetLocationDetailFragmentToEventListFragment(
                args.petLocationid
            )
            findNavController().navigate(action)
        }

        fragBinding.chooseImage.setOnClickListener {
            showImagePicker(imageIntentLauncher)
        }

        return root
    }

    private fun render(petLocation: PetLocationModel) {
        fragBinding.petLocationvm = detailViewModel
        fragBinding.petLocationName.setText(petLocation.title)
        events = petLocation.events
        petLocationCategory = petLocation?.category.toString()
        if (!imageLoad) {
            image = petLocation?.image.toString()
        }
        val spinner = fragBinding.petLocationCategorySpinner
        val adapter = activity?.applicationContext?.let { ArrayAdapter(it, android.R.layout.simple_spinner_item, petLocationCategorys) } as SpinnerAdapter
        spinner.adapter = adapter
        val spinnerPosition = petLocationCategorys.indexOf(petLocationCategory)
        spinner.setSelection(spinnerPosition)
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
        println("petLocation.image in render ${petLocation?.image}")
        println("image in render $image")
        if (image.isNotEmpty()) {
            fragBinding.chooseImage.setText(ie.setu.pupplan.R.string.button_changeImage)
            Picasso.get()
                .load(image)
                .resize(450, 420)
                .centerCrop()
                .into(fragBinding.petLocationImage) }
    }

    private fun getCurrentPetLocation(petLocation: PetLocationModel) {
        currentPetLocation = petLocation
        println("this is currentPetLocation3 $currentPetLocation")
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
                (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(ie.setu.pupplan.R.menu.menu_petlocation_detail, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                when (menuItem.itemId) {
                    ie.setu.pupplan.R.id.item_cancel -> {
                        findNavController().navigate(ie.setu.pupplan.R.id.action_petLocationDetailFragment_to_petLocationListFragment)
                    }
                    ie.setu.pupplan.R.id.item_petLocation_save -> {
                        if (fragBinding.petLocationTitle.text.isEmpty()) {
                            Toast.makeText(context, ie.setu.pupplan.R.string.enter_petLocation_title, Toast.LENGTH_LONG).show()
                        } else {
                            if(imageLoad) {
                                image = FirebaseImageManager.imageUriPetLocation.value.toString()
                                println("tell me imageUriValue ${FirebaseImageManager.imageUriPetLocation.value.toString()}")
                            }
                            detailViewModel.updatePetLocation(loggedInViewModel.liveFirebaseUser.value?.uid!!,
                                args.petLocationid, PetLocationModel(uid = args.petLocationid, title = fragBinding.petLocationTitle.text.toString(), description = fragBinding.description.text.toString(), category = petLocationCategory,
                                    email = loggedInViewModel.liveFirebaseUser.value?.email!!, events = events, profilePic = FirebaseImageManager.imageUri.value.toString(), image = image))
                            println("tell me image $image")
                        }
                        findNavController().navigate(ie.setu.pupplan.R.id.action_petLocationDetailFragment_to_petLocationListFragment)
                    }

                    ie.setu.pupplan.R.id.item_petLocation_delete -> {
                        events?.forEach {
                            detailViewModel.removeFavourite(loggedInViewModel.liveFirebaseUser.value?.uid!!, it.eventId)
                        }
                        detailViewModel.deletePetLocation(loggedInViewModel.liveFirebaseUser.value?.uid!!, args.petLocationid)
                        findNavController().navigate(ie.setu.pupplan.R.id.action_petLocationDetailFragment_to_petLocationListFragment)

                    }

                    ie.setu.pupplan.R.id.item_goToEvents -> {
                        val action = PetLocationDetailFragmentDirections.actionPetLocationDetailFragmentToEventListFragment(
                            args.petLocationid
                        )
                        findNavController().navigate(action)
                    }

                }
                return NavigationUI.onNavDestinationSelected(menuItem,
                    requireView().findNavController())
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
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
                            println("image in imageLauncher $image")
                            fragBinding.chooseImage.setText(ie.setu.pupplan.R.string.button_changeImage)
                            FirebaseImageManager
                                .updatePetLocationImage(loggedInViewModel.liveFirebaseUser.value!!.uid,
                                    readImageUri(result.resultCode, result.data),
                                    fragBinding.petLocationImage,
                                    true)
                            imageLoad = true
                            testing = true
                        }
                    }
                    AppCompatActivity.RESULT_CANCELED -> { } else -> { }
                }
            }
    }

    override fun onResume() {
        super.onResume()
        detailViewModel.getPetLocation(loggedInViewModel.liveFirebaseUser.value?.uid!!,
            args.petLocationid)
        println("onResume is used")

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }
}