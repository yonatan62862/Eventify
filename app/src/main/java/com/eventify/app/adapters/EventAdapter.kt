package com.eventify.app.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.eventify.app.R
import com.eventify.app.data.local.EventEntity

class EventAdapter(private val onDeleteClick: (String) -> Unit) : ListAdapter<EventEntity, EventAdapter.EventViewHolder>(DiffCallback()) {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(event: EventEntity, onDeleteClick: (String) -> Unit) {
            itemView.findViewById<TextView>(R.id.eventTitle).text = event.name
            itemView.findViewById<TextView>(R.id.eventDate).text = event.startDate

            itemView.findViewById<Button>(R.id.btnDeleteEvent).setOnClickListener {
                Log.d("EventAdapter", "Delete button clicked for eventId: ${event.id}")
                onDeleteClick(event.id)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position), onDeleteClick)
    }

    class DiffCallback : DiffUtil.ItemCallback<EventEntity>() {
        override fun areItemsTheSame(oldItem: EventEntity, newItem: EventEntity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: EventEntity, newItem: EventEntity): Boolean =
            oldItem == newItem
    }
}
