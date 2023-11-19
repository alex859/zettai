@file:Suppress("PackageNaming")

package exercises2

data class FuncStack<T>(private val elements: List<T> = emptyList()) {
    fun push(element: T): FuncStack<T> {
        return FuncStack(elements = elements + element)
    }

    fun size(): Int = elements.size

    fun pop(): Pair<T, FuncStack<T>> = elements.last() to copy(elements = elements.dropLast(1))
}
