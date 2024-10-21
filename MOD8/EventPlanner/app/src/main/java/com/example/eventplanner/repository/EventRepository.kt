package com.example.eventplanner.repository

import com.example.eventplanner.model.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class EventRepository {
    // Simulate a database or network call
    fun getEvents(): Flow<List<Event>> = flowOf(listOf(
        Event("1", "Team Meeting", "2024-03-15", "Discuss Q1 goals"),
        Event("2", "Birthday Party", "2024-03-20", "John's surprise party")
    ))
}