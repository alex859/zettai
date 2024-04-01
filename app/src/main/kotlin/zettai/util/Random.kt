package zettai.util

import zettai.ListName
import zettai.ToDoItem
import zettai.User
import kotlin.random.Random

fun stringGenerator(
    charSet: String,
    minLength: Int,
    maxLength: Int,
): Sequence<String> = generateSequence { randomString(charSet, minLength, maxLength) }

fun randomString(
    charSet: String = DEFAULT_CHARSET,
    minLength: Int = 5,
    maxLength: Int = 10,
): String =
    StringBuilder().run {
        repeat(Random.nextInt(maxLength - minLength) + minLength) {
            append(charSet.random())
        }
        toString()
    }

fun randomListName() = ListName.fromTrusted(randomString())

fun randomUser() = User(name = randomString())

fun randomToDoItem() = ToDoItem(description = randomString())

internal const val ALPHABET_LOWER_CASE = "abcdefghijklmnopqrstuvxyz"
internal val ALPHABET_UPPER_CASE = "abcdefghijklmnopqrstuvxyz".uppercase()
internal const val DIGITS = "1234567890"
private val DEFAULT_CHARSET = ALPHABET_LOWER_CASE + ALPHABET_UPPER_CASE + DIGITS
