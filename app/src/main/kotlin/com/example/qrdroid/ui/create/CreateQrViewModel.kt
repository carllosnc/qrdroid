package com.example.qrdroid.ui.create

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qrdroid.generator.QrCodeGenerator
import com.example.qrdroid.scanner.WifiParser
import kotlinx.coroutines.Dispatchers
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
    WIFI("Wi-Fi")
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
    // Generated
    val generatedPayload: String = "",
    val qrBitmap: Bitmap? = null,
    val isGenerating: Boolean = false
)

class CreateQrViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CreateQrUiState())
    val uiState: StateFlow<CreateQrUiState> = _uiState.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    fun selectType(type: QrCreateType) {
        _uiState.value = _uiState.value.copy(selectedType = type)
        regenerateQr()
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

    private fun regenerateQr() {
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
        }

        if (payload.isBlank()) {
            _uiState.value = state.copy(generatedPayload = "", qrBitmap = null)
            return
        }

        viewModelScope.launch {
            val bitmap = withContext(Dispatchers.Default) {
                QrCodeGenerator.generateBitmap(payload, size = 650)
            }
            _uiState.value = _uiState.value.copy(
                generatedPayload = payload,
                qrBitmap = bitmap
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
