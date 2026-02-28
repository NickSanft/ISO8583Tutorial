package com.divora.routes

import com.divora.iso8583.*
import com.divora.views.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.simulatorRoutes() {
    get("/simulator") {
        call.respondHtml {
            renderSimulatorPage()
        }
    }

    post("/simulator/generate") {
        val params = call.receiveParameters()

        val pan = params["pan"]?.trim()?.takeIf { it.isNotBlank() }
        val processingCode = params["processingCode"]?.trim() ?: "000000"
        val amount = params["amount"]?.trim() ?: "000000000000"
        val stan = params["stan"]?.trim() ?: "000001"
        val terminalId = params["terminalId"]?.trim() ?: "TERM0001"
        val cardAcceptorId = params["cardAcceptorId"]?.trim() ?: "MERCHANT00001"

        // Server-side validation
        val errors = mutableListOf<String>()
        if (!processingCode.matches(Regex("\\d{6}")))
            errors.add("Processing Code must be exactly 6 digits (e.g. 000000)")
        if (!amount.matches(Regex("\\d{12}")))
            errors.add("Amount must be exactly 12 digits (e.g. 000000001000 for \$10.00)")
        if (!stan.matches(Regex("\\d{6}")))
            errors.add("STAN must be exactly 6 digits (e.g. 000001)")
        if (terminalId.length > 8)
            errors.add("Terminal ID must be at most 8 characters")
        if (cardAcceptorId.length > 15)
            errors.add("Card Acceptor ID must be at most 15 characters")
        if (pan != null && !pan.matches(Regex("\\d{13,19}")))
            errors.add("PAN must be 13–19 digits if provided")

        if (errors.isNotEmpty()) {
            call.respondHtml(HttpStatusCode.BadRequest) {
                renderErrorPage("Validation errors:\n• ${errors.joinToString("\n• ")}")
            }
            return@post
        }

        val message = ISO8583Message("0200")
        pan?.let { message.setField(2, it) }
        message.setField(3, processingCode)
        message.setField(4, amount)
        message.setField(11, stan)
        message.setField(41, terminalId)
        message.setField(42, cardAcceptorId)

        val hexString = message.toHexString()

        call.respondHtml {
            renderGeneratedMessagePage(message, hexString)
        }
    }

    post("/simulator/send") {
        val params = call.receiveParameters()
        val hexMessage = params["hexMessage"] ?: ""

        try {
            val requestMessage = ISO8583Message.fromHexString(hexMessage)
            val responseMessage = MockHostService.processAuthorizationRequest(requestMessage)
            val responseHex = responseMessage.toHexString()
            val analysis = MockHostService.analyzeTransaction(requestMessage, responseMessage)

            call.respondHtml {
                renderResponsePage(requestMessage, responseMessage, responseHex, analysis)
            }
        } catch (e: Exception) {
            call.respondHtml(HttpStatusCode.BadRequest) {
                renderErrorPage("Invalid message format: ${e.message}")
            }
        }
    }
}
