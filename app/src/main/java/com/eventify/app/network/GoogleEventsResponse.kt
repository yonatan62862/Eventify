package com.eventify.app.network

data class GoogleEventsResponse(
    val items: List<GoogleEvent>
)


data class GoogleEvent(
    val summary: String,
    val start: EventDateTime,
    val end: EventDateTime?,
    val htmlLink: String?,
    val location: String?
)

data class EventDateTime(
    val dateTime: String?,
    val date: String?
)