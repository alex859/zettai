package zettai

import java.time.LocalDate

data class ToDoList(val name: ListName, val items: List<ToDoItem>)

data class ListName internal constructor(val name: String) {
    companion object {
        private val validPattern = "[A-Za-z0-9-]+".toRegex()

        fun fromTrusted(name: String): ListName = ListName(name)

        @Suppress("MagicNumber")
        fun fromUntrusted(name: String): ListName? =
            if (name.matches(validPattern) && name.length in 1..40) {
                ListName(name)
            } else {
                null
            }
    }
}

data class ToDoItem(val description: String, val dueDate: LocalDate? = null, val status: ToDoStatus = ToDoStatus.Todo)

enum class ToDoStatus { Todo, InProgress, Done, Blocked }

data class User(val name: String)
