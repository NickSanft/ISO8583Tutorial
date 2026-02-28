package com.divora.views

import kotlinx.html.*

// Renders previous/next page navigation at the bottom of a tutorial page.
private fun MAIN.tutorialPageNav(
    prev: Pair<String, String>? = null,
    next: Pair<String, String>? = null
) {
    div("page-nav") {
        if (prev != null) {
            a(href = prev.first) { +"← ${prev.second}" }
        } else {
            span("page-nav-spacer") {}
        }
        if (next != null) {
            a(href = next.first) { +"${next.second} →" }
        } else {
            span("page-nav-spacer") {}
        }
    }
}

fun HTML.renderHomePage() = commonLayout("ISO8583 Tutorial", "/") {
    h1 { +"Welcome to the ISO8583 Tutorial" }

    p {
        +"""
        This interactive tutorial will teach you everything you need to know about ISO8583 messages,
        the international standard for financial transaction card originated messages.
        """
    }

    h2 { +"What You'll Learn" }

    div("example-box") {
        ul {
            li { +"The structure and purpose of ISO8583 messages" }
            li { +"How the Message Type Indicator (MTI) works" }
            li { +"Understanding the bitmap and field presence indicators" }
            li { +"Common data fields and their meanings" }
            li { +"Fixed-length vs variable-length field encoding" }
            li { +"How authorization request/response flows work end-to-end" }
            li { +"Building and parsing real ISO8583 messages" }
        }
    }

    h2 { +"Getting Started" }

    p {
        +"Start with the "
        a(href = "/tutorial/introduction") { +"Introduction" }
        +" to learn the basics, then progress through each section. "
        +"When you're ready, try the "
        a(href = "/simulator") { +"Interactive Simulator" }
        +" to build and send your own messages!"
    }

    tutorialPageNav(prev = null, next = "/tutorial/introduction" to "Introduction")
}

fun HTML.renderIntroductionPage() = commonLayout("Introduction to ISO8583", "/tutorial/introduction") {
    h1 { +"Introduction to ISO8583" }

    p {
        +"""
        ISO8583 is an international standard for financial transaction card originated messages.
        It defines a message format and communication flow for electronic transactions made by
        cardholders using payment cards.
        """
    }

    h2 { +"Why ISO8583?" }

    p {
        +"""
        Banks, payment processors, and merchants worldwide use ISO8583 to exchange transaction
        information. When you swipe your credit card at a store, ISO8583 messages are being
        exchanged behind the scenes to authorize the transaction.
        """
    }

    h2 { +"Message Structure" }

    p { +"An ISO8583 message consists of three main components:" }

    div("example-box") {
        h3 { +"1. Message Type Indicator (MTI)" }
        p { +"A 4-digit code that identifies what type of message this is (e.g., 0200 = Authorization Request)" }

        h3 { +"2. Bitmap" }
        p { +"A binary field that indicates which data fields are present in the message" }

        h3 { +"3. Data Fields" }
        p { +"Up to 128 numbered fields containing transaction information" }
    }

    h2 { +"Example Message" }

    pre {
        +"""
MTI:    0200
Bitmap: 7234000102C08000
Fields:
  [3]  Processing Code: 000000
  [4]  Amount: 000000001000 (${'$'}10.00)
  [11] STAN: 000123
  [41] Terminal ID: TERM0001
        """.trimIndent()
    }

    tutorialPageNav(
        prev = "/" to "Home",
        next = "/tutorial/mti" to "MTI"
    )
}

