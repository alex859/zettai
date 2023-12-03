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
}

typealias ToDoListFetcher = (User, ListName) -> ToDoList?

interface ToDoListUpdatableFetcher : ToDoListFetcher {
    fun assignListToUser(
        user: User,
        list: ToDoList,
    ): ToDoList?
}

class ToDoListHub(val fetcher: ToDoListUpdatableFetcher) : ZettaiHub {
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
            listMap.apply { put(list.name, list) }
        }?.let { list }
}
