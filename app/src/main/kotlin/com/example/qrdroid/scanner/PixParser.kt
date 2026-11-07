package com.example.qrdroid.scanner

import java.util.Locale

data class PixData(
    val pixKey: String? = null,
    val merchantName: String? = null,
    val merchantCity: String? = null,
    val amount: Double? = null,
    val txId: String? = null,
    val description: String? = null
)

object PixParser {

    /**
     * Parses an EMVCo BR Code (Pix Copy & Paste / QR Code string)
     */
    fun parse(raw: String): PixData {
        if (!raw.startsWith("000201")) {
            return PixData()
        }

        var index = 0
        var pixKey: String? = null
        var description: String? = null
        var merchantName: String? = null
        var merchantCity: String? = null
        var amount: Double? = null
        var txId: String? = null

        while (index + 4 <= raw.length) {
            val tag = raw.substring(index, index + 2)
            val lenStr = raw.substring(index + 2, index + 4)
            val len = lenStr.toIntOrNull() ?: break
            val valStart = index + 4
            val valEnd = valStart + len

            if (valEnd > raw.length) break
            val value = raw.substring(valStart, valEnd)
            index = valEnd

            when (tag) {
                // Merchant Account Information (Pix)
                "26" -> {
                    var subIdx = 0
                    while (subIdx + 4 <= value.length) {
                        val subTag = value.substring(subIdx, subIdx + 2)
                        val subLen = value.substring(subIdx + 2, subIdx + 4).toIntOrNull() ?: break
                        val subValStart = subIdx + 4
                        val subValEnd = subValStart + subLen
                        if (subValEnd > value.length) break
                        val subValue = value.substring(subValStart, subValEnd)
                        subIdx = subValEnd

                        when (subTag) {
                            "01" -> pixKey = subValue
                            "02" -> description = subValue
                        }
                    }
                }
                "54" -> {
                    amount = value.toDoubleOrNull()
                }
                "59" -> {
                    merchantName = value
                }
                "60" -> {
                    merchantCity = value
                }
                "62" -> {
                    var subIdx = 0
                    while (subIdx + 4 <= value.length) {
                        val subTag = value.substring(subIdx, subIdx + 2)
                        val subLen = value.substring(subIdx + 2, subIdx + 4).toIntOrNull() ?: break
                        val subValStart = subIdx + 4
                        val subValEnd = subValStart + subLen
                        if (subValEnd > value.length) break
                        val subValue = value.substring(subValStart, subValEnd)
                        subIdx = subValEnd

                        if (subTag == "05") {
                            txId = subValue
                        }
                    }
                }
            }
        }

        return PixData(
            pixKey = pixKey,
            merchantName = merchantName,
            merchantCity = merchantCity,
            amount = amount,
            txId = txId,
            description = description
        )
    }

    /**
     * Builds a valid EMVCo static Pix payload (QR Code copia e cola)
     */
    fun buildPayload(
        key: String,
        merchantName: String = "RECEBEDOR",
        merchantCity: String = "BRASIL",
        amount: Double? = null,
        txId: String = "***"
    ): String {
        fun tlv(id: String, value: String): String {
            val len = String.format(Locale.US, "%02d", value.length)
            return "$id$len$value"
        }

        // Clean merchant name and city (no accents, max 25 chars for name, 15 for city)
        val cleanName = normalizeString(merchantName).take(25).uppercase().ifBlank { "RECEBEDOR" }
        val cleanCity = normalizeString(merchantCity).take(15).uppercase().ifBlank { "BRASIL" }
        val cleanTxId = txId.filter { it.isLetterOrDigit() || it == '*' }.take(25).ifBlank { "***" }

        val sb = StringBuilder()
        sb.append(tlv("00", "01")) // Payload Format Indicator

        // 26: Merchant Account Information
        val gui = tlv("00", "BR.GOV.BCB.PIX")
        val pixKeyField = tlv("01", key.trim())
        val mai = gui + pixKeyField
        sb.append(tlv("26", mai))

        sb.append(tlv("52", "0000")) // Merchant Category Code
        sb.append(tlv("53", "986"))  // Transaction Currency (BRL = 986)

        if (amount != null && amount > 0.0) {
            val amountFormatted = String.format(Locale.US, "%.2f", amount)
            sb.append(tlv("54", amountFormatted))
        }

        sb.append(tlv("58", "BR"))
        sb.append(tlv("59", cleanName))
        sb.append(tlv("60", cleanCity))

        // 62: Additional Data Field (txId)
        val txIdTlv = tlv("05", cleanTxId)
        sb.append(tlv("62", txIdTlv))

        // 63: CRC16 placeholder
        val partial = sb.toString() + "6304"
        val crc = calculateCrc16(partial)

        return partial + crc
    }

    private fun normalizeString(input: String): String {
        val accents = "ÀÁÂÃÄÅàáâãäåÈÉÊËèéêëÌÍÎÏìíîïÒÓÔÕÖØòóôõöøÙÚÛÜùúûüÇçÑñ"
        val normalized = "AAAAAAaaaaaaEEEEeeeeIIIIiiiiOOOOOOooooooUUUUuuuuCcNn"
        var result = input
        for (i in accents.indices) {
            result = result.replace(accents[i], normalized[i])
        }
        return result
    }

    private fun calculateCrc16(input: String): String {
        var crc = 0xFFFF
        val polynomial = 0x1021

        val bytes = input.toByteArray(Charsets.ISO_8859_1)
        for (b in bytes) {
            for (i in 0 until 8) {
                val bit = (b.toInt() shr (7 - i) and 1) == 1
                val c15 = (crc shr 15 and 1) == 1
                crc = crc shl 1
                if (c15 xor bit) {
                    crc = crc xor polynomial
                }
            }
        }
        crc = crc and 0xFFFF
        return String.format(Locale.US, "%04X", crc)
    }
}
