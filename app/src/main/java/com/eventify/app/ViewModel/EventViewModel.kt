package com.eventify.app.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.eventify.app.data.local.AppDatabase
import com.eventify.app.data.local.EventEntity
import com.eventify.app.data.local.remote.EventRepository
import kotlinx.coroutines.launch

class EventViewModel(application: Application) : AndroidViewModel(application) {
    private val eventDao = AppDatabase.getDatabase(application).eventDao()
    private val repository = EventRepository(eventDao)

    private val _eventsLiveData = MutableLiveData<List<EventEntity>>()
    val eventsLiveData: LiveData<List<EventEntity>> = _eventsLiveData

    fun insertEvent(event: EventEntity) {
        viewModelScope.launch {
            repository.insertEvent(event)
            loadUserEvents(event.ownerId)
        }
    }

    fun loadUserEvents(userId: String) {
        viewModelScope.launch {
            val events = repository.getUserEvents(userId)
            _eventsLiveData.postValue(events)
        }
    }
}