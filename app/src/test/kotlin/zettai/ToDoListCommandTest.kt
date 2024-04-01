package zettai

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import zettai.util.randomListName
import zettai.util.randomUser

class ToDoListCommandTest {
    val fakeRetriever: ToDoListRetriever = ToDoListRetriever { _, _ -> InitialState }
    val fetcher = ToDoListFetcherFromMap(mutableMapOf())

    @Test
    fun `create to do list generates the correct event`() {
        val cmd = CreateToDoList(user = randomUser(), name = randomListName())

        val handler = ToDoListCommandHandler(fakeRetriever, readModel = fetcher)
        val result = handler(cmd)?.single()

        expectThat(result).isEqualTo(ListCreated(cmd.id, cmd.user, cmd.name))
    }

    @Test
    fun `add list fails if the user has already a list with the same name`() {
        val cmd = CreateToDoList(user = randomUser(), name = randomListName())
        val handler = ToDoListCommandHandler(fakeRetriever, fetcher)

        fun handle(cmd: TodoListCommand) = handler(cmd)
        val result = handler(cmd)?.single()

        expectThat(result).isA<ListCreated>()

        val duplicates = handler(cmd)
        expectThat(duplicates).isNull()
    }
}
