package app

import org.http4k.server.Jetty
import org.http4k.server.asServer

fun main() {
    Zettai().asServer(Jetty(9090)).start()
}
