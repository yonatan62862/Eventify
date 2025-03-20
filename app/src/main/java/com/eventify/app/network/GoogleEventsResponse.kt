package com.eventify.app.network

data class GoogleEventsResponse(
    val items: List<GoogleEvent>
)


data class GoogleEvent(
    val id: String,
    val summary: String?,
    val location: String?,
    val start: EventDateTime,
    val end: EventDateTime,
    val htmlLink: String?,
    val attachments: List<GoogleEventAttachment>?
)

data class GoogleEventAttachment(
    val fileId: String?,
    val fileUrl: String?,
    val title: String?
)

data class EventDateTime(
    val dateTime: String?,
    val date: String?
)