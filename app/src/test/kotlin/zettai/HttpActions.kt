package zettai

import com.ubertob.pesticide.core.DdtActions
import com.ubertob.pesticide.core.DdtProtocol
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

private fun createList(
    listName: String,
    items: List<String>,
) = ToDoList(ListName(listName), items = items.map(::ToDoItem))
