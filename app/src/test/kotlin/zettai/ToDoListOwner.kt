package zettai

import com.ubertob.pesticide.core.DdtActor
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEmpty
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.map

class ToDoListOwner(override val name: String) : DdtActor<ZettaiActions>() {
    val user = User(name)

    fun `can see #listname with #itemnames`(
        listName: String,
        expectedItems: List<String>,
    ) = step(listName, expectedItems) {
        val list = getToDoList(user, ListName(listName))
        expectThat(list).isNotNull().get { items.map { it.description } }.containsExactlyInAnyOrder(expectedItems)
    }

    fun `cannot see #listname`(listName: String) =
        step(listName) {
            expectThat(getToDoList(user, ListName(listName))).isNull()
        }

    fun `can add #item to #listname`(
        item: String,
        listName: String,
    ) = step(item, listName) {
        addListItem(user, ListName(listName), ToDoItem(item, null))
    }

    fun `cannot see any lists`() =
        step {
            val lists = allUserLists(user)
            expectThat(lists).isEmpty()
        }

    fun `can see the lists #listNames`(keys: Set<String>) =
        step(keys) {
            val lists = allUserLists(user)
            expectThat(lists)
                .map(ListName::name)
                .containsExactly(keys)
        }

    fun `can create a new list called #listname`(listName: String) =
        step(listName) {
            createList(user, ListName.fromTrusted(listName))
        }
}
