package ie.setu.pupplan.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ie.setu.pupplan.databinding.CardPetlocationBinding
import ie.setu.pupplan.models.PetLocationModel
import ie.setu.pupplan.utils.customTransformation

//interface will represent click events on a petLocation Card,
// and allow us to abstract the response to this event

// below class is designed to work with a RecyclerView to display a
// list of PetLocationModel objects in a user interface.
// It handles creating view holders, binding data to them, and determining the number of items in the list.
// To use this adapter, you would typically set it as
// the adapter for a RecyclerView and provide a list of PetLocationModel objects to display.
interface PetLocationClickListener {
    fun onPetLocationClick(petLocation: PetLocationModel)
}

class PetLocationAdapter constructor(private var petLocations: ArrayList<PetLocationModel>,
                                   private val listener: PetLocationClickListener,
                                   private val readOnly: Boolean)
    : RecyclerView.Adapter<PetLocationAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        // binding petLocation card
        val binding = CardPetlocationBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding, readOnly)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        //specific petLocation is based on adapter position
        val petLocation = petLocations[holder.adapterPosition]
        holder.bind(petLocation,listener)
    }

    fun removeAt(position: Int) {
        petLocations.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getItemCount(): Int = petLocations.size

    inner class MainHolder(val binding : CardPetlocationBinding, private val readOnly: Boolean) :
        RecyclerView.ViewHolder(binding.root) {
        //read only variable set for when all petLocations are loaded and you don't want user editing others
        val readOnlyRow = readOnly
        //binding different elements within petLocation card
        fun bind(petLocation: PetLocationModel, listener: PetLocationClickListener) {
            binding.root.tag = petLocation
            binding.petLocation = petLocation
            if (petLocation.image.isNotEmpty()) {
                Picasso.get().load(petLocation.image).resize(200,200).into(binding.imageIcon)
            }
            binding.root.setOnClickListener { listener.onPetLocationClick(petLocation) }
            binding.executePendingBindings()
        }
    }
}