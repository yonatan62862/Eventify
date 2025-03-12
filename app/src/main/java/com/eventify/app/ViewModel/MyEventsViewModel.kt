package com.eventify.app.ViewModel

import android.app.Application
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

    val localEvents: LiveData<List<EventEntity>> = repository.getAllLocalEvents(userId)

    private val _remoteEvents = MutableLiveData<List<EventEntity>>()
    val remoteEvents: LiveData<List<EventEntity>> get() = _remoteEvents

    init {
        fetchUserEvents()
    }

    fun fetchUserEvents() {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: return
        viewModelScope.launch {
            val events = repository.getUserEvents()
            _remoteEvents.postValue(events)
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            repository.deleteEvent(eventId)
        }
    }



}
