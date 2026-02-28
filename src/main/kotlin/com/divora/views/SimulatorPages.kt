package com.divora.views

import com.divora.iso8583.*
import kotlinx.html.*

fun HTML.renderSimulatorPage() = commonLayout("ISO8583 Message Simulator", "/simulator") {
    h1 { +"Interactive Message Builder" }

    p {
        +"""
        Build your own ISO8583 authorization request (0200) message. Fill in the fields below
        and click "Generate Message" to see the resulting hex string.
        """
    }

    form(action = "/simulator/generate", method = FormMethod.post) {
        div("form-group") {
            label { +"PAN — Primary Account Number (Field 2) — Optional" }
            input(type = InputType.text, name = "pan") {
                placeholder = "4111111111111111"
                attributes["pattern"] = "\\d{13,19}"
                attributes["autocomplete"] = "off"
            }
            p("field-hint") {
                +"13–19 digit card number. Leave empty to omit this field from the message."
            }
        }

        div("form-group") {
            label { +"Processing Code (Field 3)" }
            input(type = InputType.text, name = "processingCode") {
                value = "000000"
                placeholder = "000000"
                attributes["pattern"] = "\\d{6}"
                attributes["required"] = "required"
            }
            p("field-hint") {
                +"6 digits: Transaction Type (2) + From Account (2) + To Account (2). Example: 000000 = Purchase"
            }
        }

        div("form-group") {
            label { +"Amount (Field 4)" }
            input(type = InputType.text, name = "amount") {
                value = "000000001000"
                placeholder = "000000001000"
                attributes["pattern"] = "\\d{12}"
                attributes["required"] = "required"
            }
            p("field-hint") {
                +"12 digits in cents. Example: 000000001000 = ${'$'}10.00. "
                strong { +"Mock host rules: amounts ending in '00' are approved; amounts over ${'$'}5,000.00 get code 51 (Insufficient Funds)." }
            }
        }

        div("form-group") {
            label { +"STAN — Systems Trace Audit Number (Field 11)" }
            input(type = InputType.text, name = "stan") {
                value = "000001"
                placeholder = "000001"
                attributes["pattern"] = "\\d{6}"
                attributes["required"] = "required"
            }
            p("field-hint") {
                +"6 digits: Unique transaction number, echoed back in the response for matching"
            }
        }

        div("form-group") {
            label { +"Terminal ID (Field 41)" }
            input(type = InputType.text, name = "terminalId") {
                value = "TERM0001"
                placeholder = "TERM0001"
                attributes["maxlength"] = "8"
            }
            p("field-hint") {
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
            p("field-hint") {
                +"Up to 15 characters: Identifies the merchant"
            }
        }

        button(type = ButtonType.submit) {
            +"Generate Message"
        }
    }
}

fun HTML.renderGeneratedMessagePage(message: ISO8583Message, hexString: String) = commonLayout("Generated Message", "/simulator") {
    h1 { +"Generated ISO8583 Message" }

    div("success") {
        h3 { +"Message successfully generated!" }
    }

    h2 { +"Raw Hex String" }
    div("pre-wrapper") {
        button(type = ButtonType.button, classes = "copy-btn") {
            attributes["onclick"] = "copyToClipboard('hex-output', this)"
            +"Copy"
        }
        pre {
            id = "hex-output"
            +hexString
        }
    }

    h2 { +"Message Breakdown" }

    val bitmap = BitmapHelper.fieldsToHexBitmap(message.fields.keys)
    val bitmapBinary = bitmap.map {
        it.toString().toInt(16).toString(2).padStart(4, '0')
    }.joinToString(" ")

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
            td { strong { +"Bitmap (hex)" } }
            td { code { +bitmap } }
            td { +"Indicates fields: ${message.fields.keys.sorted().joinToString(", ")}" }
        }
        tr {
            td { strong { +"Bitmap (binary)" } }
            td {
                style = "font-family: 'Courier New', monospace; font-size: 0.85em; word-break: break-all;"
                +bitmapBinary
            }
            td { +"Each '1' bit = field present; each '0' bit = field absent" }
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
                        2 -> "Primary Account Number (PAN)"
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
        Remember the mock host rules:
        """
    }
    ul {
        li { +"Amounts ending in '00' → Approved (code 00)" }
        li { +"Amounts over ${'$'}5,000.00 → Insufficient Funds (code 51)" }
        li { +"All other amounts → Do Not Honor (code 05)" }
    }

    form(action = "/simulator/send", method = FormMethod.post) {
        input(type = InputType.hidden, name = "hexMessage") {
            value = hexString
        }
        button(type = ButtonType.submit) {
            +"Send to Mock Host →"
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
) = commonLayout("Transaction Response", "/simulator") {
    h1 { +"Transaction Response" }

    if (analysis.approved) {
        div("success") {
            h2 { +"✓ Transaction APPROVED" }
            p { +"Response Code: ${analysis.responseCode} — ${analysis.responseDescription}" }
        }
    } else {
        div("error") {
            h2 { +"✗ Transaction DECLINED" }
            p { +"Response Code: ${analysis.responseCode} — ${analysis.responseDescription}" }
        }
    }

    h2 { +"Transaction Analysis" }

    div("example-box") {
        p {
            strong { +"Amount: " }
            +analysis.getFormattedAmount()
        }
        p {
            strong { +"Decision: " }
            +analysis.reason
        }
    }

    h2 { +"Response Message (0210)" }

    div("pre-wrapper") {
        button(type = ButtonType.button, classes = "copy-btn") {
            attributes["onclick"] = "copyToClipboard('response-hex', this)"
            +"Copy"
        }
        pre {
            id = "response-hex"
            +"MTI: ${responseMessage.mti}\nHex: $responseHex"
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
                        2 -> "Primary Account Number (echoed)"
                        3 -> "Processing Code (echoed)"
                        4 -> "Amount (echoed)"
                        11 -> "STAN (echoed — use this to match response to request)"
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
            li {
                +"Apply decision rules (in priority order):"
                ul {
                    li { +"Amount > \$5,000.00 → Code '51' (Insufficient Funds)" }
                    li { +"Amount ends in '00' → Code '00' (Approved)" }
                    li { +"Otherwise → Code '05' (Do Not Honor)" }
                }
            }
            li { +"Echo back Fields 2, 3, 4, 11, 41, 42" }
            li { +"Set Field 39 (Response Code)" }
            li { +"Return 0210 (Authorization Response)" }
        }
    }

    h2 { +"Try More Transactions" }

    p { +"Want to see different results? Try these amounts:" }

    table {
        tr {
            th { +"Amount Field Value" }
            th { +"Dollar Amount" }
            th { +"Expected Result" }
            th { +"Code" }
        }
        tr {
            td { code { +"000000001000" } }
            td { +"${'$'}10.00" }
            td { strong { style = "color: green;"; +"APPROVED" } }
            td { code { +"00" } }
        }
        tr {
            td { code { +"000000002500" } }
            td { +"${'$'}25.00" }
            td { strong { style = "color: green;"; +"APPROVED" } }
            td { code { +"00" } }
        }
        tr {
            td { code { +"000000001234" } }
            td { +"${'$'}12.34" }
            td { strong { style = "color: red;"; +"DECLINED" } }
            td { code { +"05" } }
        }
        tr {
            td { code { +"000000000099" } }
            td { +"${'$'}0.99" }
            td { strong { style = "color: red;"; +"DECLINED" } }
            td { code { +"05" } }
        }
        tr {
            td { code { +"000000500100" } }
            td { +"${'$'}5,001.00" }
            td { strong { style = "color: red;"; +"DECLINED" } }
            td { code { +"51" } }
        }
    }

    p {
        a(href = "/simulator") { +"← Build Another Message" }
    }
}

fun HTML.renderErrorPage(errorMessage: String) = commonLayout("Error", "/simulator") {
    h1 { +"Error" }

    div("error") {
        p {
            style = "white-space: pre-line;"
            +errorMessage
        }
    }

    p {
        a(href = "/simulator") { +"← Back to Simulator" }
    }
}
