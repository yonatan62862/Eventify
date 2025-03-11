package com.eventify.app.data.remote

import com.eventify.app.data.local.EventDao
import com.eventify.app.data.local.EventEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class EventRepository(private val eventDao: EventDao) {
    private val db = FirebaseFirestore.getInstance()
    private val eventsCollection = db.collection("events")

    suspend fun insertEvent(event: EventEntity) {
        withContext(Dispatchers.IO) {
            eventDao.insertEvent(event)
            eventsCollection.document(event.id).set(event.toRemote())
        }
    }

    suspend fun getUserEvents(userId: String): List<EventEntity> {
        return withContext(Dispatchers.IO) {
            val localEvents = eventDao.getUserEvents(userId)

            try {
                val snapshot = eventsCollection.whereEqualTo("ownerId", userId).get().await()
                val remoteEvents = snapshot.toObjects(Event::class.java)

                eventDao.clearAllEvents()
                remoteEvents.forEach { eventDao.insertEvent(it.toLocal()) }

                return@withContext remoteEvents.map { it.toLocal() }
            } catch (e: Exception) {
                return@withContext localEvents
            }
        }
    }

    suspend fun deleteEvent(eventId: String) {
        withContext(Dispatchers.IO) {
            eventDao.deleteEvent(eventId)
            eventsCollection.document(eventId).delete()
        }
    }
}
