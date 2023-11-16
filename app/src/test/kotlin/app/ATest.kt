package app

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ATest {
    @Test
    fun `just a test`() {
        expectThat(12) isEqualTo 23
    }
}