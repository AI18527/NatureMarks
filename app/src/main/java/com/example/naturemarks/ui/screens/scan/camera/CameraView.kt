package com.example.naturemarks.ui.screens.scan.camera

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.naturemarks.ui.screens.scan.QrAnalyzer
import java.io.File

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
    val imageCapture = remember { ImageCapture.Builder().build() }

    AndroidView(
        factory = { previewView },
        update = { view ->
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(view.surfaceProvider)
            }
            val cameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()

            if (mode == CameraMode.SCAN) {
                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalyzer.setAnalyzer(
                    ContextCompat.getMainExecutor(context),
                    QrAnalyzer { qrText ->
                        onQrCodeScanned(qrText)
                    }
                )

                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
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
                if (captureTrigger) {
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(
                        File.createTempFile("photo_", ".jpg", context.cacheDir)
                    ).build()

                    imageCapture.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                val uri = output.savedUri ?: return
                                val source = ImageDecoder.createSource(context.contentResolver, uri)
                                val bitmap = ImageDecoder.decodeBitmap(source)
                                onPhotoCaptured(bitmap)
                            }

                            override fun onError(exception: ImageCaptureException) {
                                exception.printStackTrace()
                            }
                        }
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}