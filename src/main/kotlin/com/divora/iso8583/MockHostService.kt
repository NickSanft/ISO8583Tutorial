package com.divora.iso8583

/**
 * Mock host service that simulates a payment processor.
 * Implements simple approval/decline logic based on transaction amount.
 */
object MockHostService {
    /**
     * Processes a 0200 (Authorization Request) and returns a 0210 (Authorization Response).
     *
     * Business Logic (in priority order):
     * - Amount > $5,000.00 (500000 cents): Declined - Insufficient Funds (Response Code "51")
     * - Amount ends in "00" (e.g., $10.00, $25.00): Approved (Response Code "00")
     * - Otherwise: Declined - Do Not Honor (Response Code "05")
     */
    fun processAuthorizationRequest(requestMessage: ISO8583Message): ISO8583Message {
        require(requestMessage.mti == "0200") { "Expected MTI 0200, got ${requestMessage.mti}" }

        val amount = requestMessage.getField(4) ?: "000000000000"
        val amountVal = amount.toLongOrNull() ?: 0L

        val responseCode = when {
            amountVal > 500000L -> "51"              // > $5,000.00: Insufficient Funds
            amount.takeLast(2) == "00" -> "00"       // Round dollar amounts: Approved
            else -> "05"                              // Other: Do Not Honor
        }

        val responseMessage = ISO8583Message("0210")

        // Echo back certain fields from request
        requestMessage.getField(2)?.let { responseMessage.setField(2, it) }  // PAN
        requestMessage.getField(3)?.let { responseMessage.setField(3, it) }  // Processing Code
        requestMessage.getField(4)?.let { responseMessage.setField(4, it) }  // Amount
        requestMessage.getField(11)?.let { responseMessage.setField(11, it) } // STAN
        requestMessage.getField(41)?.let { responseMessage.setField(41, it) } // Terminal ID
        requestMessage.getField(42)?.let { responseMessage.setField(42, it) } // Card Acceptor ID

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
            "51" -> "Insufficient Funds - Amount exceeds limit"
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
        val amountVal = amount.toLongOrNull() ?: 0L
        val amountEnding = amount.takeLast(2)

        val reason = when {
            amountVal > 500000L ->
                "Amount exceeds \$5,000.00 — mock host high-amount decline rule triggered"
            responseCode == "00" ->
                "Amount ends in '00' (round dollar amount) — mock host approval rule triggered"
            else ->
                "Amount ends in '$amountEnding' (not '00') — mock host decline rule triggered"
        }

        return TransactionAnalysis(
            approved = responseCode == "00",
            responseCode = responseCode,
            responseDescription = getResponseCodeDescription(responseCode),
            reason = reason,
            amountInCents = amountVal
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
