package zettai

import org.http4k.server.Jetty
import org.http4k.server.asServer

fun main() {
    val items = listOf("write a book", "play piano")
    val toDoList = ToDoList(ListName("example"), items = items.map { ToDoItem(it, null) })
    val lists = mutableMapOf(User("cicci") to mutableMapOf(toDoList.listName to toDoList))

    val hun =
        zettaiHub(
            eventStore = ToDoListEventStore(eventStreamer = ToDoListEventStreamerInMemory()),
            fetcher = ToDoListFetcherFromMap(lists),
        )

    val server = Zettai(hun).asServer(Jetty(PORT))
    server.start()
    println("Server started at http://localhost:$PORT/todo/cicci/example")
}

fun zettaiHub(
    eventStore: ToDoListEventStore,
    fetcher: ToDoListUpdatableFetcher,
) = ToDoListHub(
    fetcher = fetcher,
    commandHandler = ToDoListCommandHandler(retriever = eventStore, readModel = fetcher),
    persistEvent = eventStore,
)

private const val PORT = 9090
