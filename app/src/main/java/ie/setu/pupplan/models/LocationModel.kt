package ie.setu.pupplan.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocationModel(var id: Long = 0,
                         var title: String = "",
                         var description: String = "",
//                        var locationCategory: String = "",
//                        var lat: Double = 00.00,
//                        var lng: Double = 00.00,
//                        var openingTime: Double = 00.00,
//                        var closingTime: Double = 00.00,
//                        var userId: String? = "",
//                        var images:
): Parcelable
