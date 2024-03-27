package zettai

import org.http4k.server.Jetty
import org.http4k.server.asServer

fun main() {
    val items = listOf("write a book", "play piano")
    val toDoList = ToDoList(ListName("example"), items = items.map { ToDoItem(it, null) })
    val lists = ToDoListFetcherFromMap(mutableMapOf(User("cicci") to mutableMapOf(toDoList.listName to toDoList)))
    val server = Zettai(ToDoListHub(lists)).asServer(Jetty(PORT))
    server.start()
    println("Server started at http://localhost:$PORT/todo/cicci/example")
}

private const val PORT = 9090
