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
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "Unknown"
            val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: "Unknown"

            val eventWithDetails = event.copy(ownerId = userId, invitedUsers = event.invitedUsers)

            CoroutineScope(Dispatchers.IO).launch {
                eventDao.insertEvent(eventWithDetails)

                Log.d("EventRepository", "Saving event with owner: $userEmail and invited users: ${eventWithDetails.invitedUsers}")

                eventsCollection.document(eventWithDetails.id).set(eventWithDetails)
                    .addOnSuccessListener {
                        Log.d("EventRepository", "Event saved successfully with invited users: ${eventWithDetails.invitedUsers}")
                    }
                    .addOnFailureListener { e ->
                        Log.e("EventRepository", "Failed to save event to Firestore", e)
                    }
            }
        }




        suspend fun getUserEvents(): List<EventEntity> {
            return withContext(Dispatchers.IO) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@withContext emptyList()

                val ownedEventsSnapshot = eventsCollection.whereEqualTo("ownerId", userId).get().await()
                val ownedEvents = ownedEventsSnapshot.toObjects(Event::class.java)

                val invitedEventsSnapshot = eventsCollection.whereArrayContains("invitedUsers", userId).get().await()
                val invitedEvents = invitedEventsSnapshot.toObjects(Event::class.java)

                val allEvents = (ownedEvents + invitedEvents).map { it.toLocal() }

                Log.d("EventRepository", "Fetched ${allEvents.size} events from Firestore")

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
