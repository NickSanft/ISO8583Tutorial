package com.divora.iso8583

/**
 * Helper class for ISO8583 Bitmap generation and manipulation.
 * The bitmap indicates which data fields are present in the message.
 * - Primary bitmap (bits 1-64): Always present
 * - Secondary bitmap (bits 65-128): Present if bit 1 of primary bitmap is set
 */
object BitmapHelper {
    /**
     * Converts a set of field numbers to a hex bitmap string.
     * @param fields Set of field numbers (1-based) that are present
     * @return Hex string representing the bitmap (16 or 32 chars for 64/128 bit)
     */
    fun fieldsToHexBitmap(fields: Set<Int>): String {
        val hasSecondaryBitmap = fields.any { it > 64 }
        val bitmapSize = if (hasSecondaryBitmap) 128 else 64
        
        // Create bit array (false = 0, true = 1)
        val bits = BooleanArray(bitmapSize) { false }
        
        // Set bit 1 if we have secondary bitmap
        if (hasSecondaryBitmap) {
            bits[0] = true // Bit 1 (index 0) indicates secondary bitmap present
        }
        
        // Set bits for each field
        fields.forEach { fieldNum ->
            if (fieldNum in 1..bitmapSize) {
                bits[fieldNum - 1] = true // Convert 1-based to 0-based index
            }
        }
        
        // Convert bits to hex string
        return bitsToHex(bits)
    }
    
    /**
     * Converts a hex bitmap string to a set of field numbers.
     * @param hexBitmap Hex string representing the bitmap
     * @return Set of field numbers (1-based) that are present
     */
    fun hexBitmapToFields(hexBitmap: String): Set<Int> {
        val bits = hexToBits(hexBitmap)
        val fields = mutableSetOf<Int>()
        
        bits.forEachIndexed { index, isSet ->
            if (isSet) {
                fields.add(index + 1) // Convert 0-based to 1-based
            }
        }
        
        return fields
    }
    
    /**
     * Converts boolean array to hex string.
     * Each group of 4 bits becomes one hex character.
     */
    private fun bitsToHex(bits: BooleanArray): String {
        val hexChars = StringBuilder()
        
        for (i in bits.indices step 4) {
            var nibble = 0
            for (j in 0..3) {
                if (i + j < bits.size && bits[i + j]) {
                    nibble = nibble or (1 shl (3 - j))
                }
            }
            hexChars.append(nibble.toString(16).uppercase())
        }
        
        return hexChars.toString()
    }
    
    /**
     * Converts hex string to boolean array.
     * Each hex character represents 4 bits.
     */
    private fun hexToBits(hex: String): BooleanArray {
        val bits = BooleanArray(hex.length * 4)
        
        hex.forEachIndexed { charIndex, hexChar ->
            val value = hexChar.toString().toInt(16)
            for (bitPos in 0..3) {
                bits[charIndex * 4 + bitPos] = (value and (1 shl (3 - bitPos))) != 0
            }
        }
        
        return bits
    }
    
    /**
     * Pretty prints a bitmap showing which bits are set.
     * Useful for debugging and educational purposes.
     */
    fun prettyPrintBitmap(hexBitmap: String): String {
        val fields = hexBitmapToFields(hexBitmap)
        val builder = StringBuilder()
        builder.append("Bitmap: $hexBitmap\n")
        builder.append("Binary: ${hexToBinary(hexBitmap)}\n")
        builder.append("Active Fields: ${fields.sorted().joinToString(", ")}\n")
        return builder.toString()
    }
    
    private fun hexToBinary(hex: String): String {
        return hex.map { 
            it.toString().toInt(16).toString(2).padStart(4, '0') 
        }.joinToString(" ")
    }
}
