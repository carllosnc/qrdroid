package com.example.qrdroid.scanner

import com.example.qrdroid.data.model.ScanType
import com.google.mlkit.vision.barcode.common.Barcode

object PayloadClassifier {

    fun classify(barcode: Barcode): Pair<ScanType, String> {
        val raw = barcode.rawValue ?: ""
        val format = barcode.format

        // 1. Check for PIX
        if (isPix(raw)) {
            return Pair(ScanType.PIX, raw)
        }

        // 2. Check for Boleto (44 digits or 47/48 digits)
        if (isBoleto(raw, format)) {
            val formatted = formatBoleto(raw)
            return Pair(ScanType.BOLETO, formatted)
        }

        // 3. ML Kit built-in types
        when (barcode.valueType) {
            Barcode.TYPE_URL -> {
                return Pair(ScanType.URL, barcode.url?.url ?: raw)
            }
            Barcode.TYPE_WIFI -> {
                val wifi = barcode.wifi
                val ssid = wifi?.ssid ?: ""
                val encryptionType = when (wifi?.encryptionType) {
                    Barcode.WiFi.TYPE_WPA -> "WPA"
                    Barcode.WiFi.TYPE_WEP -> "WEP"
                    else -> "Aberta"
                }
                val summary = "Rede: $ssid ($encryptionType)"
                return Pair(ScanType.WIFI, summary)
            }
            Barcode.TYPE_PHONE -> {
                return Pair(ScanType.PHONE, barcode.phone?.number ?: raw)
            }
            Barcode.TYPE_EMAIL -> {
                return Pair(ScanType.EMAIL, barcode.email?.address ?: raw)
            }
            Barcode.TYPE_CONTACT_INFO -> {
                val name = barcode.contactInfo?.name?.formattedName ?: "Contato"
                return Pair(ScanType.CONTACT, name)
            }
            Barcode.TYPE_PRODUCT -> {
                return Pair(ScanType.PRODUCT, raw)
            }
        }

        // 4. Product formats (EAN-13, EAN-8, UPC-A, UPC-E)
        if (format == Barcode.FORMAT_EAN_13 || format == Barcode.FORMAT_EAN_8 ||
            format == Barcode.FORMAT_UPC_A || format == Barcode.FORMAT_UPC_E
        ) {
            return Pair(ScanType.PRODUCT, raw)
        }

        // 5. Fallback URL detection
        if (raw.startsWith("http://", ignoreCase = true) ||
            raw.startsWith("https://", ignoreCase = true) ||
            raw.startsWith("www.", ignoreCase = true)
        ) {
            return Pair(ScanType.URL, raw)
        }

        return Pair(ScanType.TEXT, raw)
    }

    internal fun isPix(value: String): Boolean {
        return (value.startsWith("000201") && value.contains("pix", ignoreCase = true)) ||
                value.contains("gov.bcb.pix", ignoreCase = true)
    }

    internal fun isBoleto(value: String, format: Int): Boolean {
        val clean = value.filter { it.isDigit() }
        // Boleto barcode usually has 44 digits (ITF-14 or Code 128)
        if (clean.length == 44 && (format == Barcode.FORMAT_ITF || format == Barcode.FORMAT_CODE_128)) {
            return true
        }
        // Linha digitável has 47 (bancário) or 48 (concessionária) digits
        if (clean.length == 47 || clean.length == 48) {
            return true
        }
        return false
    }

    internal fun formatBoleto(value: String): String {
        val clean = value.filter { it.isDigit() }
        if (clean.length == 47) {
            // Formato: XXXXX.XXXXX XXXXX.XXXXXX XXXXX.XXXXXX X XXXXXXXXXXXXXX
            return buildString {
                append(clean.substring(0, 5)).append('.')
                append(clean.substring(5, 10)).append(' ')
                append(clean.substring(10, 15)).append('.')
                append(clean.substring(15, 21)).append(' ')
                append(clean.substring(21, 26)).append('.')
                append(clean.substring(26, 32)).append(' ')
                append(clean.substring(32, 33)).append(' ')
                append(clean.substring(33))
            }
        }
        return value
    }
}
