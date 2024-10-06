package com.example.todo

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.Date

class ToDoDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "todo.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_NAME = "todos"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TASK = "task"
        private const val COLUMN_COMPLETED = "completed"
        private const val COLUMN_COMPLETED_AT = "completed_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = "CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_TASK TEXT, " +
                "$COLUMN_COMPLETED INTEGER, " +
                "$COLUMN_COMPLETED_AT INTEGER)"
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_COMPLETED_AT INTEGER")
        }
    }

    fun addTodo(todo: ToDoItem): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TASK, todo.task)
            put(COLUMN_COMPLETED, if (todo.isCompleted) 1 else 0)
            put(COLUMN_COMPLETED_AT, todo.completedAt?.time)
        }
        return db.insert(TABLE_NAME, null, values)
    }

    fun getAllTodos(): List<ToDoItem> {
        return getTodos("SELECT * FROM $TABLE_NAME WHERE $COLUMN_COMPLETED = 0")
    }

    fun getCompletedTodos(): List<ToDoItem> {
        return getTodos("SELECT * FROM $TABLE_NAME WHERE $COLUMN_COMPLETED = 1 ORDER BY $COLUMN_COMPLETED_AT DESC")
    }

    private fun getTodos(query: String): List<ToDoItem> {
        val todos = mutableListOf<ToDoItem>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {
            val todo = ToDoItem(
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMPLETED)) == 1,
                cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_COMPLETED_AT))?.let { Date(it) }
            )
            todos.add(todo)
        }
        cursor.close()
        return todos
    }

    fun updateTodo(todo: ToDoItem) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TASK, todo.task)
            put(COLUMN_COMPLETED, if (todo.isCompleted) 1 else 0)
            put(COLUMN_COMPLETED_AT, todo.completedAt?.time)
        }
        db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(todo.id.toString()))
    }

    fun deleteTodo(todoId: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(todoId.toString()))
    }

    fun clearCompletedTasks() {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_COMPLETED = 1", null)
    }
}