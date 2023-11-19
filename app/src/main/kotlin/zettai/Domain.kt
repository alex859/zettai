package zettai

data class ToDoList(val name: ListName, val items: List<ToDoItem>)

data class ListName(val value: String)

data class ToDoItem(val description: String)

data class User(val name: String)
