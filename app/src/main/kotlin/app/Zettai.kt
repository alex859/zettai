package app

import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.routing.bind
import org.http4k.routing.routes

class Zettai : HttpHandler {
    private val routes: HttpHandler =
        routes(
            "/todo/{user}/{list}" bind Method.GET to ::showList,
        )

    private fun showList(request: Request): Response {
        return request
            .let(::extractListData)
            .let(::fetchListContent)
            .let(::renderHtmlPage)
            .let(::createResponse)
//        val user: String? = request.path("user")
//        val list: String? = request.path("list")
//        val html =
//            """
//            <html>
//                <body>
//                    <h1>Zettai</h1>
//                    <p>Here is a list <b>$list</b> of user <b>$user</b></p>
//                </body>
//            </html>
//            """.trimIndent()
//        return Response(Status.OK).body(html)
    }

    override fun invoke(request: Request) = routes(request)
}

private fun extractListData(request: Request): Pair<User, ListName> = TODO()

private fun fetchListContent(listData: Pair<User, ListName>): ToDoList = TODO()

private fun renderHtmlPage(toDoList: ToDoList): HtmlPage = TODO()

private fun createResponse(htmlPage: HtmlPage): Response = TODO()

data class HtmlPage(val html: String)

data class ToDoList(val name: ListName, val items: List<ToDoItem>)

data class ListName(val value: String)

data class ToDoItem(val description: String)

data class User(val name: String)
