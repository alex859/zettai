package zettai.exercises5

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class Exercises5Test {
    @Test
    fun `collatz sequence`() {
        tailrec fun collatzR(
            list: List<Int>,
            x: Int,
        ): List<Int> =
            when {
                x <= 0 -> error("cannot accept negative numbers")
                x == 1 -> list + x
                x % 2 == 0 -> collatzR(list + x, x / 2)
                else -> collatzR(list + x, x * 3 + 1)
            }

        fun Int.collatz(): List<Int> = collatzR(listOf(), this)

        expectThat(13.collatz()).isEqualTo(listOf(13, 40, 20, 10, 5, 16, 8, 4, 2, 1))
        expectThat(8.collatz()).isEqualTo(listOf(8, 4, 2, 1))
    }

    @Test
    fun fold() {
        val values = listOf(Up, Up, Down, Up, Down, Down, Up, Up, Up, Down)
        val tot =
            values.fold(Elevator(0)) { acc, direction ->
                when (direction) {
                    Up -> Elevator(acc.floor + 1)
                    Down -> Elevator(acc.floor - 1)
                }
            }
        expectThat(tot).isEqualTo(Elevator(2))
    }

    @Test
    fun monoid() {
        data class Monoid<T>(val zero: T, val combine: (T, T) -> T) {
            fun List<T>.fold() = fold(zero, combine)
        }

        with(Monoid(0, Int::plus)) {
            expectThat(listOf(1, 2, 3, 4, 10).fold()).isEqualTo(20)
        }
    }
}

data class Elevator(val floor: Int)

sealed class Direction

data object Up : Direction()

data object Down : Direction()
