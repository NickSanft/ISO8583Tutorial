package com.divora.views

import kotlinx.html.*

fun HTML.renderHomePage() = commonLayout("ISO8583 Tutorial") {
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
}

fun HTML.renderIntroductionPage() = commonLayout("Introduction to ISO8583") {
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
    
    p {
        +"Continue to "
        a(href = "/tutorial/mti") { +"MTI" }
        +" to learn about Message Type Indicators."
    }
}

fun HTML.renderMTIPage() = commonLayout("Message Type Indicator (MTI)") {
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
    
    p {
        +"Next, learn about "
        a(href = "/tutorial/bitmap") { +"Bitmaps" }
        +" to understand how field presence is indicated."
    }
}

fun HTML.renderBitmapPage() = commonLayout("Understanding Bitmaps") {
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
        p { +"Indicates presence of fields 1-64" }
        
        h3 { +"Secondary Bitmap (64 bits)" }
        p { +"If bit 1 of primary bitmap is set, a secondary bitmap follows, indicating fields 65-128" }
    }
    
    h2 { +"Example: Bitmap Breakdown" }
    
    div("bitmap-visual") {
        pre {
            +"""
Hex Bitmap: 7234000102C08000

Converting to Binary (grouped by hex digit):
7    2    3    4    0    0    0    1
0111 0010 0011 0100 0000 0000 0000 0001

Bit positions (1-indexed):
Position:  1  2  3  4  5  6  7  8  9 10 11 12 ...
Bit:       0  1  1  1  0  0  1  0  0  0  1  1 ...

Active Fields: 2, 3, 4, 7, 11, 12, 22, 32

This means the message contains:
  Field 2:  Primary Account Number
  Field 3:  Processing Code
  Field 4:  Amount, Transaction
  Field 7:  Transmission Date & Time
  Field 11: Systems Trace Audit Number
  Field 12: Time, Local Transaction
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
            td { code { +"7" } }
            td { code { +"0111" } }
            td { code { +"F" } }
            td { code { +"1111" } }
        }
    }
    
    p {
        +"Now learn about "
        a(href = "/tutorial/fields") { +"Data Fields" }
        +" and what information they contain."
    }
}

fun HTML.renderFieldsPage() = commonLayout("ISO8583 Data Fields") {
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
            td { +"Credit/debit card number" }
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
            td { +"Unique transaction identifier" }
        }
        tr {
            td { code { +"39" } }
            td { +"Response Code" }
            td { +"an2" }
            td { +"Transaction approval/decline code" }
        }
        tr {
            td { code { +"41" } }
            td { +"Card Acceptor Terminal ID" }
            td { +"ans8" }
            td { +"Terminal identifier" }
        }
        tr {
            td { code { +"42" } }
            td { +"Card Acceptor ID Code" }
            td { +"ans15" }
            td { +"Merchant identifier" }
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
    
    p {
        +"Ready to try it yourself? Head to the "
        a(href = "/simulator") { +"Interactive Simulator" }
        +" to build and send ISO8583 messages!"
    }
}
