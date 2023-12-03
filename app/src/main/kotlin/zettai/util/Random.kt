package zettai.util

import kotlin.random.Random

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

private const val ALPHABET_LOWER_CASE = "abcdefghijklmnopqrstuvxyz"
private val ALPHABET_UPPER_CASE = "abcdefghijklmnopqrstuvxyz".uppercase()
private const val NUMBERS = "1234567890"
private val DEFAULT_CHARSET = ALPHABET_LOWER_CASE + ALPHABET_UPPER_CASE + NUMBERS
