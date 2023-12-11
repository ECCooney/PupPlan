package ie.setu.pupplan.firebase

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import ie.setu.pupplan.utils.customTransformation
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.util.Random

object FirebaseImageManager {

    var storage = FirebaseStorage.getInstance().reference
    var imageUri = MutableLiveData<Uri>()
    var imageUriPetLocation = MutableLiveData<Uri>()
    var imageUriEvent = MutableLiveData<Uri>()
    var imageUriEvent2 = MutableLiveData<Uri>()
    var imageUriEvent3 = MutableLiveData<Uri>()

    // Function for generating random ID numbers
    internal fun generateRandomId(): Long {
        return Random().nextLong()
    }

    fun checkStorageForExistingProfilePic(userid: String) {
        val imageRef = storage.child("photos").child("${userid}.jpg")
        val defaultImageRef = storage.child("homer.jpg")

        imageRef.metadata.addOnSuccessListener { //File Exists
            imageRef.downloadUrl.addOnCompleteListener { task ->
                imageUri.value = task.result!!
            }
            //File Doesn't Exist
        }.addOnFailureListener {
            imageUri.value = Uri.EMPTY
        }
    }

    fun uploadImageToFirebase(userid: String, bitmap: Bitmap, updating : Boolean, path: String) {
        // Get the data from an ImageView as bytes
        val imageRef = storage.child("photos").child("${userid}.jpg")
        //val bitmap = (imageView as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        lateinit var uploadTask: UploadTask

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        imageRef.metadata.addOnSuccessListener { //File Exists
            if(updating) // Update existing Image
            {
                uploadTask = imageRef.putBytes(data)
                uploadTask.addOnSuccessListener { ut ->
                    ut.metadata!!.reference!!.downloadUrl.addOnCompleteListener { task ->
                        imageUri.value = task.result!!
                        FirebaseDBManager.updateImageRef(userid,imageUri.value.toString(),path)
                    }
                }
            }
        }.addOnFailureListener { //File Doesn't Exist
            uploadTask = imageRef.putBytes(data)
            uploadTask.addOnSuccessListener { ut ->
                ut.metadata!!.reference!!.downloadUrl.addOnCompleteListener { task ->
                    imageUri.value = task.result!!
                    // FirebaseDBManager.updateImageRef(userid,imageUri.value.toString())
                }
            }
        }
    }

    fun updateUserImage(userid: String, imageUri : Uri?, imageView: ImageView, updating : Boolean) {
        Picasso.get().load(imageUri)
            .resize(200, 200)
            .transform(customTransformation())
            .memoryPolicy(MemoryPolicy.NO_CACHE)
            .centerCrop()
            .into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?,
                                            from: Picasso.LoadedFrom?
                ) {
                    Timber.i("DX onBitmapLoaded $bitmap")
                    uploadImageToFirebase(userid, bitmap!!,updating,"profilePic")
                    imageView.setImageBitmap(bitmap)
                }

