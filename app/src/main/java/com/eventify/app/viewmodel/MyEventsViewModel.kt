package com.eventify.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.eventify.app.data.local.AppDatabase
import com.eventify.app.data.local.EventEntity
import com.eventify.app.data.remote.EventRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MyEventsViewModel(application: Application) : AndroidViewModel(application) {
    private val eventDao = AppDatabase.getDatabase(application).eventDao()
    private val repository = EventRepository(eventDao)

    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Local events from database
    val localEvents: LiveData<List<EventEntity>> = repository.getAllLocalEvents(userId)

    // Remote events
    private val _remoteEvents = MutableLiveData<List<EventEntity>>()
    val remoteEvents: LiveData<List<EventEntity>> get() = _remoteEvents

    // All events
    private val _allEvents = MutableLiveData<List<EventEntity>>()
    val allEvents: LiveData<List<EventEntity>> = _allEvents

    init {
        loadUserEvents()
    }

    fun loadUserEvents() {
        viewModelScope.launch {
            val localEventsList = repository.getAllLocalEvents(userId).value ?: emptyList()
            val remoteEventsList = repository.getUserEvents()

            val mergedEvents = (localEventsList + remoteEventsList).distinctBy { it.id }

            _allEvents.postValue(mergedEvents)
        }
    }


    fun insertEvent(event: EventEntity) {
        viewModelScope.launch {
            repository.insertEvent(event)
            loadUserEvents()
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            repository.deleteEvent(eventId)
            loadUserEvents()
        }
    }
}