fun HTML.renderMTIPage() = commonLayout("Message Type Indicator (MTI)", "/tutorial/mti") {
    h1 { +"Message Type Indicator (MTI)" }

    p {
        +"""
        The MTI is a 4-digit numeric field that appears at the start of every ISO8583 message.
        It identifies the message's purpose and characteristics.
        """
    }

    h2 { +"MTI Structure" }

    p { +"Each digit has a specific meaning:" }

    div("example-box") {
        pre {
            +"""
Position 1 - Version:
  0 = ISO 8583:1987
  1 = ISO 8583:1993
  2 = ISO 8583:2003

Position 2 - Message Class:
  1 = Authorization
  2 = Financial
  3 = File Action
  4 = Reversal/Chargeback

Position 3 - Message Function:
  0 = Request
  1 = Request Response
  2 = Advice
  3 = Advice Response

Position 4 - Transaction Origin:
  0 = Acquirer
  1 = Acquirer Repeat
  2 = Issuer
  3 = Issuer Repeat
            """.trimIndent()
        }
    }

    h2 { +"Common MTI Values" }

    table {
        tr {
            th { +"MTI" }
            th { +"Description" }
            th { +"Usage" }
        }
        tr {
            td { code { +"0200" } }
            td { +"Authorization Request" }
            td { +"Merchant requests approval for a transaction" }
        }
        tr {
            td { code { +"0210" } }
            td { +"Authorization Response" }
            td { +"Host responds to authorization request" }
        }
        tr {
            td { code { +"0400" } }
            td { +"Reversal Request" }
            td { +"Cancel a previous transaction" }
        }
        tr {
            td { code { +"0800" } }
            td { +"Network Management" }
            td { +"Sign-on, echo test, key exchange" }
        }
    }

    h2 { +"Example: Breaking Down MTI 0200" }

    div("bitmap-visual") {
        pre {
            +"""
MTI: 0200
     ││││
     │││└─ Origin: 0 (Acquirer)
     ││└── Function: 0 (Request)
     │└─── Class: 2 (Financial)
     └──── Version: 0 (ISO 8583:1987)

Result: Financial Request from Acquirer
        (i.e., Authorization Request)
            """.trimIndent()
        }
    }

    tutorialPageNav(
        prev = "/tutorial/introduction" to "Introduction",
        next = "/tutorial/bitmap" to "Bitmap"
    )
}

fun HTML.renderBitmapPage() = commonLayout("Understanding Bitmaps", "/tutorial/bitmap") {
    h1 { +"Understanding the Bitmap" }

    p {
        +"""
        The bitmap is one of the most important parts of an ISO8583 message. It's a compact way
        of indicating which data fields are present in the message without having to list them all.
        """
    }

    h2 { +"How Bitmaps Work" }

    p {
        +"""
        Think of the bitmap as a checklist. Each bit position corresponds to a field number:
        if the bit is 1, that field is present; if it's 0, the field is absent.
        """
    }

    div("example-box") {
        h3 { +"Primary Bitmap (64 bits)" }
        p { +"Indicates presence of fields 1–64. Bit 1 (the very first bit) is special: if set, it means a secondary bitmap follows." }

        h3 { +"Secondary Bitmap (64 bits)" }
        p { +"If bit 1 of the primary bitmap is set, a secondary bitmap follows, indicating fields 65–128." }
    }

    h2 { +"Example: Bitmap Breakdown" }

    div("bitmap-visual") {
        pre {
            +"""
Hex Bitmap: 7234000102C08000

Converting to Binary (each hex digit = 4 bits):
7    2    3    4    0    0    0    1    0    2    C    0    8    0    0    0
0111 0010 0011 0100 0000 0000 0000 0001 0000 0010 1100 0000 1000 0000 0000 0000

Bit positions (1-indexed):
 1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 ...
 0  1  1  1  0  0  1  0  0  0  1  1  0  0  1  0 ...

Active Fields: 2, 3, 4, 7, 11, 12, 15, 22, 32

This means the message contains:
  Field 2:  Primary Account Number
  Field 3:  Processing Code
  Field 4:  Amount, Transaction
  Field 7:  Transmission Date & Time
  Field 11: Systems Trace Audit Number
  Field 12: Time, Local Transaction
  Field 15: Date, Settlement
  Field 22: Point of Service Entry Mode
  Field 32: Acquiring Institution ID
            """.trimIndent()
        }
    }

    h2 { +"Hex to Binary Conversion" }

    p { +"Each hex digit represents 4 bits:" }

    table {
        tr {
            th { +"Hex" }
            th { +"Binary" }
            th { +"Hex" }
            th { +"Binary" }
        }
        tr {
            td { code { +"0" } }
            td { code { +"0000" } }
            td { code { +"8" } }
            td { code { +"1000" } }
        }
        tr {
            td { code { +"1" } }
            td { code { +"0001" } }
            td { code { +"9" } }
            td { code { +"1001" } }
        }
        tr {
            td { code { +"2" } }
            td { code { +"0010" } }
            td { code { +"A" } }
            td { code { +"1010" } }
        }
        tr {
            td { code { +"3" } }
            td { code { +"0011" } }
            td { code { +"B" } }
            td { code { +"1011" } }
        }
        tr {
            td { code { +"4" } }
            td { code { +"0100" } }
            td { code { +"C" } }
            td { code { +"1100" } }
        }
        tr {
            td { code { +"5" } }
            td { code { +"0101" } }
            td { code { +"D" } }
            td { code { +"1101" } }
        }
        tr {
            td { code { +"6" } }
            td { code { +"0110" } }
            td { code { +"E" } }
            td { code { +"1110" } }
        }
        tr {
            td { code { +"7" } }
            td { code { +"0111" } }
            td { code { +"F" } }
            td { code { +"1111" } }
        }
    }

    tutorialPageNav(
        prev = "/tutorial/mti" to "MTI",
        next = "/tutorial/fields" to "Fields"
    )
}

