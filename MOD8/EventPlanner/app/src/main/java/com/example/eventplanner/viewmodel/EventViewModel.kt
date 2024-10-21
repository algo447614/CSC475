package com.example.eventplanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventplanner.model.Event
import com.example.eventplanner.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EventViewModel(private val repository: EventRepository = EventRepository()) : ViewModel() {
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    init {
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            repository.getEvents().collect { eventList ->
                _events.value = eventList
            }
        }
    }
}