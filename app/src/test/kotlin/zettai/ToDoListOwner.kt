package zettai

import com.ubertob.pesticide.core.DdtActor
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isNotNull
import strikt.assertions.isNull

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
}
