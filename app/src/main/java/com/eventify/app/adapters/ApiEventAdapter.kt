package com.eventify.app.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.eventify.app.R
import com.eventify.app.network.GoogleEvent
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class ApiEventAdapter(private val context: Context, private val events: List<GoogleEvent>) :
    RecyclerView.Adapter<ApiEventAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.eventTitle)
        val date: TextView = view.findViewById(R.id.eventDate)
        val time: TextView = view.findViewById(R.id.eventTime)
        val location: TextView = view.findViewById(R.id.eventLocation)
        val eventImage: ImageView = view.findViewById(R.id.eventIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_api_event, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = events[position]

        holder.title.text = event.summary

        holder.date.text = when {
            event.start.dateTime != null -> "📅 " + formatDate(event.start.dateTime)
            event.start.date != null -> "📅 " + formatDate(event.start.date) + " (All Day)"
            else -> "📅 תאריך לא ידוע"
        }

        holder.time.text = when {
            event.start.dateTime != null && event.end?.dateTime != null -> {
                "⏰ ${formatTime(event.start.dateTime)} - ${formatTime(event.end.dateTime)}"
            }
            event.start.dateTime != null -> {
                "⏰ ${formatTime(event.start.dateTime)}"
            }
            event.start.date != null -> "⏰ All Day"
            else -> "⏰ שעה לא זמינה"
        }

        holder.location.text = event.location?.let { "📍 $it" } ?: "📍New York"

        val imageUrl = event.attachments?.firstOrNull()?.fileUrl ?: ""
        if (imageUrl.isNotEmpty()) {
            Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.error_image)
                .error(R.drawable.error_image)
                .into(holder.eventImage)
        } else {
            holder.eventImage.setImageResource(R.drawable.ic_calendar)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = android.net.Uri.parse(event.htmlLink)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = events.size

    private fun formatDate(date: String): String {
        return try {
            if (date.length == 10) {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val parsedDate = inputFormat.parse(date)
                outputFormat.format(parsedDate!!)
            } else {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")

                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                outputFormat.timeZone = TimeZone.getDefault()

                val parsedDate = inputFormat.parse(date)
                outputFormat.format(parsedDate!!)
            }
        } catch (e: Exception) {
            "תאריך לא ידוע"
        }
    }

    private fun formatTime(dateTime: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")

            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            outputFormat.timeZone = TimeZone.getDefault()

            val parsedDate = inputFormat.parse(dateTime)
            outputFormat.format(parsedDate!!)
        } catch (e: Exception) {
            "שעה לא זמינה"
        }
    }
}
