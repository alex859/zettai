package app

import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes

class Zettai : HttpHandler {
    private val routes: HttpHandler =
        routes(
            "/todo/{user}/{list}" bind Method.GET to ::showList,
        )

    private fun showList(request: Request): Response {
        val user: String? = request.path("user")
        val list: String? = request.path("list")
        val html =
            """
            <html>
                <body>
                    <h1>Zettai</h1>
                    <p>Here is a list <b>$list</b> of user <b>$user</b></p>
                </body>
            </html>
            """.trimIndent()
        return Response(Status.OK).body(html)
    }

    override fun invoke(request: Request) = routes(request)
}