fun HTML.renderFieldsPage() = commonLayout("ISO8583 Data Fields", "/tutorial/fields") {
    h1 { +"ISO8583 Data Fields" }

    p {
        +"""
        ISO8583 defines up to 128 data fields, each with a specific purpose and format.
        Here are the most commonly used fields in financial transactions.
        """
    }

    h2 { +"Common Fields" }

    table {
        tr {
            th { +"Field" }
            th { +"Name" }
            th { +"Format" }
            th { +"Description" }
        }
        tr {
            td { code { +"2" } }
            td { +"Primary Account Number (PAN)" }
            td { +"n..19" }
            td { +"Credit/debit card number (variable length)" }
        }
        tr {
            td { code { +"3" } }
            td { +"Processing Code" }
            td { +"n6" }
            td { +"Transaction type and account types" }
        }
        tr {
            td { code { +"4" } }
            td { +"Amount, Transaction" }
            td { +"n12" }
            td { +"Transaction amount in cents/minor units" }
        }
        tr {
            td { code { +"11" } }
            td { +"Systems Trace Audit Number (STAN)" }
            td { +"n6" }
            td { +"Unique transaction identifier, echoed in response" }
        }
        tr {
            td { code { +"39" } }
            td { +"Response Code" }
            td { +"an2" }
            td { +"Transaction approval/decline code (response only)" }
        }
        tr {
            td { code { +"41" } }
            td { +"Card Acceptor Terminal ID" }
            td { +"ans8" }
            td { +"Terminal identifier (fixed 8 chars)" }
        }
        tr {
            td { code { +"42" } }
            td { +"Card Acceptor ID Code" }
            td { +"ans15" }
            td { +"Merchant identifier (fixed 15 chars)" }
        }
    }

    h2 { +"Field Formats" }

    div("example-box") {
        h3 { +"Format Notation" }
        ul {
            li {
                code { +"n" }
                +" = Numeric only"
            }
            li {
                code { +"an" }
                +" = Alphanumeric"
            }
            li {
                code { +"ans" }
                +" = Alphanumeric and special characters"
            }
            li {
                code { +"..19" }
                +" = Variable length up to 19 characters"
            }
            li {
                code { +"6" }
                +" = Fixed length of 6 characters"
            }
        }
    }

    h2 { +"Field 3: Processing Code" }

    p { +"The processing code is a 6-digit field structured as:" }

    div("bitmap-visual") {
        pre {
            +"""
Format: TTFFTT

TT = Transaction Type
  00 = Purchase
  01 = Cash Withdrawal
  20 = Refund
  30 = Balance Inquiry

FF = From Account Type
TT = To Account Type
  00 = Default/Not Specified
  10 = Savings
  20 = Checking
  30 = Credit

Example: 000000
  00 = Purchase
  00 = From Default Account
  00 = To Default Account
            """.trimIndent()
        }
    }

    h2 { +"Field 4: Amount" }

    p {
        +"""
        Always 12 digits, right-justified, zero-filled. Represents the amount in the smallest
        currency unit (cents for USD, pence for GBP, etc.).
        """
    }

    div("example-box") {
        pre {
            +"""
${'$'}10.00     → 000000001000
${'$'}1,234.56  → 000000123456
${'$'}0.99      → 000000000099
            """.trimIndent()
        }
    }

    h2 { +"Field 39: Response Code" }

    p { +"Two-character code indicating transaction result:" }

    table {
        tr {
            th { +"Code" }
            th { +"Description" }
        }
        tr {
            td { code { +"00" } }
            td { +"Approved" }
        }
        tr {
            td { code { +"05" } }
            td { +"Do Not Honor" }
        }
        tr {
            td { code { +"51" } }
            td { +"Insufficient Funds" }
        }
        tr {
            td { code { +"54" } }
            td { +"Expired Card" }
        }
        tr {
            td { code { +"91" } }
            td { +"Issuer or Switch Inoperative" }
        }
    }

    tutorialPageNav(
        prev = "/tutorial/bitmap" to "Bitmap",
        next = "/tutorial/variable-length" to "Field Formats"
    )
}

