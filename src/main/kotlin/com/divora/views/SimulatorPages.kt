package com.divora.views

import com.divora.iso8583.*
import kotlinx.html.*

fun HTML.renderSimulatorPage() = commonLayout("ISO8583 Message Simulator") {
    h1 { +"Interactive Message Builder" }
    
    p {
        +"""
        Build your own ISO8583 authorization request (0200) message. Fill in the fields below
        and click "Generate Message" to see the resulting hex string.
        """
    }
    
    form(action = "/simulator/generate", method = FormMethod.post) {
        div("form-group") {
            label { +"Processing Code (Field 3)" }
            input(type = InputType.text, name = "processingCode") {
                value = "000000"
                placeholder = "000000"
                attributes["pattern"] = "\\d{6}"
            }
            p { 
                style = "font-size: 0.9em; color: #666; margin-top: 5px;"
                +"6 digits: Transaction Type (2) + From Account (2) + To Account (2). Example: 000000 = Purchase"
            }
        }
        
        div("form-group") {
            label { +"Amount (Field 4)" }
            input(type = InputType.text, name = "amount") {
                value = "000000001000"
                placeholder = "000000001000"
                attributes["pattern"] = "\\d{12}"
            }
            p { 
                style = "font-size: 0.9em; color: #666; margin-top: 5px;"
                +"12 digits in cents. Example: 000000001000 = ${'$'}10.00. "
                strong { +"Amounts ending in '00' will be approved!" }
            }
        }
        
        div("form-group") {
            label { +"STAN - Systems Trace Audit Number (Field 11)" }
            input(type = InputType.text, name = "stan") {
                value = "000001"
                placeholder = "000001"
                attributes["pattern"] = "\\d{6}"
            }
            p { 
                style = "font-size: 0.9em; color: #666; margin-top: 5px;"
                +"6 digits: Unique transaction number for tracking"
            }
        }
        
        div("form-group") {
            label { +"Terminal ID (Field 41)" }
            input(type = InputType.text, name = "terminalId") {
                value = "TERM0001"
                placeholder = "TERM0001"
                attributes["maxlength"] = "8"
            }
            p { 
                style = "font-size: 0.9em; color: #666; margin-top: 5px;"
                +"Up to 8 characters: Identifies the terminal/POS device"
            }
        }
        
        div("form-group") {
            label { +"Card Acceptor ID (Field 42)" }
            input(type = InputType.text, name = "cardAcceptorId") {
                value = "MERCHANT00001"
                placeholder = "MERCHANT00001"
                attributes["maxlength"] = "15"
            }
            p { 
                style = "font-size: 0.9em; color: #666; margin-top: 5px;"
                +"Up to 15 characters: Identifies the merchant"
            }
        }
        
        button(type = ButtonType.submit) {
            +"Generate Message"
        }
    }
}

fun HTML.renderGeneratedMessagePage(message: ISO8583Message, hexString: String) = commonLayout("Generated Message") {
    h1 { +"Generated ISO8583 Message" }
    
    div("success") {
        h3 { +"Message successfully generated!" }
    }
    
    h2 { +"Raw Hex String" }
    div("bitmap-visual") {
        pre {
            +hexString
        }
    }
    
    h2 { +"Message Breakdown" }
    
    table {
        tr {
            th { +"Component" }
            th { +"Value" }
            th { +"Description" }
        }
        tr {
            td { strong { +"MTI" } }
            td { code { +message.mti } }
            td { +"Authorization Request" }
        }
        tr {
            td { strong { +"Bitmap" } }
            td { 
                code { 
                    val bitmap = BitmapHelper.fieldsToHexBitmap(message.fields.keys)
                    +bitmap
                }
            }
            td { +"Indicates fields: ${message.fields.keys.sorted().joinToString(", ")}" }
        }
    }
    
    h2 { +"Data Fields" }
    
    table {
        tr {
            th { +"Field" }
            th { +"Value" }
            th { +"Meaning" }
        }
        message.fields.toSortedMap().forEach { (fieldNum, value) ->
            tr {
                td { code { +"Field $fieldNum" } }
                td { code { +value } }
                td {
                    +when (fieldNum) {
                        3 -> "Processing Code"
                        4 -> {
                            val amount = value.toLongOrNull() ?: 0
                            val dollars = amount / 100
                            val cents = amount % 100
                            "Amount: ${'$'}$dollars.${cents.toString().padStart(2, '0')}"
                        }
                        11 -> "STAN (Transaction ID)"
                        41 -> "Terminal ID"
                        42 -> "Merchant ID"
                        else -> "Field $fieldNum"
                    }
                }
            }
        }
    }
    
    h2 { +"Send to Mock Host" }
    
    p {
        +"""
        Now send this message to our mock payment processor to see how it responds.
        Remember: amounts ending in '00' will be approved, others will be declined!
        """
    }
    
    form(action = "/simulator/send", method = FormMethod.post) {
        input(type = InputType.hidden, name = "hexMessage") {
            value = hexString
        }
        button(type = ButtonType.submit) {
            +"Send to Mock Host"
        }
    }
    
    p {
        a(href = "/simulator") { +"← Build Another Message" }
    }
}

