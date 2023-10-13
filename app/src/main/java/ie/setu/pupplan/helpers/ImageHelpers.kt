package ie.setu.pupplan.helpers

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import ie.setu.pupplan.R

fun showImagePicker(intentLauncher : ActivityResultLauncher<Intent>) {
    var chooseFile = Intent(Intent.ACTION_OPEN_DOCUMENT)
    chooseFile.type = "image/*"
    chooseFile = Intent.createChooser(chooseFile, R.string.select_location_image.toString())
    intentLauncher.launch(chooseFile)
}