package zettai

import org.junit.jupiter.api.Test
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import zettai.util.randomString

class ToDoListHubTest {
    fun emptyStore(): ToDoListStore = mutableMapOf()

    val fetcher = ToDoListFetcherFromMap(emptyStore())

    val hub =
        zettaiHub(
            eventStore = ToDoListEventStore(eventStreamer = ToDoListEventStreamerInMemory()),
            fetcher = fetcher,
        )

    @Test
    fun `get list by user and name`() {
        repeat(10) {
            val user = randomUser()
            val list = randomToDoList()
            fetcher.assignListToUser(user, list)

            val myList = hub.getList(user, list.listName)

            expectThat(myList).isEqualTo(list)
        }
    }

    private fun randomToDoList(): ToDoList {
        return ToDoList(ListName(randomString()), emptyList())
    }

    private fun randomUser(): User = User(randomString())

    @Test
    fun `don't get lists from other users`() {
        repeat(10) {
            val user1 = randomUser()
            val list1 = randomToDoList()
            val user2 = randomUser()
            val list2 = randomToDoList()
            fetcher.assignListToUser(user1, list1)
            fetcher.assignListToUser(user2, list2)

            expect {
                that(hub.getList(user1, list2.listName)).isNull()
                that(hub.getList(user2, list1.listName)).isNull()
            }
        }
    }
}