fun HTML.renderVariableLengthPage() = commonLayout("Variable-Length Fields", "/tutorial/variable-length") {
    h1 { +"Variable-Length Fields" }

    p {
        +"""
        Not all ISO8583 fields are the same size. Some are fixed-length — they always occupy
        exactly the same number of characters. Others are variable-length — their size depends
        on the actual data. Understanding this distinction is essential for parsing and building
        correct messages.
        """
    }

    h2 { +"Fixed-Length Fields" }

    p {
        +"""
        Fixed-length fields always occupy the same number of bytes in the message, regardless
        of the actual data. If the value is shorter than the required length, it is padded:
        """
    }

    div("example-box") {
        ul {
            li {
                strong { +"Numeric fields" }
                +" are zero-padded on the left: "
                code { +"${'$'}10.00 → \"000000001000\"" }
                +" (Field 4, n12)"
            }
            li {
                strong { +"Alphanumeric fields" }
                +" are space-padded on the right: "
                code { +"\"TERM1\" → \"TERM1   \"" }
                +" (Field 41, ans8)"
            }
        }
    }

    table {
        tr {
            th { +"Field" }
            th { +"Format" }
            th { +"Fixed Length" }
            th { +"Example" }
        }
        tr {
            td { code { +"3" } }
            td { +"n6" }
            td { +"6 digits" }
            td { code { +"000000" } }
        }
        tr {
            td { code { +"4" } }
            td { +"n12" }
            td { +"12 digits" }
            td { code { +"000000001000" } }
        }
        tr {
            td { code { +"11" } }
            td { +"n6" }
            td { +"6 digits" }
            td { code { +"000001" } }
        }
        tr {
            td { code { +"39" } }
            td { +"an2" }
            td { +"2 chars" }
            td { code { +"00" } }
        }
        tr {
            td { code { +"41" } }
            td { +"ans8" }
            td { +"8 chars" }
            td { code { +"TERM0001" } }
        }
        tr {
            td { code { +"42" } }
            td { +"ans15" }
            td { +"15 chars" }
            td { code { +"MERCHANT00001  " } }
        }
    }

    h2 { +"Variable-Length Fields (LLVAR)" }

    p {
        +"""
        For fields where data length can vary significantly — like a card number (PAN) — it would
        waste space to always allocate the maximum. ISO8583 solves this with variable-length
        encoding called LLVAR.
        """
    }

    div("example-box") {
        h3 { +"LLVAR Format" }
        p {
            +"LLVAR = "
            strong { +"LL" }
            +" (2-digit length prefix) + "
            strong { +"VAR" }
            +" (variable data)"
        }
        p {
            +"The first 2 characters state how many characters of data follow. "
            +"Maximum length: 99 characters."
        }
    }

    h2 { +"Example: Field 2 (PAN)" }

    p {
        +"Field 2 is defined as "
        code { +"n..19" }
        +" — numeric, up to 19 digits. It uses LLVAR encoding:"
    }

    div("bitmap-visual") {
        pre {
            +"""
16-digit PAN: 4111111111111111
  Length prefix: "16"
  Data:          "4111111111111111"
  Encoded:       "164111111111111111"

13-digit PAN: 4111111111111
  Length prefix: "13"
  Data:          "4111111111111"
  Encoded:       "134111111111111"

19-digit PAN: 4111111111111111111
  Length prefix: "19"
  Data:          "4111111111111111111"
  Encoded:       "194111111111111111111"
            """.trimIndent()
        }
    }

    h2 { +"LLLVAR Fields" }

    p {
        +"""
        Some fields use a 3-digit length prefix (LLLVAR), allowing up to 999 characters.
        These are typically used for extended data fields like Field 120 (Private Data).
        """
    }

    div("example-box") {
        pre {
            +"""
LLLVAR example (Field 120, 42 characters of data):
  Length prefix: "042"
  Data:          "some extended private data here..."
  Encoded:       "042some extended private data here..."
            """.trimIndent()
        }
    }

    h2 { +"Why This Matters for Parsing" }

    p {
        +"""
        When a parser reads an ISO8583 message, it processes fields strictly in order
        (lowest field number first). For each field, it must know from the spec whether
        the field is fixed-length or variable-length in order to know where it ends and
        the next field begins.
        """
    }

    div("example-box") {
        h3 { +"Parsing Steps" }
        ol {
            li { +"Read the MTI (first 4 characters)" }
            li { +"Read the bitmap (next 16 or 32 characters)" }
            li { +"Determine which field numbers are present from the bitmap" }
            li { +"Process each present field in ascending order:" }
        }
        ul {
            style = "margin-left: 30px; margin-top: 10px;"
            li { +"Fixed-length: read exactly N characters" }
            li { +"LLVAR: read 2 chars → convert to number N → read N chars" }
            li { +"LLLVAR: read 3 chars → convert to number N → read N chars" }
        }
        p {
            style = "margin-top: 10px;"
            +"This is why field type definitions are essential — without knowing a field's "
            +"encoding type, you cannot correctly locate the next field in the byte stream."
        }
    }

    tutorialPageNav(
        prev = "/tutorial/fields" to "Fields",
        next = "/tutorial/flow" to "Message Flow"
    )
}

