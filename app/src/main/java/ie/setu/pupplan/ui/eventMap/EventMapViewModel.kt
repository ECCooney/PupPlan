import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import ie.setu.pupplan.models.Location

class EventMapViewModel : ViewModel() {
    var location = MutableLiveData<Location>()
    lateinit var map : GoogleMap

}