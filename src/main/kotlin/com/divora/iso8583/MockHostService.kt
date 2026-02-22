package com.divora.iso8583

/**
 * Mock host service that simulates a payment processor.
 * Implements simple approval/decline logic based on transaction amount.
 */
object MockHostService {
    /**
     * Processes a 0200 (Authorization Request) and returns a 0210 (Authorization Response).
     * 
     * Business Logic:
     * - If amount ends in "00" (e.g., $10.00, $25.00): Approved (Response Code 39 = "00")
     * - Otherwise: Declined (Response Code 39 = "05" - Do Not Honor)
     */
    fun processAuthorizationRequest(requestMessage: ISO8583Message): ISO8583Message {
        // Validate this is a 0200 message
        require(requestMessage.mti == "0200") { "Expected MTI 0200, got ${requestMessage.mti}" }
        
        // Extract amount from Field 4
        val amount = requestMessage.getField(4) ?: "000000000000"
        
        // Determine approval based on amount ending
        val isApproved = amount.takeLast(2) == "00"
        val responseCode = if (isApproved) "00" else "05"
        
        // Build 0210 response message
        val responseMessage = ISO8583Message("0210")
        
        // Echo back certain fields from request
        requestMessage.getField(3)?.let { responseMessage.setField(3, it) } // Processing Code
        requestMessage.getField(4)?.let { responseMessage.setField(4, it) } // Amount
        requestMessage.getField(11)?.let { responseMessage.setField(11, it) } // STAN
        requestMessage.getField(41)?.let { responseMessage.setField(41, it) } // Terminal ID
        requestMessage.getField(42)?.let { responseMessage.setField(42, it) } // Card Acceptor ID
        
        // Add response code (Field 39)
        responseMessage.setField(39, responseCode)
        
        return responseMessage
    }
    
    /**
     * Returns human-readable explanation of the response code.
     */
    fun getResponseCodeDescription(code: String): String {
        return when (code) {
            "00" -> "Approved - Transaction successful"
            "05" -> "Do Not Honor - Transaction declined"
            "51" -> "Insufficient Funds"
            "54" -> "Expired Card"
            "57" -> "Transaction Not Permitted"
            "91" -> "Issuer or Switch Inoperative"
            else -> "Unknown Response Code"
        }
    }
    
    /**
     * Generates a detailed analysis of why a transaction was approved or declined.
     */
    fun analyzeTransaction(requestMessage: ISO8583Message, responseMessage: ISO8583Message): TransactionAnalysis {
        val amount = requestMessage.getField(4) ?: "000000000000"
        val responseCode = responseMessage.getField(39) ?: "99"
        
        val amountEnding = amount.takeLast(2)
        val reason = if (amountEnding == "00") {
            "Amount ends in '00' - Mock host approval rule triggered"
        } else {
            "Amount ends in '$amountEnding' (not '00') - Mock host decline rule triggered"
        }
        
        return TransactionAnalysis(
            approved = responseCode == "00",
            responseCode = responseCode,
            responseDescription = getResponseCodeDescription(responseCode),
            reason = reason,
            amountInCents = amount.toLongOrNull() ?: 0L
        )
    }
}

/**
 * Data class representing transaction analysis results.
 */
data class TransactionAnalysis(
    val approved: Boolean,
    val responseCode: String,
    val responseDescription: String,
    val reason: String,
    val amountInCents: Long
) {
    fun getFormattedAmount(): String {
        val dollars = amountInCents / 100
        val cents = amountInCents % 100
        return "$${dollars}.${cents.toString().padStart(2, '0')}"
    }
}
