@file:Suppress("PackageNaming")

package zettai.exercises2

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.math.BigDecimal
import java.math.RoundingMode

class ReversePolishNotationTest {
    @Test
    fun `calculates the result`() {
        expectThat("4 5 +".calc()).isEqualTo("9.0".toBigDecimal())
        expectThat("5 1 -".calc()).isEqualTo("4.0".toBigDecimal())
        expectThat("6 2 /".calc()).isEqualTo("3.0".toBigDecimal())
        expectThat("5 6 2 1 + / *".calc()).isEqualTo("10.0".toBigDecimal())
        expectThat("2 5 * 4 + 3 2 * 1 + /".calc()).isEqualTo("2.0".toBigDecimal())
    }

    private fun String.calc(): BigDecimal =
        elements().fold(emptyStack) { stack, element ->
            element.toOperationOrNull()
                ?.let { operation ->
                    stack.operandsAndResult().let { (operands, result) ->
                        result.push(operation(operands.second, operands.first).rounded())
                    }
                } ?: stack.push(element.toBigDecimal().rounded())
        }.pop().first

    private fun String.elements() = split(" ")
}

private fun String.toOperationOrNull(): ((BigDecimal, BigDecimal) -> BigDecimal)? =
    when (this) {
        "+" -> BigDecimal::plus
        "/" -> BigDecimal::divide
        "-" -> BigDecimal::minus
        "*" -> BigDecimal::times
        else -> null
    }

private fun FuncStack<BigDecimal>.operandsAndResult(): Pair<Pair<BigDecimal, BigDecimal>, FuncStack<BigDecimal>> {
    val (operand1, stack1) = pop()
    val (operand2, stack2) = stack1.pop()
    return (Pair(operand1, operand2) to stack2)
}

private fun BigDecimal.rounded() = setScale(1, RoundingMode.HALF_UP)

val emptyStack = FuncStack<BigDecimal>()
