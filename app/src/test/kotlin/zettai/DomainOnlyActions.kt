package zettai

import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainOnly
import com.ubertob.pesticide.core.DomainSetUp
import com.ubertob.pesticide.core.Ready

class DomainOnlyActions : ZettaiActions {
    private val lists: ToDoListStore = mutableMapOf()

    val fetcher = ToDoListFetcherFromMap(store = lists)
    val retriever = ToDoListEventStore(ToDoListEventStreamerInMemory())
    private val hub =
        ToDoListHub(
            fetcher = fetcher,
            commandHandler = ToDoListCommandHandler(retriever, readModel = fetcher),
            persistEvent = retriever,
        )

    override fun ToDoListOwner.`starts with a list`(
        listName: String,
        items: List<String>,
    ) {
        val newList = createList(listName, items)
        val currentLists: MutableMap<ListName, ToDoList> = lists[user] ?: mutableMapOf()
        currentLists += (ListName(listName) to newList)
        lists[user] = currentLists
    }

    override fun getToDoList(
        user: User,
        listName: ListName,
    ): ToDoList? = hub.getList(user, listName)

    override fun addListItem(
        user: User,
        listName: ListName,
        item: ToDoItem,
    ) {
        hub.addItemToList(user, listName, item)
    }

    override fun allUserLists(user: User) = hub.getLists(user) ?: emptyList()

    override fun createList(
        user: User,
        listName: ListName,
    ) {
        hub.handle(CreateToDoList(user, listName))
    }

    override val protocol: DdtProtocol = DomainOnly

    override fun prepare(): DomainSetUp = Ready
}

private fun createList(
    listName: String,
    items: List<String>,
) = ToDoList(ListName(listName), items = items.map { ToDoItem(it, null) })
