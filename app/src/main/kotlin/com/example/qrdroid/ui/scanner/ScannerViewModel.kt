package com.example.qrdroid.ui.scanner

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qrdroid.data.model.ScanItem
import com.example.qrdroid.data.repository.ScanRepository
import com.example.qrdroid.scanner.BarcodeFormatUtils
import com.example.qrdroid.scanner.PayloadClassifier
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ScannerUiState(
    val isTorchOn: Boolean = false,
    val zoomLinear: Float = 0.0f,
    val isFrontCamera: Boolean = false,
    val isScanningActive: Boolean = true,
    val currentResult: ScanItem? = null,
    val errorMessage: String? = null
)

class ScannerViewModel(
    private val repository: ScanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState = _uiState.asStateFlow()

    private val _hapticEvent = MutableSharedFlow<Unit>()
    val hapticEvent = _hapticEvent.asSharedFlow()

    private var lastScanTime = 0L

    fun onBarcodeDetected(barcode: Barcode) {
        val now = System.currentTimeMillis()
        if (now - lastScanTime < 1500L) {
            // Cooldown to avoid duplicate popups
            return
        }
        val raw = barcode.rawValue ?: return
        if (raw.isBlank()) return

        lastScanTime = now
        val (scanType, displayVal) = PayloadClassifier.classify(barcode)
        val formatName = BarcodeFormatUtils.getFormatName(barcode.format)

        val item = ScanItem(
            rawValue = raw,
            displayValue = displayVal,
            formatName = formatName,
            formatCode = barcode.format,
            valueType = scanType,
            timestamp = now
        )

        viewModelScope.launch {
            val insertedId = repository.insertScan(item)
            val savedItem = item.copy(id = insertedId)
            _uiState.value = _uiState.value.copy(
                currentResult = savedItem,
                isScanningActive = false
            )
            _hapticEvent.emit(Unit)
        }
    }

    fun scanImageUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val inputImage = InputImage.fromFilePath(context, uri)
                val scanner = BarcodeScanning.getClient(
                    BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                        .build()
                )

                scanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        val barcode = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }
                        if (barcode != null) {
                            onBarcodeDetected(barcode)
                        } else {
                            _uiState.value = _uiState.value.copy(
                                errorMessage = "Nenhum código encontrado na imagem selecionada."
                            )
                        }
                    }
                    .addOnFailureListener {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Falha ao analisar a imagem: ${it.message}"
                        )
                    }
                    .addOnCompleteListener {
                        try {
                            scanner.close()
                        } catch (_: Exception) {}
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Erro ao carregar imagem: ${e.message}"
                )
            }
        }
    }

    fun toggleTorch() {
        _uiState.value = _uiState.value.copy(isTorchOn = !_uiState.value.isTorchOn)
    }

    fun setZoom(linearZoom: Float) {
        _uiState.value = _uiState.value.copy(zoomLinear = linearZoom)
    }

    fun switchCamera() {
        _uiState.value = _uiState.value.copy(
            isFrontCamera = !_uiState.value.isFrontCamera,
            isTorchOn = false // Turn off torch when switching
        )
    }

    fun dismissResult() {
        _uiState.value = _uiState.value.copy(
            currentResult = null,
            isScanningActive = true
        )
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun pauseScanner() {
        _uiState.value = _uiState.value.copy(
            isTorchOn = false,
            isScanningActive = false
        )
    }

    fun resumeScanner() {
        _uiState.value = _uiState.value.copy(
            isScanningActive = true
        )
    }

    fun toggleFavorite(item: ScanItem) {
        viewModelScope.launch {
            val newFav = !item.isFavorite
            repository.toggleFavorite(item.id, newFav)
            if (_uiState.value.currentResult?.id == item.id) {
                _uiState.value = _uiState.value.copy(
                    currentResult = _uiState.value.currentResult?.copy(isFavorite = newFav)
                )
            }
        }
    }

    fun triggerVibration(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(70L)
            }
        } catch (e: Exception) {
            // Ignore if vibration is not supported
        }
    }
}
