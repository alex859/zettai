package zettai

import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.SEE_OTHER
import org.http4k.core.body.form
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import zettai.util.andThen

data class Zettai(val hub: ZettaiHub) : HttpHandler {
    private val showList: HttpHandler =
        ::extractListData andThen
            ::fetchListContent andThen
            ::renderHtmlPage andThen
            ::createResponse

    private val routes: HttpHandler =
        routes(
            "/todo/{user}/{list}" bind GET to showList,
            "/todo/{user}/{list}" bind POST to ::addNewItem,
        )

    @Suppress("ReturnCount")
    private fun addNewItem(request: Request): Response {
        val user =
            request.path("user")
                ?.let(::User)
                ?: return Response(BAD_REQUEST)
        val listName =
            request.path("list")
                ?.let(::ListName)
                ?: return Response(BAD_REQUEST)

        val item =
            request.form("itemname")
                ?.let(::ToDoItem)
                ?: return Response(BAD_REQUEST)

        return hub.addItemToList(user, listName, item)
            ?.let { Response(SEE_OTHER).header("Location", "/todo/${user.name}/${listName.name}") }
            ?: Response(NOT_FOUND)
    }

    override fun invoke(request: Request) = routes(request)

    private fun extractListData(request: Request): Pair<User, ListName> {
        val user = request.path("user").orEmpty()
        val list = request.path("list").orEmpty()
        return User(user) to ListName(list)
    }

    private fun fetchListContent(listData: Pair<User, ListName>): ToDoList? {
        val (user, listName) = listData
        return hub.getList(user, listName)
    }

    private fun renderHtmlPage(toDoList: ToDoList?) =
        toDoList?.let {
            HtmlPage(
                """
                <html>
                    <body>
                        <h1>Zettai</h1>
                        <h2>${toDoList.name.name}</h2>
                        <p>Here is a list <b>${toDoList.name.name}</b>:</p>
                        <table>
                            <tbody>${renderItems(toDoList.items)}</tbody>
                        </table>
                    </body>
                </html>
                """.trimIndent(),
            )
        }

    private fun renderItems(items: List<ToDoItem>) =
        items.map {
            """<tr><td>${it.description}</td></tr>"""
        }.joinToString(separator = " ")

    private fun createResponse(htmlPage: HtmlPage?): Response =
        htmlPage?.let { Response(Status.OK).body(htmlPage.raw) } ?: Response(NOT_FOUND)
}

data class HtmlPage(val raw: String)
