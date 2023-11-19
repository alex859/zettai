package app

import org.http4k.client.JettyClient
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.filter.ClientFilters
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.opentest4j.AssertionFailedError
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

private fun ToDoListOwner.asUser() = User(name)

class SeeATodoListTest {
    val frank = ToDoListOwner("frank")
    val foodToBuy = listOf("carrots", "apples", "milk")
    val frankList = createList("shopping", foodToBuy)

    val bob = ToDoListOwner("bob")
    val gardenItems = listOf("fix the lawn", "water the plants")
    val bobList = createList("gardening", gardenItems)

    val lists =
        mapOf(
            frank.asUser() to listOf(frankList),
            bob.asUser() to listOf(bobList),
        )

    @Test
    fun `List owners can see their list`() {
        val app = startApplication(lists)
        app.runScenario(
            frank.canSeeTheList("shopping", foodToBuy),
            bob.canSeeTheList("gardening", gardenItems),
        )
    }

    @Test
    fun `Only owners can see their lists`() {
        val app = startApplication(lists)
        app.runScenario(
            frank.cannotSeeTheList("gardening"),
            bob.cannotSeeTheList("shopping"),
        )
    }
}

private fun startApplication(lists: Map<User, List<ToDoList>>): ApplicationForAcceptanceTest {
    val port = 9090
    val server = Zettai(ToDoListHub(lists)).asServer(Jetty(port))
    server.start()

    val client =
        ClientFilters
            .SetBaseUriFrom(Uri.of("http://localhost:$port"))
            .then(JettyClient())

    return ApplicationForAcceptanceTest(client, server)
}

interface ScenarioActor {
    val name: String
}

class ToDoListOwner(override val name: String) : ScenarioActor {
    fun canSeeTheList(
        listName: String,
        items: List<String>,
    ): Step =
        {
            val expectedList = createList(listName, items)

            val list = getTodoList(name, listName)

            expectThat(list).isEqualTo(expectedList)
        }

    fun cannotSeeTheList(listName: String): Step =
        {
            expectThrows<AssertionFailedError> {
                getTodoList(name, listName)
            }
        }
}

class ApplicationForAcceptanceTest(val client: HttpHandler, private val server: AutoCloseable) : Actions {
    override fun getTodoList(
        user: String,
        listName: String,
    ): ToDoList {
        val response = client(Request(Method.GET, "/todo/$user/$listName"))
        return if (response.status == Status.OK) {
            parseResponse(response.bodyString())
        } else {
            fail(response.toMessage())
        }
    }

    fun runScenario(vararg steps: Step) {
        server.use {
            steps.onEach { step -> this.step() }
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

private fun createList(
    listName: String,
    items: List<String>,
) = ToDoList(ListName(listName), items = items.map(::ToDoItem))

interface Actions {
    fun getTodoList(
        user: String,
        listName: String,
    ): ToDoList?
}

typealias Step = Actions.() -> Unit
