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
import zettai.ui.HtmlPage
import zettai.ui.renderListsPage
import zettai.util.andUnlessNull
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Zettai(val hub: ZettaiHub) : HttpHandler {
    val processUnlessNull =
        ::extractListData andUnlessNull
            ::fetchListContent andUnlessNull
            ::renderHtmlPage andUnlessNull
            ::createResponse

    private val showList: HttpHandler = { processUnlessNull(it) ?: Response(NOT_FOUND, "Not found") }

    private val routes: HttpHandler =
        routes(
            "/todo/{user}/{list}" bind GET to showList,
            "/todo/{user}/{list}" bind POST to ::addNewItem,
            "/todo/{user}" bind GET to ::getAllLists,
            "/todo/{user}" bind POST to ::createNewList,
        )

    private fun getAllLists(request: Request): Response {
        val user = request.extractUser()

        return hub.getLists(user)
            ?.let { renderListsPage(user, it) }
            ?.let(::createResponse)
            ?: Response(BAD_REQUEST)
    }

    private fun createNewList(request: Request): Response {
        val user = request.extractUser()
        return request.extractListNameFromForm("listname")
            ?.let { listName -> CreateToDoList(user, listName) }
            ?.let(hub::handle)
            ?.let { Response(SEE_OTHER).header("Location", "/todo/${user.name}") }
            ?: Response(BAD_REQUEST)
    }

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

        return request.form("itemname")
            ?.let(::ToDoItem)
            ?.let { AddToDoItem(user, listName, it) }
            ?.let(hub::handle)
            ?.let { Response(SEE_OTHER).header("Location", "/todo/${user.name}/${listName.name}") }
            ?: Response(BAD_REQUEST)
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
                //language=html
                """
                <!DOCTYPE html>
                <html>
                <head>
                    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css" integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">
                    <title>Zettai - a ToDoList application</title>
                </head>
                <body>
                <div id="container">
                <div class="row justify-content-md-center"> 
                <div class="col-md-center">
                    <h1>Zettai</h1>
                    <h2>ToDo List ${it.listName.name}</h2>
                    <table class="table table-hover">
                        <thead>
                            <tr>
                              <th>Name</th>
                              <th>Due Date</th>
                              <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                        ${it.renderItems()}
                        </tbody>
                    </table>
                    </div>
                </div>
                </div>
                </body>
                </html>
                """.trimIndent(),
            )
        }

    private fun ToDoList.renderItems() = items.joinToString("", transform = ::renderItem)

    private fun renderItem(it: ToDoItem): String =
        """
        <tr>
          <td>${it.description}</td>
          <td>${it.dueDate?.toIsoString().orEmpty()}</td>
          <td>${it.status}</td>
        </tr>
        """.trimIndent()

    private fun createResponse(htmlPage: HtmlPage): Response = Response(Status.OK).body(htmlPage.raw)
}

private fun Request.extractUser(): User = path("user").orEmpty().let(::User)

private fun Request.extractListNameFromForm(formName: String): ListName? =
    form(formName)?.let(ListName.Companion::fromUntrusted)

fun LocalDate.toIsoString(): String = format(DateTimeFormatter.ISO_LOCAL_DATE)

fun String?.toIsoLocalDate(): LocalDate? = unlessNullOrEmpty { LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE) }

fun <T : Any> CharSequence?.unlessNullOrEmpty(f: (CharSequence) -> T): T? = if (this.isNullOrEmpty()) null else f(this)

fun String.toStatus(): ToDoStatus = ToDoStatus.valueOf(this)
