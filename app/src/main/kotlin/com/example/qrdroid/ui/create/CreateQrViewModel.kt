package com.example.qrdroid.ui.create

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qrdroid.generator.QrCodeGenerator
import com.example.qrdroid.scanner.PixParser
import com.example.qrdroid.scanner.WifiParser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class QrCreateType(val label: String) {
    TEXT("Texto"),
    URL("Link / URL"),
    WIFI("Wi-Fi"),
    PIX("PIX")
}

data class CreateQrUiState(
    val selectedType: QrCreateType = QrCreateType.TEXT,
    // Text fields
    val textContent: String = "",
    // URL fields
    val urlContent: String = "https://",
    // Wi-Fi fields
    val wifiSsid: String = "",
    val wifiPassword: String = "",
    val wifiSecurity: String = "WPA",
    val wifiHidden: Boolean = false,
    // PIX fields
    val pixKey: String = "",
    val pixMerchantName: String = "",
    val pixMerchantCity: String = "",
    val pixAmount: String = "",
    val pixTxId: String = "***",
    // Colors
    val darkColor: Int = 0xFF000000.toInt(),
    val lightColor: Int = 0xFFFFFFFF.toInt(),
    // Generated
    val generatedPayload: String = "",
    val qrBitmap: Bitmap? = null,
    val isGenerating: Boolean = false
)

class CreateQrViewModel(
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateQrUiState())
    val uiState: StateFlow<CreateQrUiState> = _uiState.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    private var generationJob: Job? = null

    fun selectType(type: QrCreateType) {
        _uiState.value = _uiState.value.copy(selectedType = type)
        regenerateQr(debounce = false)
    }

    fun onTextChange(text: String) {
        _uiState.value = _uiState.value.copy(textContent = text)
        regenerateQr()
    }

    fun onUrlChange(url: String) {
        _uiState.value = _uiState.value.copy(urlContent = url)
        regenerateQr()
    }

    fun onWifiSsidChange(ssid: String) {
        _uiState.value = _uiState.value.copy(wifiSsid = ssid)
        regenerateQr()
    }

    fun onWifiPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(wifiPassword = password)
        regenerateQr()
    }

    fun onWifiSecurityChange(security: String) {
        _uiState.value = _uiState.value.copy(wifiSecurity = security)
        regenerateQr()
    }

    fun onPixKeyChange(key: String) {
        _uiState.value = _uiState.value.copy(pixKey = key)
        regenerateQr()
    }

    fun onPixMerchantNameChange(name: String) {
        _uiState.value = _uiState.value.copy(pixMerchantName = name)
        regenerateQr()
    }

    fun onPixMerchantCityChange(city: String) {
        _uiState.value = _uiState.value.copy(pixMerchantCity = city)
        regenerateQr()
    }

    fun onPixAmountChange(amount: String) {
        _uiState.value = _uiState.value.copy(pixAmount = amount)
        regenerateQr()
    }

    fun onPixTxIdChange(txId: String) {
        _uiState.value = _uiState.value.copy(pixTxId = txId)
        regenerateQr()
    }

    fun onDarkColorChange(color: Int) {
        if (_uiState.value.darkColor != color) {
            _uiState.value = _uiState.value.copy(darkColor = color)
            regenerateQr(debounce = false)
        }
    }

    fun onLightColorChange(color: Int) {
        if (_uiState.value.lightColor != color) {
            _uiState.value = _uiState.value.copy(lightColor = color)
            regenerateQr(debounce = false)
        }
    }

    fun swapColors() {
        val currentDark = _uiState.value.darkColor
        val currentLight = _uiState.value.lightColor
        _uiState.value = _uiState.value.copy(darkColor = currentLight, lightColor = currentDark)
        regenerateQr(debounce = false)
    }

    fun resetColors() {
        _uiState.value = _uiState.value.copy(
            darkColor = 0xFF000000.toInt(),
            lightColor = 0xFFFFFFFF.toInt()
        )
        regenerateQr(debounce = false)
    }

    private fun regenerateQr(debounce: Boolean = true) {
        generationJob?.cancel()

        val state = _uiState.value
        val payload = when (state.selectedType) {
            QrCreateType.TEXT -> state.textContent.trim()
            QrCreateType.URL -> state.urlContent.trim().let {
                if (it == "https://" || it.isBlank()) "" else it
            }
            QrCreateType.WIFI -> {
                if (state.wifiSsid.isBlank()) ""
                else WifiParser.buildPayload(
                    ssid = state.wifiSsid,
                    password = state.wifiPassword,
                    type = state.wifiSecurity,
                    isHidden = state.wifiHidden
                )
            }
            QrCreateType.PIX -> {
                if (state.pixKey.isBlank()) ""
                else {
                    val amountDouble = state.pixAmount.replace(",", ".").toDoubleOrNull()
                    PixParser.buildPayload(
                        key = state.pixKey.trim(),
                        merchantName = state.pixMerchantName.ifBlank { "RECEBEDOR" },
                        merchantCity = state.pixMerchantCity.ifBlank { "BRASIL" },
                        amount = amountDouble,
                        txId = state.pixTxId.ifBlank { "***" }
                    )
                }
            }
        }

        if (payload.isBlank()) {
            _uiState.value = state.copy(
                generatedPayload = "",
                qrBitmap = null,
                isGenerating = false
            )
            return
        }

        generationJob = viewModelScope.launch {
            if (debounce) {
                delay(200)
            }
            _uiState.value = _uiState.value.copy(isGenerating = true)
            val bitmap = withContext(defaultDispatcher) {
                QrCodeGenerator.generateBitmap(
                    content = payload,
                    size = 650,
                    darkColor = state.darkColor,
                    lightColor = state.lightColor
                )
            }
            _uiState.value = _uiState.value.copy(
                generatedPayload = payload,
                qrBitmap = bitmap,
                isGenerating = false
            )
        }
    }

    fun saveQrToGallery(context: Context) {
        val bitmap = _uiState.value.qrBitmap ?: return
        viewModelScope.launch {
            val uri = withContext(Dispatchers.IO) {
                val title = when (_uiState.value.selectedType) {
                    QrCreateType.TEXT -> "texto"
                    QrCreateType.URL -> "link"
                    QrCreateType.WIFI -> "wifi_${_uiState.value.wifiSsid}"
                    QrCreateType.PIX -> "pix_${_uiState.value.pixKey.take(10)}"
                }
                QrCodeGenerator.saveToGallery(context, bitmap, title)
            }
            if (uri != null) {
                _toastEvent.emit("QR Code salvo na galeria!")
            } else {
                _toastEvent.emit("Erro ao salvar imagem.")
            }
        }
    }

    fun shareQrCode(context: Context) {
        val bitmap = _uiState.value.qrBitmap ?: return
        val payload = _uiState.value.generatedPayload
        QrCodeGenerator.shareQrCode(context, bitmap, payload)
    }
}
