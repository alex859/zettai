package zettai

import java.time.LocalDate

data class ToDoList(val name: ListName, val items: List<ToDoItem>)

data class ListName(val name: String)

data class ToDoItem(val description: String, val dueDate: LocalDate? = null)

data class User(val name: String)
