package zettai

interface ZettaiHub {
    fun getList(
        user: User,
        listName: ListName,
    ): ToDoList?
}

class ToDoListHub(val list: Map<User, List<ToDoList>>) : ZettaiHub {
    override fun getList(
        user: User,
        listName: ListName,
    ): ToDoList? =
        list[user]
            ?.firstOrNull { it.name == listName }
}
