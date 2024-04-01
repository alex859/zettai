package zettai

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import zettai.util.randomString

class ToDoListEventTest {
    val id = TodoListId.mint()
    val name = ListName.fromTrusted(randomString())
    val user = User(name = randomString())
    val item1 = ToDoItem(description = randomString())
    val item2 = ToDoItem(description = randomString())
    val item3 = ToDoItem(description = randomString())

    @Test
    fun `the first event creates a list`() {
        val events = listOf(ListCreated(id, user, name))

        val list = events.fold()

        expectThat(list).isEqualTo(ActiveToDoList(id, user, name, emptyList()))
    }

    @Test
    fun `add and remove items from the list`() {
        val events =
            listOf(
                ListCreated(id, user, name),
                ItemAdded(id, item1),
                ItemAdded(id, item2),
                ItemAdded(id, item3),
                ItemRemoved(id, item2),
            )

        val list = events.fold()

        expectThat(list).isEqualTo(ActiveToDoList(id, user, name, listOf(item1, item3)))
    }

    @Test
    fun `put list on hold`() {
        val events =
            listOf(
                ListCreated(id, user, name),
                ItemAdded(id, item1),
                ItemAdded(id, item2),
                ItemAdded(id, item3),
                ListPutOnHold(id, "not urgent"),
            )

        val list = events.fold()

        expectThat(list).isEqualTo(OnHoldToDoList(id, user, name, listOf(item1, item2, item3), "not urgent"))
    }
}
