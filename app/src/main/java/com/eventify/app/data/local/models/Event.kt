package com.eventify.app.data.local.models

data class Event(
    val id: String = "",
    val name: String = "",
    val date: String = "",
    val location: String = "",
    val description: String = "",
    val ownerId: String = "",
    val guests: List<String> = listOf()
)
