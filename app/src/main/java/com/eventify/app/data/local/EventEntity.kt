package com.eventify.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.eventify.app.data.remote.Event

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val name: String,
    val eventType: String,
    val description: String,
    val startDate: String,
    val startTime: String,
    val endDate: String,
    val endTime: String,
    val location: String,
    val ownerId: String
)
{
    fun toRemote(): Event {
        return Event(id, name, eventType, description, startDate, startTime, endDate, endTime, location, ownerId)
    }

}
