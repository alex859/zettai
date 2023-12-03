package zettai

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import zettai.util.ALPHABET_LOWER_CASE
import zettai.util.ALPHABET_UPPER_CASE
import zettai.util.DIGITS
import zettai.util.randomString
import zettai.util.stringGenerator

class ListNameTest {
    val validCharset = ALPHABET_UPPER_CASE + ALPHABET_LOWER_CASE + DIGITS + "-"
    val invalidCharset = " !^&%$£@?+_-=|\\"

    @Test
    fun `Valid names are alphanumeric and hyphen between 3 and 40 character length`() {
        stringGenerator(validCharset, 3, 40)
            .take(100)
            .forEach {
                expectThat(ListName.fromUntrusted(it)).isEqualTo(ListName.fromTrusted(it))
            }
    }

    @Test
    fun `Name cannot be empty`() {
        expectThat(ListName.fromUntrusted("")).isNull()
    }

    @Test
    fun `Names longer than 40 characters are not allowed`() {
        stringGenerator(validCharset, 41, 200)
            .take(100)
            .forEach {
                expectThat(ListName.fromUntrusted(it)).isNull()
            }
    }

    @Test
    fun `Invalid chars are not allowed in the name`() {
        stringGenerator(validCharset, 3, 10)
            .map { it + randomString(invalidCharset, 3, 6) }
            .take(100)
            .forEach {
                expectThat(ListName.fromUntrusted(it)).isNull()
            }
    }
}
