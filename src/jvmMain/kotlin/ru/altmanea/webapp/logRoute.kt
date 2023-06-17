package ru.altmanea.webapp

import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.logRoute() {
    log.info("Current route:")
    val root = plugin(Routing)
    val allRoutes = allRoutes(root)
    val allRoutesWithMethod = allRoutes.filter { it.selector is HttpMethodRouteSelector }
    allRoutesWithMethod.forEach {
        log.info("route: $it")
    }
}

fun allRoutes(root: Route): List<Route> {
    return listOf(root) + root.children.flatMap { allRoutes(it) }
}