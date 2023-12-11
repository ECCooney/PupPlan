package ie.setu.pupplan.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ie.setu.pupplan.databinding.CardEventBinding
import ie.setu.pupplan.models.NewEvent

interface EventListener {
    fun onEventClick(event: NewEvent)
}

class EventAdapter constructor(private var events: ArrayList<NewEvent>,
                                 private val listener: EventListener) :
    RecyclerView.Adapter<EventAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardEventBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val event = events[holder.adapterPosition]
        holder.bind(event, listener)
    }

    fun removeAt(position: Int) {
        events.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getItemCount(): Int = events.size

    inner class MainHolder(val binding : CardEventBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(event: NewEvent, listener: EventListener) {
            // Function to bind different values to the event adapter card
            binding.root.tag = event
            binding.event = event
            binding.eventTitle.text = event.eventTitle
            binding.eventCost.text = event.eventCost.toString()
            binding.eventDescription.text = event.eventDescription
            if (event.eventImage.isNotEmpty()) {
                Picasso.get().load(event.eventImage).resize(200,200).into(binding.eventImageIcon)
            }
            binding.root.setOnClickListener { listener.onEventClick(event) }

        }
    }
}