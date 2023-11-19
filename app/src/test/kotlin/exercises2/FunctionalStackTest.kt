@file:Suppress("PackageNaming")

package exercises2

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class FunctionalStackTest {
    @Test
    fun `push into the stack`() {
        val stack1 = FuncStack<Char>()
        val stack2 = stack1.push('A')
        val stack3 = stack2.push('B')

        expectThat(stack1.size()).isEqualTo(0)
        expectThat(stack2.size()).isEqualTo(1)
        expectThat(stack3.size()).isEqualTo(2)
    }

    @Test
    fun `push push pop`() {
        val (b, stack) = FuncStack<Char>().push('A').push('B').pop()

        expectThat(stack.size()).isEqualTo(1)
        expectThat(b).isEqualTo('B')
        expectThat(stack.pop().first).isEqualTo('A')
    }
}
