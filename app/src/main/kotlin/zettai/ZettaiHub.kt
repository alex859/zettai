package zettai

interface ZettaiHub {
    fun getList(
        user: User,
        listName: ListName,
    ): ToDoList?

    fun addItemToList(
        user: User,
        listName: ListName,
        item: ToDoItem,
    ): ToDoList?

    fun getLists(user: User): List<ListName>?

    fun createToDoList(
        user: User,
        listName: ListName,
    )

    fun handle(command: TodoListCommand): TodoListCommand?
}

typealias ToDoListFetcher = (User, ListName) -> ToDoList?

interface ToDoListUpdatableFetcher : ToDoListFetcher {
    fun assignListToUser(
        user: User,
        list: ToDoList,
    ): ToDoList?

    fun getAll(user: User): List<ListName>?

    fun addItemToList(
        user: User,
        listName: ListName,
        item: ToDoItem,
    ) {
        invoke(user, listName)?.run {
            val newList = copy(items = items.filterNot { it.description == item.description } + item)
            assignListToUser(user, newList)
        }
    }
}

class ToDoListHub(
    val fetcher: ToDoListUpdatableFetcher,
    val commandHandler: ToDoListCommandHandler,
    val persistEvent: EventPersister<ToDoListEvent>,
) : ZettaiHub {
    override fun getList(
        user: User,
        listName: ListName,
    ): ToDoList? = fetcher(user, listName)

    override fun addItemToList(
        user: User,
        listName: ListName,
        item: ToDoItem,
    ): ToDoList? =
        fetcher(user, listName)?.run {
            val newList = copy(items = items.replaceItem(item))
            fetcher.assignListToUser(user, newList)
        }

    override fun getLists(user: User): List<ListName>? {
        return fetcher.getAll(user)
    }

    override fun createToDoList(
        user: User,
        listName: ListName,
    ) {
        TODO("Not yet implemented")
    }

    override fun handle(command: TodoListCommand): TodoListCommand? {
        return commandHandler(command)
            ?.let(persistEvent)
            ?.let { command }
    }
}

private fun List<ToDoItem>.replaceItem(item: ToDoItem): List<ToDoItem> =
    filterNot { it.description == item.description } + item

typealias ToDoListStore = MutableMap<User, MutableMap<ListName, ToDoList>>

class ToDoListFetcherFromMap(private val store: ToDoListStore) : ToDoListUpdatableFetcher {
    override fun invoke(
        user: User,
        listName: ListName,
    ): ToDoList? = store[user]?.get(listName)

    override fun assignListToUser(
        user: User,
        list: ToDoList,
    ): ToDoList? =
        store.compute(user) { _, value ->
            val listMap = value ?: mutableMapOf()
            listMap.apply { put(list.listName, list) }
        }?.let { list }

    override fun getAll(user: User): List<ListName>? {
        return store[user]?.keys?.toList()
    }
}