fun HTML.renderMessageFlowPage() = commonLayout("Message Flow", "/tutorial/flow") {
    h1 { +"Message Flow" }

    p {
        +"""
        Understanding how ISO8583 messages flow between systems is just as important as
        understanding their structure. Let's trace a card payment from the point of sale
        all the way to the issuing bank and back.
        """
    }

    h2 { +"End-to-End Flow" }

    div("bitmap-visual") {
        pre {
            +"""
[Customer swipes card]
         │
         ▼
[POS Terminal]  ──── builds 0200 Authorization Request ────►  [Acquirer]
                                                                    │
                                                                    ▼
                                                              [Card Network]
                                                            (Visa / Mastercard)
                                                                    │
                                                                    ▼
                                                              [Card Issuer]
                                                           (customer's bank)
                                                                    │
                                                               approves or
                                                                 declines
                                                                    │
[POS Terminal]  ◄─── receives 0210 Authorization Response ─────────┘
         │
         ▼
[Receipt printed / Transaction complete]
            """.trimIndent()
        }
    }

    h2 { +"What Our Simulator Does" }

    p {
        +"""
        Our simulator simplifies this flow. You act as the POS terminal, building a 0200 message.
        The mock host simulates the entire backend — acquirer, network, and issuer — and immediately
        returns a 0210 response.
        """
    }

    div("example-box") {
        pre {
            +"""
[You (via Simulator)]  ──── 0200 ────►  [Mock Host]
                       ◄─── 0210 ────
            """.trimIndent()
        }
    }

    h2 { +"Step 1: The 0200 Authorization Request" }

    p { +"The terminal builds a 0200 message containing transaction details:" }

    table {
        tr {
            th { +"Field" }
            th { +"Name" }
            th { +"Example Value" }
            th { +"Notes" }
        }
        tr {
            td { code { +"MTI" } }
            td { +"Message Type Indicator" }
            td { code { +"0200" } }
            td { +"Financial Authorization Request" }
        }
        tr {
            td { code { +"2" } }
            td { +"Primary Account Number" }
            td { code { +"4111111111111111" } }
            td { +"Card number (optional in our simulator)" }
        }
        tr {
            td { code { +"3" } }
            td { +"Processing Code" }
            td { code { +"000000" } }
            td { +"Purchase, default accounts" }
        }
        tr {
            td { code { +"4" } }
            td { +"Amount" }
            td { code { +"000000001000" } }
            td { +"${'$'}10.00 in cents" }
        }
        tr {
            td { code { +"11" } }
            td { +"STAN" }
            td { code { +"000001" } }
            td { +"Unique ID for matching to response" }
        }
        tr {
            td { code { +"41" } }
            td { +"Terminal ID" }
            td { code { +"TERM0001" } }
            td { +"Identifies which terminal sent the request" }
        }
        tr {
            td { code { +"42" } }
            td { +"Merchant ID" }
            td { code { +"MERCHANT00001  " } }
            td { +"Identifies the merchant" }
        }
    }

    h2 { +"Step 2: The Mock Host Processes the Request" }

    div("example-box") {
        ol {
            li { +"Validates the MTI is \"0200\"" }
            li { +"Extracts Field 4 (Amount)" }
            li {
                +"Applies approval logic:"
                ul {
                    li { +"Amount > \$5,000.00 → Response Code \"51\" (Insufficient Funds)" }
                    li { +"Amount ends in \"00\" → Response Code \"00\" (Approved)" }
                    li { +"Otherwise → Response Code \"05\" (Do Not Honor)" }
                }
            }
            li { +"Echoes back Fields 2, 3, 4, 11, 41, 42 from the request" }
            li { +"Adds Field 39 (Response Code)" }
            li { +"Returns a 0210 (Authorization Response)" }
        }
    }

    h2 { +"Step 3: The 0210 Authorization Response" }

    p {
        +"""
        The response MTI 0210 is derived from the request 0200 by incrementing the
        Message Function digit from 0 (Request) to 1 (Response). The class (2) and
        version (0) stay the same.
        """
    }

    div("bitmap-visual") {
        pre {
            +"""
Request:  0200  →  Response: 0210
          ││││                ││││
          │││└── Origin:   0 (Acquirer)
          ││└─── Function: 0 (Request)  →  1 (Response)
          │└──── Class:    2 (Financial)
          └───── Version:  0 (ISO 8583:1987)
            """.trimIndent()
        }
    }

    h2 { +"Field Echoing and STAN Matching" }

    p {
        +"""
        In real payment systems, many transactions can be in-flight simultaneously.
        To match each response to its request, the STAN (Field 11) is echoed back
        unchanged in the response.
        """
    }

    div("example-box") {
        h3 { +"Why STAN Matters" }
        p {
            +"Imagine a busy terminal sending 10 transactions per second. When responses "
            +"come back (possibly out of order), the terminal checks Field 11 to match "
            +"each 0210 response to the correct 0200 request."
        }
        pre {
            +"""
Terminal sends:  0200 STAN=000001 Amount=${'$'}10.00
Terminal sends:  0200 STAN=000002 Amount=${'$'}25.00
Terminal sends:  0200 STAN=000003 Amount=${'$'}5.99

Host responds:   0210 STAN=000002 RC=00 (Approved)  ← matches request 000002
Host responds:   0210 STAN=000001 RC=00 (Approved)  ← matches request 000001
Host responds:   0210 STAN=000003 RC=05 (Declined)  ← matches request 000003
            """.trimIndent()
        }
    }

    h2 { +"Try It Yourself" }

    p {
        +"Now that you understand the full message flow, head to the "
        a(href = "/simulator") { +"Interactive Simulator" }
        +" to build and send your own 0200 messages and see the 0210 responses!"
    }

    tutorialPageNav(
        prev = "/tutorial/variable-length" to "Field Formats",
        next = "/simulator" to "Simulator"
    )
}
