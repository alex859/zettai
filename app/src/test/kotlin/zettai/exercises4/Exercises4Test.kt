package zettai.exercises4

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import zettai.ToDoItem
import zettai.ToDoStatus
import zettai.util.`+++`
import zettai.util.curry

class Exercises4Test {
    @Test
    fun `discard unless`() {
        val itemInProgress = ToDoItem("doing something", status = ToDoStatus.InProgress)
        val itemBlocked = ToDoItem("something else", status = ToDoStatus.Blocked)

        expectThat(itemInProgress.discardUnless { status == ToDoStatus.InProgress }).isEqualTo(itemInProgress)
        expectThat(itemBlocked.discardUnless { status == ToDoStatus.InProgress }).isEqualTo(null)
    }

    @Test
    fun `a curry`() {
        fun sum(num1: Int, num2: Int) = num1 + num2


        val plus3 = ::sum.curry() `+++` 3
        val starPrefixFn = ::strConcat.curry()("*")

        expectThat(plus3(5)).isEqualTo(8)
        expectThat(starPrefixFn("something")).isEqualTo("* something")
    }

    @Test
    fun `curried concat`() {
        val curriedConcat = ::strConcat.curry()

        expectThat(curriedConcat `+++` "head" `+++` "tail").isEqualTo("head tail")
    }

    fun strConcat(str1: String, str2: String) = "$str1 $str2"
}

fun <T> T.discardUnless(predicate: T.() -> Boolean): T? = takeIf { predicate(it) }