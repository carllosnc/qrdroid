package com.example.qrdroid.data.model

data class ScanItem(
    val id: Long = 0,
    val rawValue: String,
    val displayValue: String,
    val formatName: String,
    val formatCode: Int,
    val valueType: ScanType,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

enum class ScanType {
    URL,
    PIX,
    BOLETO,
    WIFI,
    PRODUCT,
    CONTACT,
    PHONE,
    EMAIL,
    TEXT;

    val displayName: String
        get() = when (this) {
            URL -> "Link Web"
            PIX -> "PIX Copia e Cola"
            BOLETO -> "Boleto Bancário"
            WIFI -> "Rede Wi-Fi"
            PRODUCT -> "Código de Produto"
            CONTACT -> "Contato"
            PHONE -> "Telefone"
            EMAIL -> "E-mail"
            TEXT -> "Texto Puro"
        }
}
