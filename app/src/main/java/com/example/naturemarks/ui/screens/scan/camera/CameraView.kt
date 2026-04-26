package com.example.naturemarks.ui.screens.scan.camera

import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
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
import com.example.naturemarks.util.CameraHelper
import com.example.naturemarks.util.QrAnalyzer

enum class CameraMode {
    SCAN,
    CAPTURE
}
@Composable
fun CameraView(
    mode: CameraMode,
    captureTrigger: Boolean = false,
    onQrCodeScanned: (String) -> Unit = {},
    onPhotoCaptured: (Bitmap) -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraHelper = remember { CameraHelper(context) }

    var isQrProcessing by remember { mutableStateOf(false) }

    LaunchedEffect(cameraProviderFuture) {
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
        }, ContextCompat.getMainExecutor(context))
    }

    LaunchedEffect(cameraProvider, mode) {
        cameraProvider?.let { cameraProvider ->
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.unbindAll()

            if (mode == CameraMode.SCAN) {
                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalyzer.setAnalyzer(
                    ContextCompat.getMainExecutor(context),
                    QrAnalyzer { qrText ->
                        if (isQrProcessing) return@QrAnalyzer
                        isQrProcessing = true
                        onQrCodeScanned(qrText)
                    }
                )

                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
            }
            if (mode == CameraMode.CAPTURE) {
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            }
        } ?: return@LaunchedEffect
    }

    LaunchedEffect(captureTrigger) {
        if (captureTrigger) {
            cameraHelper.takePicture(
                imageCapture,
                onPhotoCaptured
            )
        }
        else return@LaunchedEffect
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraProvider?.unbindAll()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )
}