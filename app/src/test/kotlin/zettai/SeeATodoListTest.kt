package zettai

import com.ubertob.pesticide.core.DDT
import com.ubertob.pesticide.core.DdtActions
import com.ubertob.pesticide.core.DdtActor
import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainDrivenTest
import com.ubertob.pesticide.core.DomainOnly
import com.ubertob.pesticide.core.DomainSetUp
import com.ubertob.pesticide.core.Http
import com.ubertob.pesticide.core.Ready
import org.http4k.client.JettyClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.junit.jupiter.api.fail
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isNotNull
import strikt.assertions.isNull

typealias ZettaiDDT = DomainDrivenTest<ZettaiActions>

fun allActions() = setOf(DomainOnlyActions(), HttpActions())

class SeeATodoListTest : ZettaiDDT(allActions()) {
    val frank by NamedActor(::ToDoListOwner)
    val bob by NamedActor(::ToDoListOwner)

    val foodToBuy = listOf("carrots", "apples", "milk")
    val shoppingListName = "shopping"
    val gardeningListName = "gardening"
    val gardenItems = listOf("fix the lawn", "water the plants")

    @DDT
    fun `List owners can see their list`() =
        ddtScenario {
            setUp {
                frank.`starts with a list`(shoppingListName, foodToBuy)
                bob.`starts with a list`(gardeningListName, gardenItems)
            }.thenPlay(
                frank.`can see #listname with #itemnames`(shoppingListName, foodToBuy),
                bob.`can see #listname with #itemnames`(gardeningListName, gardenItems),
            )
        }

    @DDT
    fun `Only owners can see their lists`() =
        ddtScenario {
            setUp {
                frank.`starts with a list`(gardeningListName, gardenItems)
                bob.`starts with a list`(shoppingListName, foodToBuy)
            }.thenPlay(
                frank.`cannot see #listname`(shoppingListName),
                bob.`cannot see #listname`(gardeningListName),
            )
        }
}

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

private fun createList(
    listName: String,
    items: List<String>,
) = ToDoList(ListName(listName), items = items.map(::ToDoItem))

interface ZettaiActions : DdtActions<DdtProtocol> {
    fun ToDoListOwner.`starts with a list`(
        listName: String,
        items: List<String>,
    )

    fun getToDoList(
        user: User,
        listName: ListName,
    ): ToDoList?
}

class DomainOnlyActions : ZettaiActions {
    private val lists: MutableMap<User, List<ToDoList>> = mutableMapOf()

    private val hub = ToDoListHub(lists)

    override fun ToDoListOwner.`starts with a list`(
        listName: String,
        items: List<String>,
    ) {
        lists[user] = listOf(createList(listName, items))
    }

    override fun getToDoList(
        user: User,
        listName: ListName,
    ): ToDoList? = hub.getList(user, listName)

    override val protocol: DdtProtocol = DomainOnly

    override fun prepare(): DomainSetUp = Ready
}

class HttpActions(env: String = "local") : ZettaiActions {
    private val lists: MutableMap<User, List<ToDoList>> = mutableMapOf()

    private val hub = ToDoListHub(lists)
    val zettaiPort = 9090
    val server = Zettai(hub).asServer(Jetty(zettaiPort))
    val client = JettyClient()

    override fun ToDoListOwner.`starts with a list`(
        listName: String,
        items: List<String>,
    ) {
        lists[user] = listOf(createList(listName, items))
    }

    override fun getToDoList(
        user: User,
        listName: ListName,
    ): ToDoList? {
        val response = callZettai(Method.GET, "todo/${user.name}/${listName.value}")
        return when (response.status) {
            Status.OK -> parseResponse(response.bodyString())
            Status.NOT_FOUND -> null
            else -> fail(response.toMessage())
        }
    }

    override val protocol: DdtProtocol = Http(env)

    override fun prepare(): DomainSetUp {
        server.start()
        return Ready
    }

    override fun tearDown(): DdtActions<DdtProtocol> = also { server.stop() }

    private fun callZettai(
        method: Method,
        path: String,
    ): Response = client(Request(method, "http://localhost:$zettaiPort/$path"))

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