fun HTML.renderResponsePage(
    requestMessage: ISO8583Message,
    responseMessage: ISO8583Message,
    responseHex: String,
    analysis: TransactionAnalysis
) = commonLayout("Transaction Response") {
    h1 { +"Transaction Response" }
    
    if (analysis.approved) {
        div("success") {
            h2 { +"✓ Transaction APPROVED" }
            p { +"Response Code: ${analysis.responseCode} - ${analysis.responseDescription}" }
        }
    } else {
        div("error") {
            h2 { +"✗ Transaction DECLINED" }
            p { +"Response Code: ${analysis.responseCode} - ${analysis.responseDescription}" }
        }
    }
    
    h2 { +"Transaction Analysis" }
    
    div("example-box") {
        p {
            strong { +"Amount: " }
            +analysis.getFormattedAmount()
        }
        p {
            strong { +"Result: " }
            +analysis.reason
        }
        p { 
            +"""
            The mock host analyzed your transaction amount. Since it ends in 
            '${requestMessage.getField(4)?.takeLast(2)}', the transaction was 
            ${if (analysis.approved) "APPROVED" else "DECLINED"}.
            """
        }
    }
    
    h2 { +"Response Message (0210)" }
    
    div("bitmap-visual") {
        pre {
            +"MTI: ${responseMessage.mti}\n"
            +"Hex: $responseHex"
        }
    }
    
    h2 { +"Response Fields" }
    
    table {
        tr {
            th { +"Field" }
            th { +"Value" }
            th { +"Description" }
        }
        responseMessage.fields.toSortedMap().forEach { (fieldNum, value) ->
            tr {
                td { code { +"Field $fieldNum" } }
                td { 
                    if (fieldNum == 39) {
                        strong { 
                            style = if (analysis.approved) "color: green;" else "color: red;"
                            +value 
                        }
                    } else {
                        code { +value }
                    }
                }
                td {
                    +when (fieldNum) {
                        3 -> "Processing Code (echoed)"
                        4 -> "Amount (echoed)"
                        11 -> "STAN (echoed)"
                        39 -> analysis.responseDescription
                        41 -> "Terminal ID (echoed)"
                        42 -> "Merchant ID (echoed)"
                        else -> "Field $fieldNum"
                    }
                }
            }
        }
    }
    
    h2 { +"How It Works" }
    
    div("example-box") {
        h3 { +"Mock Host Logic:" }
        ol {
            li { +"Receive 0200 (Authorization Request) message" }
            li { +"Extract Field 4 (Amount)" }
            li { +"Check if amount ends in '00'" }
            li { 
                +"Set Field 39 (Response Code):"
                ul {
                    li { +"'00' if ends in '00' → APPROVED" }
                    li { +"'05' otherwise → DECLINED" }
                }
            }
            li { +"Echo back key fields (3, 4, 11, 41, 42)" }
            li { +"Return 0210 (Authorization Response)" }
        }
    }
    
    h2 { +"Try More Transactions" }
    
    p {
        +"Want to see different results? Try these amounts:"
    }
    
    ul {
        li {
            code { +"000000001000" }
            +" (${'$'}10.00) → Will be "
            strong { +"APPROVED" }
        }
        li {
            code { +"000000002500" }
            +" (${'$'}25.00) → Will be "
            strong { +"APPROVED" }
        }
        li {
            code { +"000000001234" }
            +" (${'$'}12.34) → Will be "
            strong { +"DECLINED" }
        }
        li {
            code { +"000000000099" }
            +" (${'$'}0.99) → Will be "
            strong { +"DECLINED" }
        }
    }
    
    p {
        a(href = "/simulator") { +"← Build Another Message" }
    }
}

fun HTML.renderErrorPage(errorMessage: String) = commonLayout("Error") {
    h1 { +"Error" }
    
    div("error") {
        p { +errorMessage }
    }
    
    p {
        a(href = "/simulator") { +"← Back to Simulator" }
    }
}
