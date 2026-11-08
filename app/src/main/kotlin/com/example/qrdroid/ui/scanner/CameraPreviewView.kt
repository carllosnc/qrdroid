package com.example.qrdroid.ui.scanner

import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.qrdroid.scanner.QrBarcodeAnalyzer
import com.google.mlkit.vision.barcode.common.Barcode
import java.util.concurrent.Executors

private const val TAG = "QRDroid_Camera"

@Composable
fun CameraPreviewView(
    isTorchOn: Boolean,
    zoomLinear: Float,
    isFrontCamera: Boolean,
    isScanningActive: Boolean,
    isActive: Boolean = true,
    onBarcodeDetected: (Barcode) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var camera by remember { mutableStateOf<Camera?>(null) }
    var previewViewInstance by remember { mutableStateOf<PreviewView?>(null) }
    var cameraProviderRef by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    val analyzer = remember {
        QrBarcodeAnalyzer { barcode ->
            onBarcodeDetected(barcode)
        }
    }

    LaunchedEffect(isScanningActive, isActive) {
        analyzer.setScanningEnabled(isScanningActive && isActive)
    }

    // Update torch when state changes (only if active)
    LaunchedEffect(isTorchOn, camera, isActive) {
        if (isActive) {
            camera?.cameraControl?.enableTorch(isTorchOn)
        } else {
            camera?.cameraControl?.enableTorch(false)
        }
    }

    // Update zoom when linear zoom changes
    LaunchedEffect(zoomLinear, camera) {
        camera?.cameraControl?.setLinearZoom(zoomLinear)
    }

    // Bind or unbind camera lifecycle when previewView is attached, camera flips, or isActive changes
    LaunchedEffect(previewViewInstance, isFrontCamera, lifecycleOwner, isActive) {
        val pv = previewViewInstance
        Log.d(TAG, "LaunchedEffect: pv=$pv, isFront=$isFrontCamera, isActive=$isActive, lifecycle=${lifecycleOwner.lifecycle.currentState}")

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                cameraProviderRef = cameraProvider

                if (!isActive || pv == null) {
                    Log.d(TAG, "Deactivating camera: unbindAll()")
                    cameraProvider.unbindAll()
                    camera = null
                    return@addListener
                }

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = pv.surfaceProvider
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, analyzer)
                    }

                val cameraSelector = if (isFrontCamera) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }

                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
                Log.d(TAG, "bindToLifecycle success! camera=$camera")

                pv.previewStreamState.observe(lifecycleOwner) { state ->
                    Log.d(TAG, "PreviewView streamState: $state")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in camera provider binding", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "CameraPreviewView onDispose: unbinding camera and shutting down executor")
            try {
                analyzer.close()
                cameraProviderRef?.unbindAll()
                camera = null
            } catch (e: Exception) {
                Log.e(TAG, "Error unbinding camera on dispose", e)
            }
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            Log.d(TAG, "AndroidView factory creating PreviewView")
            PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                previewViewInstance = this
            }
        }
    )
}
