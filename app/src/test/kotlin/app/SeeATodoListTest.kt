package app

import org.http4k.client.JettyClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.server.Http4kServer
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.opentest4j.AssertionFailedError
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

class SeeATodoListTest {
    @Test
    fun `List owners can see their list`() {
        val listName = "shopping"
        val foodToBuy = listOf("carrots", "apples", "milk")
        val frank = ToDoListOwner("frank")

        val server = startApplication(frank.name, listName, foodToBuy).also { it.start() }

        frank.canSeeTheList(listName, foodToBuy, ApplicationForAcceptanceTest())

        server.stop()
    }

    @Test
    fun `Only owners can see their lists`() {
        val listName = "shopping"
        val bob = ToDoListOwner("bob")

        val server = startApplication("frank", listName, emptyList()).also { it.start() }

        bob.cannotSeeTheList(listName, ApplicationForAcceptanceTest())

        server.stop()
    }

    private fun startApplication(
        user: String,
        listName: String,
        foodToBuy: List<String>,
    ): Http4kServer {
        val toDoList = ToDoList(ListName(listName), items = foodToBuy.map(::ToDoItem))
        val lists = mapOf(User(user) to listOf(toDoList))
        val server = Zettai(lists).asServer(Jetty(9090))
        return server
    }
}

interface ScenarioActor {
    val name: String
}

class ToDoListOwner(override val name: String) : ScenarioActor {
    fun canSeeTheList(listName: String, items: List<String>, app: ApplicationForAcceptanceTest) {
        val expectedList = createList(listName, items)

        val list = app.getTodoList(name, listName)

        expectThat(list).isEqualTo(expectedList)
    }

    fun cannotSeeTheList(listName: String, app: ApplicationForAcceptanceTest) {
        expectThrows<AssertionFailedError> {
            app.getTodoList(name, listName)
        }
    }

    private fun createList(listName: String, items: List<String>) =
        ToDoList(ListName(listName), items = items.map(::ToDoItem))
}

class ApplicationForAcceptanceTest {
    fun getTodoList(
        user: String,
        listName: String,
    ): ToDoList {
        val client = JettyClient()
        val request = Request(Method.GET, "http://localhost:9090/todo/$user/$listName")
        val response = client(request)
        return if (response.status == Status.OK) {
            parseResponse(response.bodyString())
        } else {
            fail(response.toMessage())
        }
    }

    private fun parseResponse(html: String): ToDoList {
        val nameRegex = "<h2>.*<".toRegex()
        val listName = ListName(extractListName(nameRegex, html))
        val itemRegex = "<td>.*?<".toRegex()
        val items = itemRegex.findAll(html).map { ToDoItem(extractItemsDesc(it)) }.toList()

        return ToDoList(listName, items)
    }

    private fun extractItemsDesc(matchResult: MatchResult): String =
        matchResult.value.substringAfter("<td>").dropLast(1)

    private fun extractListName(
        nameRegex: Regex,
        html: String,
    ): String =
        nameRegex.find(html)
            ?.value
            ?.substringAfter("<h2>")
            ?.dropLast(1)
            .orEmpty()
}