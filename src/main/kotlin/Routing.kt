package com.divora

import com.divora.routes.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        tutorialRoutes()
        simulatorRoutes()
    }
}
