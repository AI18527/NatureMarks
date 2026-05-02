package com.example.naturemarks.ui.screens.scan.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.naturemarks.R
import com.example.naturemarks.ui.components.PopUpDialog
import com.example.naturemarks.ui.screens.scan.data.PopUpDialogUiModel

@Composable
fun ScanView(
    markRes : Int,
    showDialog : Boolean,
    dialogData : PopUpDialogUiModel?,
    onQrScanned : (String) -> Unit,
    onCloseDialog : () -> Unit,
    prepareForPhotoCapture : () -> Unit,
    onOpenGallery : () -> Unit,
    navigateBack : () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        CameraView(
            mode = CameraMode.SCAN,
            onQrScanned = onQrScanned
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.3f))
                .padding(WindowInsets.systemBars.asPaddingValues())
        ) {
            Text(
                text = stringResource(R.string.scan_code),
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )

            if (showDialog && dialogData != null) {
                DialogView(
                    markRes = markRes,
                    dialogData = dialogData,
                    onCloseDialog = onCloseDialog,
                    prepareForPhotoCapture = prepareForPhotoCapture,
                    onOpenGallery = onOpenGallery,
                    navigateBack = navigateBack
                )
            }
        }
    }
}

@Composable
fun DialogView(
    markRes : Int,
    dialogData : PopUpDialogUiModel,
    onCloseDialog : () -> Unit,
    prepareForPhotoCapture : () -> Unit,
    onOpenGallery : () -> Unit,
    navigateBack : () -> Unit
) {
    PopUpDialog(
        title = dialogData.title,
        text = dialogData.text,
        imageId = dialogData.imageId ?: markRes,
        textConfirm = dialogData.textConfirm,
        onConfirmation = {
            onCloseDialog()
            if (dialogData.shouldPrepareForCapture) {
                prepareForPhotoCapture()
                return@PopUpDialog
            }
            if (dialogData.shouldOpenGallery) {
                onOpenGallery()
            }
            if (dialogData.shouldNavigateBack) {
                navigateBack()
            }
        },
        textDismiss = dialogData.textDismiss,
        onDismiss = {
            if (dialogData.haveDismiss) {
                onCloseDialog()
                if (dialogData.shouldOpenGallery) {
                    onOpenGallery()
                }
                if (dialogData.shouldNavigateBack) {
                    navigateBack()
                }
            }
        }
    )
}
