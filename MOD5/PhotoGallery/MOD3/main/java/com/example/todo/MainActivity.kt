package com.example.todo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var db: ToDoDatabase
    private lateinit var todoRecyclerView: RecyclerView
    private lateinit var todoInput: EditText
    private lateinit var addButton: Button
    private lateinit var viewCompletedButton: Button
    private lateinit var adapter: TodoAdapter
    private var todos: MutableList<ToDoItem> = mutableListOf()
    private var viewingCompleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = ToDoDatabase(this)
        todoRecyclerView = findViewById(R.id.todoRecyclerView)
        todoInput = findViewById(R.id.todoInput)
        addButton = findViewById(R.id.addButton)
        viewCompletedButton = findViewById(R.id.viewCompletedButton)

        adapter = TodoAdapter(todos) { completedTodo ->
            completedTodo.isCompleted = true
            completedTodo.completedAt = Date()
            db.updateTodo(completedTodo)
            loadTodos()
        }
        todoRecyclerView.adapter = adapter
        todoRecyclerView.layoutManager = LinearLayoutManager(this)

        val itemTouchHelper = ItemTouchHelper(SwipeToRevealCallback(adapter))
        itemTouchHelper.attachToRecyclerView(todoRecyclerView)

        loadTodos()

        addButton.setOnClickListener {
            val task = todoInput.text.toString()
            if (task.isNotEmpty()) {
                val todo = ToDoItem(task = task)
                db.addTodo(todo)
                loadTodos()
                todoInput.text.clear()
            }
        }

        viewCompletedButton.setOnClickListener {
            viewingCompleted = !viewingCompleted
            loadTodos()
            viewCompletedButton.text = if (viewingCompleted) "View Active Tasks" else "View Completed Tasks"
        }
    }

    private fun loadTodos() {
        todos = if (viewingCompleted) {
            db.getCompletedTodos().toMutableList()
        } else {
            db.getAllTodos().toMutableList()
        }
        adapter.updateTodos(todos)
    }
}

class TodoAdapter(
    private var todos: List<ToDoItem>,
    private val onCompleteTask: (ToDoItem) -> Unit
) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    class TodoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskText: TextView = view.findViewById(R.id.taskText)
        val completeButton: Button = view.findViewById(R.id.completeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.todo_item, parent, false)
        return TodoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val todo = todos[position]
        holder.taskText.text = todo.task
        if (todo.isCompleted) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            holder.taskText.text = "${todo.task} (Completed: ${dateFormat.format(todo.completedAt)})"
            holder.completeButton.visibility = View.GONE
        } else {
            holder.completeButton.visibility = View.VISIBLE
            holder.completeButton.setOnClickListener { onCompleteTask(todo) }
        }
    }

    override fun getItemCount() = todos.size

    fun updateTodos(newTodos: List<ToDoItem>) {
        todos = newTodos
        notifyDataSetChanged()
    }
}

class SwipeToRevealCallback(private val adapter: TodoAdapter) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Do nothing on swipe, as we're just revealing the complete button
        adapter.notifyItemChanged(viewHolder.adapterPosition)
    }
}