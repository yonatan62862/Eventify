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
            Log.d("EventRepository", "Inserted event to ROOM: ${eventWithEmail.name}, ${eventWithEmail.startDate}")

            eventsCollection.document(eventWithEmail.id).set(eventWithEmail).addOnSuccessListener {
                Log.d("EventRepository", "Event saved to Firestore: ${eventWithEmail.name}")
            }.addOnFailureListener { e ->
                Log.e("EventRepository", "Failed to save event to Firestore", e)
            }
        }
    }




    suspend fun getUserEvents(userEmail: String): List<EventEntity> {
        return withContext(Dispatchers.IO) {
            val snapshot = eventsCollection
                .whereArrayContains("invitedUsers", userEmail)
                .get().await()

            val remoteEvents = snapshot.toObjects(Event::class.java)

            val userOwnedEvents = eventsCollection.whereEqualTo("ownerId", userEmail).get().await()
            val ownedEvents = userOwnedEvents.toObjects(Event::class.java)

            val allEvents = (remoteEvents + ownedEvents).map { it.toLocal() }

            Log.d("EventRepository", "Fetched ${allEvents.size} events from Firestore")
            allEvents.forEach { Log.d("EventRepository", "Event: ${it.name}, ${it.startDate}") }

            allEvents.forEach { eventDao.insertEvent(it) }

            return@withContext allEvents
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
