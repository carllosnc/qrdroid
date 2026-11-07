package com.example.qrdroid.scanner

import com.google.mlkit.vision.barcode.common.Barcode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PayloadClassifierTest {

    @Test
    fun isPix_validPixPayload_returnsTrue() {
        val pixPayload = "00020126580014br.gov.bcb.pix0136123e4567-e89b-12d3-a456-4266141740005204000053039865802BR5913Fulano de Tal6008BRASILIA62070503***63041D3D"
        assertTrue(PayloadClassifier.isPix(pixPayload))
    }

    @Test
    fun isPix_regularUrl_returnsFalse() {
        assertFalse(PayloadClassifier.isPix("https://antigravity.google.com"))
    }

    @Test
    fun isBoleto_44DigitBarcode_returnsTrue() {
        val boleto44 = "34191790010104351004791020150008479900000100"
        assertTrue(PayloadClassifier.isBoleto(boleto44, Barcode.FORMAT_ITF))
    }

    @Test
    fun isBoleto_47DigitLinhaDigitavel_returnsTrue() {
        val linha47 = "34191.79001 01043.510047 91020.150008 4 79900000010000"
        assertTrue(PayloadClassifier.isBoleto(linha47, Barcode.FORMAT_QR_CODE))
    }

    @Test
    fun formatBoleto_formatsLinhaDigitavelCorrectly() {
        val raw = "34191790010104351004791020150008479900000010000" // 47 digits
        val formatted = PayloadClassifier.formatBoleto(raw)
        assertEquals("34191.79001 01043.510047 91020.150008 4 79900000010000", formatted)
    }

    @Test
    fun barcodeFormatUtils_mapsFormatsCorrectly() {
        assertEquals("QR Code", BarcodeFormatUtils.getFormatName(Barcode.FORMAT_QR_CODE))
        assertEquals("EAN-13", BarcodeFormatUtils.getFormatName(Barcode.FORMAT_EAN_13))
        assertEquals("Code 128", BarcodeFormatUtils.getFormatName(Barcode.FORMAT_CODE_128))
        assertEquals("Data Matrix", BarcodeFormatUtils.getFormatName(Barcode.FORMAT_DATA_MATRIX))
    }
}
