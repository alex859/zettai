package zettai.exercises3

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class StringTemplateTest {
    @Test
    fun `a test`() {
        val template = """
            Happy birthday {name} {surname}
            from {sender}
        """.trimIndent()

        val data = mapOf(
            "name" tag "Uberto",
            "surname" tag "Barbini",
            "sender" tag "PragProg",
        )

        val actual = renderTemplate(template, data)

        expectThat(actual).isEqualTo(
            """
            Happy birthday Uberto Barbini
            from PragProg
        """.trimIndent())
    }

}

data class StringTag(val text: String)

fun renderTemplate(template: String, data: Map<String, StringTag>): String {
    return data.entries.fold(template) { acc, tag -> acc.replace("{${tag.key}}", tag.value.text) }
}

infix fun String.tag(value: String) = Pair(this, StringTag(value))