package com.example.todo

import java.util.Date

data class ToDoItem(
    var id: Int = 0,
    var task: String,
    var isCompleted: Boolean = false,
    var completedAt: Date? = null
)