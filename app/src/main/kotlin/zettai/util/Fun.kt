package zettai.util

typealias FUN<A, B> = (A) -> B

infix fun <A, B, C> FUN<A, B>.andThen(other: FUN<B, C>): FUN<A, C> = { a -> other(this(a)) }

infix fun <A, B, C> FUN<A, B?>.andUnlessNull(other: FUN<B, C?>): FUN<A, C?> = { a -> a.let(this)?.let(other) }

fun <T> T.printIt(prefix: String = ">"): T = also { println("$prefix $this") }
