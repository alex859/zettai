package zettai

import com.ubertob.pesticide.core.DdtActions
import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainSetUp
import com.ubertob.pesticide.core.Http
import com.ubertob.pesticide.core.Ready
import org.http4k.client.JettyClient
import org.http4k.core.Method
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.body.Form
import org.http4k.core.body.toBody
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.junit.jupiter.api.fail
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class HttpActions(env: String = "local") : ZettaiActions {
    private val lists: ToDoListStore = mutableMapOf()

    private val hub = ToDoListHub(ToDoListFetcherFromMap(lists))
    val zettaiPort = 9090
    val server = Zettai(hub).asServer(Jetty(zettaiPort))
    val client = JettyClient()

    override fun ToDoListOwner.`starts with a list`(
        listName: String,
        items: List<String>,
    ) {
        lists[user] = mutableMapOf(ListName(listName) to createList(listName, items))
    }

    override fun getToDoList(
        user: User,
        listName: ListName,
    ): ToDoList? {
        val response = callZettai(GET, "todo/${user.name}/${listName.name}")
        return when (response.status) {
            Status.OK -> parseResponse(response.bodyString())
            Status.NOT_FOUND -> null
            else -> fail(response.toMessage())
        }
    }

    override fun addListItem(
        user: User,
        listName: ListName,
        item: ToDoItem,
    ) {
        val response =
            submitToZettai(
                todoListUrl(user, listName),
                listOf(
                    "itemname" to item.description,
                    "itemdue" to item.dueDate?.toString(),
                ),
            )
        expectThat(response.status).isEqualTo(Status.SEE_OTHER)
    }

    private fun todoListUrl(
        user: User,
        listName: ListName,
    ) = "todo/${user.name}/${listName.name}"

    private fun submitToZettai(
        path: String,
        form: Form,
    ) = client(Request(POST, "http://localhost:$zettaiPort/$path").body(form.toBody()))

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
        val items = itemRegex.findAll(html).map { ToDoItem(extractItemsDesc(it), null) }.toList()

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
) = ToDoList(ListName(listName), items = items.map { ToDoItem(it, null) })
