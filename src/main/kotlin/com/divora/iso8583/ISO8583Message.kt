package com.divora.iso8583

import org.jpos.iso.ISOMsg
import org.jpos.iso.packager.GenericPackager

/**
 * Represents an ISO8583 message with helper methods for building and parsing.
 */
data class ISO8583Message(
    val mti: String,
    val fields: MutableMap<Int, String> = mutableMapOf()
) {
    /**
     * Sets a field value in the message.
     */
    fun setField(fieldNum: Int, value: String) {
        fields[fieldNum] = value
    }
    
    /**
     * Gets a field value from the message.
     */
    fun getField(fieldNum: Int): String? = fields[fieldNum]
    
    /**
     * Converts the message to a hex string representation.
     * Uses JPOS for proper ISO8583 packing.
     */
    fun toHexString(): String {
        try {
            val packager = createPackager()
            val isoMsg = ISOMsg()
            isoMsg.packager = packager
            isoMsg.mti = mti
            
            // Set all fields
            fields.forEach { (fieldNum, value) ->
                isoMsg.set(fieldNum, value)
            }
            
            // Pack the message
            val packed = isoMsg.pack()
            
            // Convert to hex string
            return bytesToHex(packed)
        } catch (e: Exception) {
            // Fallback to manual construction for demo purposes
            return buildManualHexString()
        }
    }
    
    /**
     * Manual construction of ISO8583 hex string for educational purposes.
     * This shows the structure clearly without needing a full packager configuration.
     */
    private fun buildManualHexString(): String {
        val builder = StringBuilder()
        
        // Add MTI (4 digits)
        builder.append(mti)
        
        // Calculate and add bitmap
        val fieldNumbers = fields.keys.toSet()
        val bitmap = BitmapHelper.fieldsToHexBitmap(fieldNumbers)
        builder.append(bitmap)
        
        // Add fields in order
        fieldNumbers.sorted().forEach { fieldNum ->
            val value = fields[fieldNum] ?: ""
            // For simplicity, we're not handling all field formats here
            // In production, you'd use proper field formatters
            when (fieldNum) {
                3 -> builder.append(value.padStart(6, '0')) // Processing Code
                4 -> builder.append(value.padStart(12, '0')) // Amount
                11 -> builder.append(value.padStart(6, '0')) // STAN
                41 -> builder.append(value.padEnd(8, ' ')) // Card Acceptor Terminal ID
                42 -> builder.append(value.padEnd(15, ' ')) // Card Acceptor ID
                else -> {
                    // For variable length fields, add length prefix
                    if (value.length <= 99) {
                        builder.append(value.length.toString().padStart(2, '0'))
                    }
                    builder.append(value)
                }
            }
        }
        
        return builder.toString()
    }
    
    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02X".format(it) }
    }
    
    /**
     * Creates a basic packager for ISO8583 messages.
     * In production, you'd load this from an XML configuration file.
     */
    private fun createPackager(): GenericPackager {
        // For simplicity, we'll use a basic configuration
        // In a real application, you'd use a proper packager XML file
        val packagerConfig = this::class.java.getResourceAsStream("/packager/basic.xml")
        return if (packagerConfig != null) {
            GenericPackager(packagerConfig)
        } else {
            // Fallback - will trigger manual construction
            throw Exception("Packager not found")
        }
    }
    
    companion object {
        /**
         * Parses a hex string into an ISO8583Message.
         */
        fun fromHexString(hexString: String): ISO8583Message {
            // Extract MTI (first 4 characters)
            val mti = hexString.substring(0, 4)
            
            // Extract bitmap (next 16 or 32 characters)
            val firstBit = hexString[4].toString().toInt(16)
            val hasSecondaryBitmap = (firstBit and 0x8) != 0
            val bitmapLength = if (hasSecondaryBitmap) 32 else 16
            val bitmap = hexString.substring(4, 4 + bitmapLength)
            
            // Parse fields based on bitmap
            val fields = BitmapHelper.hexBitmapToFields(bitmap)
            val message = ISO8583Message(mti)
            
            var position = 4 + bitmapLength
            
            // Parse each field (simplified parsing for demo)
            fields.sorted().forEach { fieldNum ->
                if (fieldNum == 1) return@forEach // Skip secondary bitmap indicator
                
                try {
                    val (value, newPosition) = parseField(hexString, position, fieldNum)
                    message.setField(fieldNum, value)
                    position = newPosition
                } catch (e: Exception) {
                    // Skip unparseable fields
                }
            }
            
            return message
        }
        
        private fun parseField(hexString: String, position: Int, fieldNum: Int): Pair<String, Int> {
            return when (fieldNum) {
                3 -> Pair(hexString.substring(position, position + 6), position + 6)
                4 -> Pair(hexString.substring(position, position + 12), position + 12)
                11 -> Pair(hexString.substring(position, position + 6), position + 6)
                39 -> Pair(hexString.substring(position, position + 2), position + 2)
                41 -> Pair(hexString.substring(position, position + 8).trim(), position + 8)
                42 -> Pair(hexString.substring(position, position + 15).trim(), position + 15)
                else -> {
                    // Variable length field
                    val length = hexString.substring(position, position + 2).toInt()
                    val value = hexString.substring(position + 2, position + 2 + length)
                    Pair(value, position + 2 + length)
                }
            }
        }
    }
}
