package zettai

import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainOnly
import com.ubertob.pesticide.core.DomainSetUp
import com.ubertob.pesticide.core.Ready

class DomainOnlyActions : ZettaiActions {
    private val lists: MutableMap<User, List<ToDoList>> = mutableMapOf()

    private val hub = ToDoListHub(lists)

    override fun ToDoListOwner.`starts with a list`(
        listName: String,
        items: List<String>,
    ) {
        lists[user] = listOf(createList(listName, items))
    }

    override fun getToDoList(
        user: User,
        listName: ListName,
    ): ToDoList? = hub.getList(user, listName)

    override val protocol: DdtProtocol = DomainOnly

    override fun prepare(): DomainSetUp = Ready
}

private fun createList(
    listName: String,
    items: List<String>,
) = ToDoList(ListName(listName), items = items.map(::ToDoItem))
