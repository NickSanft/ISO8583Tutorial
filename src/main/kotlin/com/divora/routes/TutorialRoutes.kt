package com.divora.routes

import com.divora.views.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.routing.*

fun Route.tutorialRoutes() {
    get("/") {
        call.respondHtml {
            renderHomePage()
        }
    }

    get("/tutorial/introduction") {
        call.respondHtml {
            renderIntroductionPage()
        }
    }

    get("/tutorial/mti") {
        call.respondHtml {
            renderMTIPage()
        }
    }

    get("/tutorial/bitmap") {
        call.respondHtml {
            renderBitmapPage()
        }
    }

    get("/tutorial/fields") {
        call.respondHtml {
            renderFieldsPage()
        }
    }

    get("/tutorial/variable-length") {
        call.respondHtml {
            renderVariableLengthPage()
        }
    }

    get("/tutorial/flow") {
        call.respondHtml {
            renderMessageFlowPage()
        }
    }
}
