package com.example.naturemarks.ui.screens.scan

import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.naturemarks.R
import com.example.naturemarks.app.NatureMarksApplication
import com.example.naturemarks.data.location.LocationRepository
import com.example.naturemarks.data.memory.MemoryRepository
import com.example.naturemarks.data.postmark.PostmarkRepository
import com.example.naturemarks.data.storage.MediaStorageRepository
import com.example.naturemarks.ui.components.PopUpDialog
import com.example.naturemarks.ui.screens.scan.camera.CameraMode
import com.example.naturemarks.ui.screens.scan.camera.CameraView
import com.example.naturemarks.ui.screens.scan.camera.ImageCaptureView

@Composable
@RequiresPermission(allOf = ["android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"])
fun ScanScreen(
    onBack: () -> Unit,
    onOpenGallery: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as NatureMarksApplication

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
            Box(modifier = Modifier.fillMaxSize()) {
                CameraView(
                    mode = CameraMode.SCAN,
                    onQrCodeScanned = { raw ->
                        if (uiState.mark == null) {
                            viewModel.onQrScanned(raw)
                        }
                    }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(WindowInsets.systemBars.asPaddingValues())
            ) {

                Text(
                    text = stringResource(R.string.scan_code),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                )

                if (uiState.showDuplicateErrorDialog && uiState.mark != null){
                    PopUpDialog(
                        title = stringResource(R.string.dialog_duplicate_title),
                        text = stringResource(R.string.dialog_duplicate_description),
                        imageId = uiState.markRes,
                        textConfirm = stringResource(R.string.gallery),
                        onConfirmation = {
                            viewModel.onCloseDuplicateErrorDialog()
                            onOpenGallery()
                        }
                    )
                }

                if (uiState.showLocationErrorDialog && uiState.mark != null){
                    PopUpDialog(
                        title = stringResource(R.string.dialog_duplicate_title),
                        text = stringResource(R.string.dialog_location_error_description),
                        imageId = R.drawable.mark,
                        textConfirm = stringResource(R.string.ok),
                        onConfirmation = {
                            viewModel.onCloseLocationErrorDialog()
                        }
                    )
                }

                if (uiState.showDialog && uiState.mark != null) {
                    PopUpDialog(
                        title = stringResource(R.string.dialog_new_mark_title),
                        text = stringResource(R.string.dialog_new_mark_description) + " " + stringResource(R.string.camera_emoji),
                        imageId = R.drawable.mark,
                        textConfirm = stringResource(R.string.take_photo),
                        onConfirmation = {
                            viewModel.prepareForPhotoCapture()
                        },
                        textDismiss = stringResource(R.string.skip),
                        onDismiss = {
                            viewModel.onCloseDialog()
                            onOpenGallery()
                        }
                    )
                }

                if (uiState.showErrorDialog && uiState.mark == null) {
                    PopUpDialog(
                        title = stringResource(R.string.dialog_invalid_qr_title),
                        text = stringResource(R.string.dialog_invalid_qr_description),
                        imageId = R.drawable.question_mark,
                        textConfirm = stringResource(R.string.close),
                        onConfirmation = {
                            viewModel.onCloseErrorDialog()
                        }
                    )
                }
            }
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