package com.eventify.app.data.remote

import com.eventify.app.data.local.EventEntity

data class Event(
    val id: String = "",
    val name: String = "",
    val eventType: String = "",
    val description: String = "",
    val startDate: String = "",
    val startTime: String = "",
    val endDate: String = "",
    val endTime: String = "",
    val location: String = "",
    val ownerId: String = "",
    val invitedUsers: List<String> = listOf(),
    val imageUrl: String? = null

) {
    fun toLocal(): EventEntity {
        return EventEntity(id, name, eventType, description, startDate, startTime, endDate, endTime, location, ownerId, invitedUsers, imageUrl)
    }
}
