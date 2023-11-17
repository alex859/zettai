package app

import org.http4k.client.JettyClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class SeeATodoListTest {
    @Test
    fun `List owners can see their list`() {
        val user = "frank"
        val listName = "shopping"
        val foodToBuy = listOf("carrots", "apples", "milk")

        startApplication(user, listName, foodToBuy)

        val list = getTodoList(user, listName)

        expectThat(list.name.value) isEqualTo listName
        expectThat(list.items.map { it.description }) isEqualTo foodToBuy
    }

    private fun startApplication(
        user: String,
        listName: String,
        foodToBuy: List<String>,
    ) {
        Zettai().asServer(Jetty(9090)).start()
    }

    private fun getTodoList(
        user: String,
        listName: String,
    ): ToDoList {
        val client = JettyClient()
        val request = Request(Method.GET, "http://localhost:9090/todo/$user/$listName")
        val response = client(request)
        return if (response.status == Status.OK) {
            parseResponse(response)
        } else {
            fail(response.toMessage())
        }
    }

    private fun parseResponse(response: Response): ToDoList {
        TODO("Not yet implemented")
    }
}
