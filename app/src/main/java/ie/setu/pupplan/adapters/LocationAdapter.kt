package ie.setu.pupplan.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ie.setu.pupplan.databinding.CardLocationBinding
import ie.setu.pupplan.models.LocationModel

//interface will represent click events on a location Card,
// and allow us to abstract the response to this event
interface LocationListener {
    fun onLocationClick(location: LocationModel, position: Int)
}

// below class is designed to work with a RecyclerView to display a
// list of LocationModel objects in a user interface.
// It handles creating view holders, binding data to them, and determining the number of items in the list.
// To use this adapter, you would typically set it as
// the adapter for a RecyclerView and provide a list of LocationModel objects to display.
class LocationAdapter constructor(private var locations: List<LocationModel>,
                                  private val listener: LocationListener) :
    RecyclerView.Adapter<LocationAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardLocationBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val location = locations[holder.adapterPosition]
        holder.bind(location, listener)
    }

    override fun getItemCount(): Int = locations.size

    fun updateList(filteredLocations: List<LocationModel>) {
        locations = filteredLocations
        notifyDataSetChanged()
    }

    inner class MainHolder(private val binding : CardLocationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(location: LocationModel, listener: LocationListener) {
            binding.locationTitle.text = location.title
//            binding.locationDescription.text = location.description
            binding.locationCategory.text = location.locationCategory

            Picasso.get().load(location.image).resize(200,200).into(binding.imageIcon)
            binding.root.setOnClickListener { listener.onLocationClick(location, adapterPosition) }
        }
    }


}