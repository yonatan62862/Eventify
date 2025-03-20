package com.eventify.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.eventify.app.R
import com.eventify.app.data.local.EventEntity
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale

class EventAdapter(
    private val onDeleteClick: (String) -> Unit,
    private val onEditClick: (EventEntity) -> Unit
) : ListAdapter<EventEntity, EventAdapter.EventViewHolder>(DiffCallback()) {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(event: EventEntity, onDeleteClick: (String) -> Unit, onEditClick: (EventEntity) -> Unit) {
            val titleTextView = itemView.findViewById<TextView>(R.id.eventTitle)
            val dateTextView = itemView.findViewById<TextView>(R.id.eventDate)
            val timeTextView = itemView.findViewById<TextView>(R.id.eventTime)
            val locationTextView = itemView.findViewById<TextView>(R.id.eventLocation)
            val eventImageView = itemView.findViewById<ImageView>(R.id.eventImage)
            val deleteButton = itemView.findViewById<Button>(R.id.btnDeleteEvent)
            val editButton = itemView.findViewById<Button>(R.id.btnEditEvent)

            titleTextView.text = event.name

            dateTextView.text = "📅 Date: ${formatDate(event.startDate)}"
            timeTextView.text = "⏰ Time: ${formatTime(event.startTime)}"
            locationTextView.text = "📍 Location: ${event.location}"

            if (!event.imageUrl.isNullOrEmpty()) {
                eventImageView.visibility = View.VISIBLE
                Picasso.get()
                    .load(event.imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(eventImageView)
            } else {
                eventImageView.visibility = View.GONE
            }

            deleteButton.setOnClickListener {
                onDeleteClick(event.id)
            }

            editButton.setOnClickListener {
                onEditClick(event)
            }
        }

        private fun formatDate(date: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val parsedDate = inputFormat.parse(date)
                outputFormat.format(parsedDate ?: date)
            } catch (e: Exception) {
                date
            }
        }

        private fun formatTime(time: String): String {
            return try {
                val inputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val parsedTime = inputFormat.parse(time)
                outputFormat.format(parsedTime ?: time)
            } catch (e: Exception) {
                time
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position), onDeleteClick, onEditClick)
    }

    class DiffCallback : DiffUtil.ItemCallback<EventEntity>() {
        override fun areItemsTheSame(oldItem: EventEntity, newItem: EventEntity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: EventEntity, newItem: EventEntity): Boolean =
            oldItem == newItem
    }
}
