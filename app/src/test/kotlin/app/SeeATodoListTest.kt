package app

import org.http4k.client.JettyClient
import org.http4k.core.Method
import org.http4k.core.Request
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
        val toDoList = ToDoList(ListName(listName), items = foodToBuy.map(::ToDoItem))
        val lists = mapOf(User(user) to listOf(toDoList))
        val server = Zettai(lists).asServer(Jetty(9090))
        server.start()
    }

    private fun getTodoList(
        user: String,
        listName: String,
    ): ToDoList {
        val client = JettyClient()
        val request = Request(Method.GET, "http://localhost:9090/todo/$user/$listName")
        val response = client(request)
        return if (response.status == Status.OK) {
            parseResponse(response.bodyString())
        } else {
            fail(response.toMessage())
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
