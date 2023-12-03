package zettai

import com.ubertob.pesticide.core.DdtActions
import com.ubertob.pesticide.core.DdtProtocol

interface ZettaiActions : DdtActions<DdtProtocol> {
    fun ToDoListOwner.`starts with a list`(
        listName: String,
        items: List<String>,
    )

    fun getToDoList(
        user: User,
        listName: ListName,
    ): ToDoList?

    fun addListItem(
        user: User,
        listName: ListName,
        item: ToDoItem,
    )
}
