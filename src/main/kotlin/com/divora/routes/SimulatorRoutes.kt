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
        
        val processingCode = params["processingCode"] ?: "000000"
        val amount = params["amount"] ?: "000000000000"
        val stan = params["stan"] ?: "000001"
        val terminalId = params["terminalId"] ?: "TERM0001"
        val cardAcceptorId = params["cardAcceptorId"] ?: "MERCHANT00001"
        
        // Build ISO8583 message
        val message = ISO8583Message("0200")
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
            // Parse the request message
            val requestMessage = ISO8583Message.fromHexString(hexMessage)
            
            // Send to mock host
            val responseMessage = MockHostService.processAuthorizationRequest(requestMessage)
            val responseHex = responseMessage.toHexString()
            
            // Analyze the transaction
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
