package zettai.util

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

internal const val ALPHABET_LOWER_CASE = "abcdefghijklmnopqrstuvxyz"
internal val ALPHABET_UPPER_CASE = "abcdefghijklmnopqrstuvxyz".uppercase()
internal const val DIGITS = "1234567890"
private val DEFAULT_CHARSET = ALPHABET_LOWER_CASE + ALPHABET_UPPER_CASE + DIGITS
