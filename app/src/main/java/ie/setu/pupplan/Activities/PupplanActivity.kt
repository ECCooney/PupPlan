package ie.setu.pupplan.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import ie.setu.pupplan.Models.PupplanModel
import ie.setu.pupplan.databinding.ActivityPupplanBinding
import timber.log.Timber
import timber.log.Timber.i

class PupplanActivity : AppCompatActivity() {

//    View Binding initiated to link module to layout file activity_pupplan.xml
//    ActivityPupplanBinding is autogenerated class that just needs to be imported, as binding is switched on in build.gradle
    private lateinit var binding: ActivityPupplanBinding

//    bring in data model
    var location = PupplanModel()

//    main function
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//     inflater takes an XML file as input and builds the View objects from it
        binding = ActivityPupplanBinding.inflate(layoutInflater)
        setContentView(binding.root)

//      logging
        Timber.plant(Timber.DebugTree() )

        i("Placemark Activity started..")

        binding.btnAdd.setOnClickListener()
        val locationTitle = binding.locationTitle.text.toString()
        if (locationTitle.isNotEmpty()) {
            i("add Button Pressed: $locationTitle")
        }
        else {
            Snackbar
                .make(it,"Please Enter a title", Snackbar.LENGTH_LONG)
                .show()
        }
    }
}