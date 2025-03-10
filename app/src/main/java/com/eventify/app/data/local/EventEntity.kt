package com.eventify.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val name: String,
    val date: String,
    val location: String,
    val description: String,
    val ownerId: String
)
