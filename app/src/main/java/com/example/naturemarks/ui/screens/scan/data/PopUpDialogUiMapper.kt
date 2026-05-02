package com.example.naturemarks.ui.screens.scan.data

import android.content.Context
import com.example.naturemarks.R
import com.example.naturemarks.ui.screens.scan.ScanViewModel

class PopUpDialogUiMapper(private val context: Context) {
    fun map(dialogType: ScanViewModel.DialogType?): PopUpDialogUiModel? =
        when (dialogType) {
            ScanViewModel.DialogType.SUCCESS -> prepareSuccessDialog()
            ScanViewModel.DialogType.DUPLICATE -> prepareDuplicateDialog()
            ScanViewModel.DialogType.LOCATION -> prepareLocationDialog()
            ScanViewModel.DialogType.ERROR -> prepareErrorDialog()
            else -> null
        }

    private fun prepareSuccessDialog(): PopUpDialogUiModel =
        PopUpDialogUiModel(
            title = context.getString(R.string.dialog_new_mark_title),
            text = context.getString(R.string.dialog_new_mark_description) + " " + context.getString(R.string.camera_emoji),
            imageId = R.drawable.mark,
            textConfirm = context.getString(R.string.take_photo),
            textDismiss = context.getString(R.string.skip),
            haveDismiss = true,
            shouldPrepareForCapture = true,
            shouldOpenGallery = true
        )

    private fun prepareDuplicateDialog(): PopUpDialogUiModel =
        PopUpDialogUiModel(
            title = context.getString(R.string.dialog_duplicate_title),
            text = context.getString(R.string.dialog_duplicate_description),
            textConfirm = context.getString(R.string.gallery),
            shouldOpenGallery = true
        )

    private fun prepareLocationDialog(): PopUpDialogUiModel =
        PopUpDialogUiModel(
            title = context.getString(R.string.dialog_duplicate_title),
            text = context.getString(R.string.dialog_location_error_description),
            imageId = R.drawable.mark,
            textConfirm = context.getString(R.string.ok),
            shouldNavigateBack = true
        )

    private fun prepareErrorDialog(): PopUpDialogUiModel =
        PopUpDialogUiModel(
            title = context.getString(R.string.dialog_invalid_qr_title),
            text = context.getString(R.string.dialog_invalid_qr_description),
            imageId = R.drawable.question_mark,
            textConfirm = context.getString(R.string.close),
            shouldNavigateBack = true
        )

}