                override fun onBitmapFailed(e: java.lang.Exception?,
                                            errorDrawable: Drawable?) {
                    Timber.i("DX onBitmapFailed $e")
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
            })
    }

    fun updateDefaultImage(userid: String, resource: Int, imageView: ImageView) {
        Picasso.get().load(resource)
            .resize(200, 200)
            .transform(customTransformation())
            .memoryPolicy(MemoryPolicy.NO_CACHE)
            .centerCrop()
            .into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?,
                                            from: Picasso.LoadedFrom?
                ) {
                    Timber.i("DX onBitmapLoaded $bitmap")
                    uploadImageToFirebase(userid, bitmap!!,false, "profilePic")
                    imageView.setImageBitmap(bitmap)
                }

                override fun onBitmapFailed(e: java.lang.Exception?,
                                            errorDrawable: Drawable?) {
                    Timber.i("DX onBitmapFailed $e")
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
            })
    }

    fun uploadPetLocationImageToFirebase(userid: String, fileName: String, bitmap: Bitmap, updating : Boolean, path: String) {
        // Get the data from an ImageView as bytes
        val imageName = generateRandomId().toString()
        val imageRef = storage.child("photos").child("${fileName}.jpg")

        imageRef.metadata.addOnSuccessListener { //File Exists
            imageRef.downloadUrl.addOnCompleteListener { task ->
                imageUriPetLocation.value = task.result!!
                var imageUriPetLocationValue = imageUriPetLocation.value.toString()
                println("this is existing imageUriValue $imageUriPetLocationValue")
            }
            //File Doesn't Exist
        }.addOnFailureListener {
            //val bitmap = (imageView as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            lateinit var uploadTask: UploadTask

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            uploadTask = imageRef.putBytes(data)
            uploadTask.addOnSuccessListener { ut ->
                ut.metadata!!.reference!!.downloadUrl.addOnCompleteListener { task ->
                    imageUriPetLocation.value = task.result!!
                    var imageUriPetLocationValue = imageUriPetLocation.value.toString()
                    println("this is new imageUriValue $imageUriPetLocationValue")
                }
            }
        }



    }

    fun updatePetLocationImage(userid: String, imageUri : Uri?, imageView: ImageView, updating : Boolean) {
        println("this is imageUri $imageUri")
        var fileName = imageUri?.lastPathSegment
        println("this is fileName $fileName")

        Picasso.get().load(imageUri)
            .resize(450, 420)
            .memoryPolicy(MemoryPolicy.NO_CACHE)
            .centerCrop()
            .into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?,
                                            from: Picasso.LoadedFrom?
                ) {
                    Timber.i("DX onBitmapLoaded $bitmap")
                    uploadPetLocationImageToFirebase(userid, fileName!!, bitmap!!,updating, "image")
                    imageView.setImageBitmap(bitmap)
                }

                override fun onBitmapFailed(e: java.lang.Exception?,
                                            errorDrawable: Drawable?) {
                    Timber.i("DX onBitmapFailed $e")
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
            })
    }

    fun uploadEventImageToFirebase(userid: String, fileName: String, bitmap: Bitmap, updating : Boolean, imageName: String) {
        // Get the data from an ImageView as bytes
        //val imageName = generateRandomId().toString()
        val imageRef = storage.child("photos").child("${fileName}.jpg")
        println("this is imageRef $imageRef")

        imageRef.metadata.addOnSuccessListener { //File Exists


            if (imageName == "eventImage") {
                imageRef.downloadUrl.addOnCompleteListener { task ->
                    imageUriEvent.value = task.result!!
                    var imageUriEventValue = imageUriEvent.value.toString()
                    println("this is existing imageUriValue $imageUriEventValue")
                }
            }
            if (imageName == "eventImage2") {
                imageRef.downloadUrl.addOnCompleteListener { task ->
                    imageUriEvent2.value = task.result!!
                    var imageUriEventValue2 = imageUriEvent2.value.toString()
                    println("this is existing imageUriValue2 $imageUriEventValue2")
                }
            }
            if (imageName == "eventImage3") {
                imageRef.downloadUrl.addOnCompleteListener { task ->
                    imageUriEvent3.value = task.result!!
                    var imageUriEventValue3 = imageUriEvent3.value.toString()
                    println("this is existing imageUriValue3 $imageUriEventValue3")
                }
            }

            //File Doesn't Exist
        }.addOnFailureListener {
            //val bitmap = (imageView as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            lateinit var uploadTask: UploadTask

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            uploadTask = imageRef.putBytes(data)
            if (imageName == "eventImage") {
                uploadTask.addOnSuccessListener { ut ->
                    ut.metadata!!.reference!!.downloadUrl.addOnCompleteListener { task ->
                        imageUriEvent.value = task.result!!
                    }
                }
            }
            if (imageName == "eventImage2") {
                uploadTask.addOnSuccessListener { ut ->
                    ut.metadata!!.reference!!.downloadUrl.addOnCompleteListener { task ->
                        imageUriEvent2.value = task.result!!
                    }
                }
            }
            if (imageName == "eventImage3") {
                uploadTask.addOnSuccessListener { ut ->
                    ut.metadata!!.reference!!.downloadUrl.addOnCompleteListener { task ->
                        imageUriEvent3.value = task.result!!
                    }
                }
            }
        }




    }

    fun updateEventImage(userid: String, imageUri : Uri?, imageView: ImageView, updating : Boolean, imageName: String) {
        println("this is initial passed imageUriEvent $imageUri")
        var fileName = imageUri?.lastPathSegment
        println("this is fileName $fileName")

        Picasso.get().load(imageUri)
            .resize(450, 420)
            .memoryPolicy(MemoryPolicy.NO_CACHE)
            .centerCrop()
            .into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?,
                                            from: Picasso.LoadedFrom?
                ) {
                    Timber.i("DX onBitmapLoaded $bitmap")
                    uploadEventImageToFirebase(userid, fileName!!, bitmap!!,updating, imageName)
                    imageView.setImageBitmap(bitmap)
                }

                override fun onBitmapFailed(e: java.lang.Exception?,
                                            errorDrawable: Drawable?) {
                    Timber.i("DX onBitmapFailed $e")
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
            })
    }
}
