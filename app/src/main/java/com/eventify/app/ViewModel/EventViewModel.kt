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

class EventViewModel(application: Application) : AndroidViewModel(application) {
    private val eventDao = AppDatabase.getDatabase(application).eventDao()
    private val repository = EventRepository(eventDao)

    private val _eventsLiveData = MutableLiveData<List<EventEntity>>()
    val eventsLiveData: LiveData<List<EventEntity>> = _eventsLiveData

    fun insertEvent(event: EventEntity) {
        viewModelScope.launch {
            repository.insertEvent(event)
            loadUserEvents()
        }
    }

    fun loadUserEvents() {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: return
        viewModelScope.launch {
            val events = repository.getUserEvents(userEmail)
            _eventsLiveData.postValue(events)
        }
    }


    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            repository.deleteEvent(eventId)
            loadUserEvents()
        }
    }


}
