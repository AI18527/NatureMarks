package com.example.naturemarks.ui.screens.scan.camera

import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController.IMAGE_ANALYSIS
import androidx.camera.view.CameraController.IMAGE_CAPTURE
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.naturemarks.util.QrAnalyzer
import java.io.File

enum class CameraMode {
    SCAN,
    CAPTURE
}
@Composable
fun CameraView(
    mode: CameraMode,
    captureTrigger: Boolean = false,
    onQrScanned: (String) -> Unit = {},
    onPhotoCaptured: (File) -> Unit = {},
    onError: (Exception) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val lifecycleCameraController = remember {
        LifecycleCameraController(context).apply {
            bindToLifecycle(lifecycleOwner)
        }
    }

    LaunchedEffect(mode) {
        lifecycleCameraController.setEnabledUseCases(
            if (mode == CameraMode.SCAN) IMAGE_ANALYSIS
            else IMAGE_CAPTURE
        )

        if (mode == CameraMode.SCAN) {
            lifecycleCameraController.setImageAnalysisAnalyzer(
                ContextCompat.getMainExecutor(context),
                QrAnalyzer { qrText -> onQrScanned(qrText) }
            )
        }
        else lifecycleCameraController.clearImageAnalysisAnalyzer()
    }

    LaunchedEffect(captureTrigger) {
        if (captureTrigger) {
            val file = File(context.cacheDir, "photo.jpg")
            val options = ImageCapture.OutputFileOptions.Builder(file).build()

            lifecycleCameraController.takePicture(
                options,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        onPhotoCaptured(file)
                    }
                    override fun onError(exc: ImageCaptureException) {
                        onError(exc)
                    }
                }
            )
        }
    }

    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                controller = lifecycleCameraController
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}