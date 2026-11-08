package com.example.qrdroid.scanner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PixParserTest {

    @Test
    fun buildPayload_generatesValidEmvCoPixString() {
        val payload = PixParser.buildPayload(
            key = "12345678900",
            merchantName = "Fulano de Tal",
            merchantCity = "Brasilia",
            amount = 150.50,
            txId = "PEDIDO123"
        )

        assertTrue("Payload should start with 000201", payload.startsWith("000201"))
        assertTrue("Payload should contain BR.GOV.BCB.PIX", payload.contains("BR.GOV.BCB.PIX"))
        assertTrue("Payload should contain the key", payload.contains("12345678900"))
        assertTrue("Payload should contain currency 986", payload.contains("5303986"))
        assertTrue("Payload should end with CRC tag", payload.contains("6304"))

        val parsed = PixParser.parse(payload)
        assertEquals("12345678900", parsed.pixKey)
        assertEquals("FULANO DE TAL", parsed.merchantName)
        assertEquals("BRASILIA", parsed.merchantCity)
        assertNotNull(parsed.amount)
        assertEquals(150.50, parsed.amount!!, 0.001)
        assertEquals("PEDIDO123", parsed.txId)
    }

    @Test
    fun buildPayload_withoutAmount_buildsPayloadSuccessfully() {
        val payload = PixParser.buildPayload(
            key = "user@test.com",
            merchantName = "Empresa ABC",
            merchantCity = "Sao Paulo"
        )

        assertTrue(payload.startsWith("000201"))
        val parsed = PixParser.parse(payload)
        assertEquals("user@test.com", parsed.pixKey)
        assertEquals("EMPRESA ABC", parsed.merchantName)
    }
}
