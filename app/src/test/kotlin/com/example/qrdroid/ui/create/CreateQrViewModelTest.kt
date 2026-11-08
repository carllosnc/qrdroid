package com.example.qrdroid.ui.create

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateQrViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onTextChange_debouncesAndGeneratesPayload() = runTest(testDispatcher) {
        val viewModel = CreateQrViewModel(defaultDispatcher = testDispatcher)

        viewModel.onTextChange("Hello World")
        advanceTimeBy(100) // Before debounce completes
        assertEquals("", viewModel.uiState.value.generatedPayload)

        advanceTimeBy(200) // After debounce
        testScheduler.advanceUntilIdle()
        assertEquals("Hello World", viewModel.uiState.value.generatedPayload)
    }

    @Test
    fun selectPixType_generatesPixPayload() = runTest(testDispatcher) {
        val viewModel = CreateQrViewModel(defaultDispatcher = testDispatcher)
        viewModel.selectType(QrCreateType.PIX)
        viewModel.onPixKeyChange("12345678900")
        viewModel.onPixMerchantNameChange("Fulano")
        viewModel.onPixMerchantCityChange("Brasilia")
        viewModel.onPixAmountChange("25.00")

        advanceTimeBy(300)
        testScheduler.advanceUntilIdle()

        val payload = viewModel.uiState.value.generatedPayload
        assertTrue("Expected payload to start with 000201 but was: $payload", payload.startsWith("000201"))
        assertTrue("Expected payload to contain key but was: $payload", payload.contains("12345678900"))
        assertTrue("Expected payload to contain name but was: $payload", payload.contains("FULANO"))
    }

    @Test
    fun colorCustomization_updatesStateAndSwapsAndResets() = runTest(testDispatcher) {
        val viewModel = CreateQrViewModel(defaultDispatcher = testDispatcher)

        assertEquals(0xFF000000.toInt(), viewModel.uiState.value.darkColor)
        assertEquals(0xFFFFFFFF.toInt(), viewModel.uiState.value.lightColor)

        val customBlue = 0xFF00506E.toInt()
        val customYellow = 0xFFFFFDE7.toInt()

        viewModel.onDarkColorChange(customBlue)
        viewModel.onLightColorChange(customYellow)

        assertEquals(customBlue, viewModel.uiState.value.darkColor)
        assertEquals(customYellow, viewModel.uiState.value.lightColor)

        // Swap colors
        viewModel.swapColors()
        assertEquals(customYellow, viewModel.uiState.value.darkColor)
        assertEquals(customBlue, viewModel.uiState.value.lightColor)

        // Reset to default
        viewModel.resetColors()
        assertEquals(0xFF000000.toInt(), viewModel.uiState.value.darkColor)
        assertEquals(0xFFFFFFFF.toInt(), viewModel.uiState.value.lightColor)
    }
}
