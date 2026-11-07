package com.example.qrdroid.scanner

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class BoletoData(
    val bankCode: String? = null,
    val bankName: String? = null,
    val amount: Double? = null,
    val dueDate: String? = null,
    val formattedLine: String = ""
)

object BoletoParser {

    private val BANK_MAP = mapOf(
        "001" to "Banco do Brasil",
        "003" to "Banco da Amazônia",
        "004" to "Banco do Nordeste",
        "033" to "Santander",
        "041" to "Banrisul",
        "070" to "BRB - Banco de Brasília",
        "077" to "Banco Inter",
        "104" to "Caixa Econômica Federal",
        "208" to "BTG Pactual",
        "212" to "Banco Original",
        "237" to "Bradesco",
        "260" to "Nubank",
        "336" to "C6 Bank",
        "341" to "Itaú Unibanco",
        "389" to "Banco Mercantil do Brasil",
        "422" to "Banco Safra",
        "655" to "Banco Neon",
        "748" to "Sicredi",
        "756" to "Sicoob"
    )

    fun parse(raw: String): BoletoData {
        val clean = raw.filter { it.isDigit() }

        if (clean.length == 47) {
            val bankCode = clean.substring(0, 3)
            val bankName = BANK_MAP[bankCode] ?: "Banco $bankCode"

            // Expiration factor & amount are in the last 14 digits (digits 33 to 47)
            val factorStr = clean.substring(33, 37)
            val amountStr = clean.substring(37, 47)

            val factor = factorStr.toIntOrNull() ?: 0
            val dueDate = calculateDueDate(factor)

            val rawAmount = amountStr.toLongOrNull() ?: 0L
            val amount = if (rawAmount > 0) rawAmount / 100.0 else null

            val formatted = buildString {
                append(clean.substring(0, 5)).append('.')
                append(clean.substring(5, 10)).append(' ')
                append(clean.substring(10, 15)).append('.')
                append(clean.substring(15, 21)).append(' ')
                append(clean.substring(21, 26)).append('.')
                append(clean.substring(26, 32)).append(' ')
                append(clean.substring(32, 33)).append(' ')
                append(clean.substring(33, 47))
            }

            return BoletoData(
                bankCode = bankCode,
                bankName = bankName,
                amount = amount,
                dueDate = dueDate,
                formattedLine = formatted
            )
        }

        if (clean.length == 44) {
            val bankCode = clean.substring(0, 3)
            val bankName = BANK_MAP[bankCode] ?: "Banco $bankCode"
            val factorStr = clean.substring(5, 9)
            val amountStr = clean.substring(9, 19)

            val factor = factorStr.toIntOrNull() ?: 0
            val dueDate = calculateDueDate(factor)
            val rawAmount = amountStr.toLongOrNull() ?: 0L
            val amount = if (rawAmount > 0) rawAmount / 100.0 else null

            return BoletoData(
                bankCode = bankCode,
                bankName = bankName,
                amount = amount,
                dueDate = dueDate,
                formattedLine = clean
            )
        }

        return BoletoData(formattedLine = raw)
    }

    private fun calculateDueDate(factor: Int): String? {
        if (factor < 1000) return null

        val cal = Calendar.getInstance()
        // Base 1: 07/10/1997
        // New cycle after 22/02/2025: factor 1000 = 22/02/2025
        val now = System.currentTimeMillis()
        val cycle2025Threshold = 1740182400000L // Feb 22, 2025

        if (now >= cycle2025Threshold) {
            // Cycle 2: Base 22/02/2025 (factor 1000)
            cal.set(2025, Calendar.FEBRUARY, 22, 0, 0, 0)
            cal.add(Calendar.DAY_OF_YEAR, factor - 1000)
        } else {
            // Cycle 1: Base 07/10/1997
            cal.set(1997, Calendar.OCTOBER, 7, 0, 0, 0)
            cal.add(Calendar.DAY_OF_YEAR, factor)
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        return sdf.format(cal.time)
    }
}
