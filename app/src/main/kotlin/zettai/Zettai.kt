package zettai

import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
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
            "/todo/{user}/{list}" bind Method.GET to showList,
        )

    override fun invoke(request: Request) = routes(request)

    private fun extractListData(request: Request): Pair<User, ListName> {
        val user = request.path("user").orEmpty()
        val list = request.path("list").orEmpty()
        return User(user) to ListName(list)
    }

    private fun fetchListContent(listData: Pair<User, ListName>): ToDoList {
        val (user, listName) = listData
        return hub.getList(user, listName) ?: error("Unknown list")
    }

    private fun renderHtmlPage(toDoList: ToDoList) =
        HtmlPage(
            """
            <html>
                <body>
                    <h1>Zettai</h1>
                    <h2>${toDoList.name.value}</h2>
                    <p>Here is a list <b>${toDoList.name.value}</b>:</p>
                    <table>
                        <tbody>${renderItems(toDoList.items)}</tbody>
                    </table>
                </body>
            </html>
            """.trimIndent(),
        )

    private fun renderItems(items: List<ToDoItem>) =
        items.map {
            """<tr><td>${it.description}</td></tr>"""
        }.joinToString(separator = " ")

    private fun createResponse(htmlPage: HtmlPage): Response = Response(Status.OK).body(htmlPage.raw)
}

data class HtmlPage(val raw: String)
