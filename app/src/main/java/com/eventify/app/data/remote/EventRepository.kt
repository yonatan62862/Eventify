package com.eventify.app.data.remote

import android.util.Log
import androidx.lifecycle.LiveData
import com.eventify.app.data.local.EventDao
import com.eventify.app.data.local.EventEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class EventRepository(private val eventDao: EventDao) {
    private val db = FirebaseFirestore.getInstance()
    private val eventsCollection = db.collection("events")

    fun insertEvent(event: EventEntity) {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: "Unknown"
        val eventWithEmail = event.copy(ownerId = userEmail)

        CoroutineScope(Dispatchers.IO).launch {
            eventDao.insertEvent(eventWithEmail)
            eventsCollection.document(eventWithEmail.id).set(eventWithEmail)
        }
    }


    suspend fun getUserEvents(): List<EventEntity> {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: return emptyList()

        return withContext(Dispatchers.IO) {
            val localEvents = eventDao.getUserEvents(userEmail)

            try {
                val snapshot = eventsCollection.whereEqualTo("ownerId", userEmail).get().await()
                val remoteEvents = snapshot.toObjects(Event::class.java)

                eventDao.clearAllEvents()
                remoteEvents.forEach { eventDao.insertEvent(it.toLocal()) }

                return@withContext remoteEvents.map { it.toLocal() }
            } catch (e: Exception) {
                return@withContext localEvents
            }
        }
    }


    fun getAllLocalEvents(userId: String): LiveData<List<EventEntity>> {
        return eventDao.getUserEventsLiveData(userId)
    }


    suspend fun deleteEvent(eventId: String) {
        withContext(Dispatchers.IO) {
            try {
                eventDao.deleteEvent(eventId)

                eventsCollection.document(eventId).delete().await()

                Log.d("EventRepository", "Event deleted successfully from Firestore")
            } catch (e: Exception) {
                Log.e("EventRepository", "Error deleting event from Firestore", e)
            }
        }
    }


}
