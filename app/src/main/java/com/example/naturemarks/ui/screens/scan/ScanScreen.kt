package com.example.naturemarks.ui.screens.scan

import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.naturemarks.app.NatureMarksApplication
import com.example.naturemarks.data.location.LocationRepository
import com.example.naturemarks.data.memory.MemoryRepository
import com.example.naturemarks.data.postmark.PostmarkRepository
import com.example.naturemarks.data.storage.MediaStorageRepository
import com.example.naturemarks.ui.screens.scan.camera.CameraMode
import com.example.naturemarks.ui.screens.scan.camera.ImageCaptureView
import com.example.naturemarks.ui.screens.scan.camera.ScanView
import com.example.naturemarks.ui.screens.scan.data.PopUpDialogUiMapper

@Composable
@RequiresPermission(allOf = ["android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"])
fun ScanScreen(
    onBack: () -> Unit,
    onOpenGallery: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as NatureMarksApplication

    val dialogMapper by lazy { PopUpDialogUiMapper(context) }

    val viewModel: ScanViewModel = viewModel(
        factory = ScanViewModelFactory(
            application = app,
            postmarkRepository = PostmarkRepository(app.database, MemoryRepository(app.database)),
            locationRepository = LocationRepository(app),
            mediaStorageRepository = MediaStorageRepository(app)
        )
    )

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is ScanViewModel.ScanEvent.NavigateToGallery -> {
                    onOpenGallery()
                }
                is ScanViewModel.ScanEvent.NavigateBack -> {
                    onBack()
                }
            }
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    when(uiState.cameraMode){
        CameraMode.SCAN -> {
            ScanView(
                showDialog = uiState.showDialog,
                onQrScanned = viewModel::onQrScanned,
                markRes = uiState.markRes,
                dialogData = dialogMapper.map(uiState.dialogType),
                onCloseDialog = viewModel::onCloseDialog,
                prepareForPhotoCapture = viewModel::prepareForPhotoCapture,
                onOpenGallery = onOpenGallery,
                navigateBack = onBack
            )
        }
        CameraMode.CAPTURE -> {
            ImageCaptureView(
                markId = uiState.markRes,
                onPhotoCaptured = viewModel::onPhotoCaptured
            )
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}