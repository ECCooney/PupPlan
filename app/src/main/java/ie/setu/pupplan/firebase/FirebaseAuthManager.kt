package ie.setu.pupplan.firebase

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import ie.setu.pupplan.R
import timber.log.Timber


class FirebaseAuthManager(application: Application) {

    private var application: Application? = null

    var firebaseAuth: FirebaseAuth? = null
    var liveFirebaseUser = MutableLiveData<FirebaseUser>()
    var loggedOut = MutableLiveData<Boolean>()
    var errorStatus = MutableLiveData<Boolean>()
    var googleSignInClient = MutableLiveData<GoogleSignInClient>()

    init {
        //initialising Firebase authorisation for application
        this.application = application
        firebaseAuth = FirebaseAuth.getInstance()

        //if user is authorised, assign them as current user and check if there is an existing profile picture for them
        if (firebaseAuth!!.currentUser != null) {
            liveFirebaseUser.postValue(firebaseAuth!!.currentUser)
            loggedOut.postValue(false)
            errorStatus.postValue(false)
            FirebaseImageManager.checkStorageForExistingProfilePic(
                firebaseAuth!!.currentUser!!.uid)
        }
        configureGoogleSignIn()
    }

    //function for configuring Google sign-in
    private fun configureGoogleSignIn() {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(application!!.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient.value = GoogleSignIn.getClient(application!!.applicationContext,gso)
    }

    //function for using Google authorisation to provide credentials (token) for signing in.
    fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Timber.i( "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(application!!.mainExecutor) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update with the signed-in user's information
                    Timber.i( "signInWithCredential:success")
                    liveFirebaseUser.postValue(firebaseAuth!!.currentUser)

                } else {
                    // If sign in fails, display a message to the user.
                    Timber.i( "signInWithCredential:failure $task.exception")
                    errorStatus.postValue(true)
                }
            }
    }

    //if Google authorisation not used, email password combo can be used for login
    fun login(email: String?, password: String?) {
        firebaseAuth!!.signInWithEmailAndPassword(email!!, password!!)
            .addOnCompleteListener(application!!.mainExecutor, { task ->
                if (task.isSuccessful) {
                    liveFirebaseUser.postValue(firebaseAuth!!.currentUser)
                    errorStatus.postValue(false)
                } else {
                    Timber.i( "Login Failure: $task.exception!!.message")
                    errorStatus.postValue(true)
                }
            })
    }

    //registration of new email password combo for signing in
    fun register(email: String?, password: String?) {
        firebaseAuth!!.createUserWithEmailAndPassword(email!!, password!!)
            .addOnCompleteListener(application!!.mainExecutor, { task ->
                if (task.isSuccessful) {
                    liveFirebaseUser.postValue(firebaseAuth!!.currentUser)
                    errorStatus.postValue(false)
                } else {
                    Timber.i( "Registration Failure: $task.exception!!.message")
                    errorStatus.postValue(true)
                }
            })
    }

    //signing out user
    fun logOut() {
        firebaseAuth!!.signOut()
        loggedOut.postValue(true)
        errorStatus.postValue(false)
        googleSignInClient.value!!.signOut()
    }
}