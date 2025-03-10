package com.eventify.app.data.local.remote

import android.util.Log
import com.eventify.app.data.local.EventDao
import com.eventify.app.data.local.EventEntity
import com.eventify.app.data.local.models.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class EventRepository(private val eventDao: EventDao) {

    suspend fun insertEvent(event: EventEntity) {
        withContext(Dispatchers.IO) {
            eventDao.insertEvent(event)
        }
    }

    suspend fun getUserEvents(userId: String): List<EventEntity> {
        return withContext(Dispatchers.IO) {
            eventDao.getUserEvents(userId)
        }
    }

    suspend fun deleteEvent(eventId: String) {
        withContext(Dispatchers.IO) {
            eventDao.deleteEvent(eventId)
        }
    }

    suspend fun clearAllEvents() {
        withContext(Dispatchers.IO) {
            eventDao.clearAllEvents()
        }
    }
}