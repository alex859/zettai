package zettai.exercises4

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import zettai.ToDoItem
import zettai.ToDoStatus

class Exercises4Test {
    @Test
    fun `discard unless`() {
        val itemInProgress = ToDoItem("doing something", status = ToDoStatus.InProgress)
        val itemBlocked = ToDoItem("something else", status = ToDoStatus.Blocked)

        expectThat(itemInProgress.discardUnless { status == ToDoStatus.InProgress }).isEqualTo(itemInProgress)
        expectThat(itemBlocked.discardUnless { status == ToDoStatus.InProgress }).isEqualTo(null)
    }
}

fun <T> T.discardUnless(predicate: T.() -> Boolean): T? = takeIf { predicate(it) }