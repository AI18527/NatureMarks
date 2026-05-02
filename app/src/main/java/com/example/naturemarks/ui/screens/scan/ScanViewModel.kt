package com.example.naturemarks.ui.screens.scan

import android.Manifest
import android.app.Application
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.naturemarks.R
import com.example.naturemarks.app.NatureMarksApplication
import com.example.naturemarks.data.location.LocationRepository
import com.example.naturemarks.data.postmark.PostmarkMapper.toLocation
import com.example.naturemarks.data.postmark.PostmarkRepository
import com.example.naturemarks.data.postmark.PostmarkModel
import com.example.naturemarks.data.storage.MediaStorageRepository
import com.example.naturemarks.ui.screens.scan.camera.CameraMode
import com.example.naturemarks.ui.screens.scan.data.PopUpDialogUiModel
import com.example.naturemarks.util.BitmapHelper
import com.example.naturemarks.util.BitmapHelper.toMutableBitmap
import com.example.naturemarks.util.MarkImageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

class ScanViewModel(
    application: Application,
    private val postmarkRepository: PostmarkRepository,
    private val locationRepository: LocationRepository,
    private val mediaStorageRepository: MediaStorageRepository
) : AndroidViewModel(application) {

    enum class DialogType {
        SUCCESS, DUPLICATE, LOCATION, ERROR
    }
    data class ScanUiState(
        val isLoading: Boolean = false,
        val showDialog : Boolean = false,
        val dialogType : DialogType? = null,
        val mark: PostmarkModel? = null,
        val memoryId: Int? = null,
        val markRes: Int = R.drawable.mark,
        val cameraMode: CameraMode = CameraMode.SCAN,
        val popUpDialog : PopUpDialogUiModel? = null
    )

    sealed class ScanEvent {
        data object NavigateToGallery : ScanEvent()
        data object NavigateBack : ScanEvent()
    }
    private val _uiState = MutableStateFlow(ScanUiState(isLoading = false))
    val uiState: StateFlow<ScanUiState> = _uiState

    private val _event = MutableSharedFlow<ScanEvent>()
    val event = _event.asSharedFlow()

    @RequiresPermission(allOf = [
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ])
    fun onQrScanned(raw: String) {
        if (_uiState.value.mark != null) return

        try {
            Json.decodeFromString<PostmarkModel>(raw)
        } catch (e: Exception) {
            _uiState.update { it.copy(showDialog = true, dialogType = DialogType.ERROR) }
            return
        }
        val mark = Json.decodeFromString<PostmarkModel>(raw)
        _uiState.update {
            it.copy(
                mark = mark,
                markRes = MarkImageHelper.getMarkImage(mark.imageId)
            )
        }
        viewModelScope.launch {
            locationRepository.getUserLocation()?.let { userLocation ->
                val isInside = locationRepository.isWithinRadius(
                    userLocation = userLocation,
                    targetLocation = mark.location.toLocation(),
                    radiusMeters = 10f
                )

                if (isInside) saveMark(mark)
                else _uiState.update {
                    it.copy(showDialog = true, dialogType = DialogType.LOCATION)
                }
            }
        }
    }

    fun prepareForPhotoCapture(){
        _uiState.update {
            it.copy(
                cameraMode = CameraMode.CAPTURE
            )
        }
    }

    fun onPhotoCaptured(file: File) {
        val markRes = _uiState.value.markRes
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch(Dispatchers.IO) {

            val bitmap = BitmapHelper.fileToBitmap(getApplication(), file).toMutableBitmap()
            val markBitmap = BitmapHelper.drawableToBitmap(getApplication(), markRes)
            val finalImage = BitmapHelper.overlayMark(bitmap, markBitmap)

            val uri = mediaStorageRepository.savePhotoToGallery(finalImage)
            uri?.let {
                savePhotoToMemory(uiState.value.memoryId ?: return@let, it.toString())
                _uiState.update { it.copy(isLoading = false) }
            }
            _event.emit(ScanEvent.NavigateToGallery)
        }
    }

    fun saveMark(mark: PostmarkModel) {
        viewModelScope.launch {
            try {
                postmarkRepository.addMark(mark)
                addNewMemory(mark.imageId)
                _uiState.update { it.copy(showDialog = true, dialogType = DialogType.SUCCESS) }
            } catch (e: IllegalArgumentException) {
                _uiState.update { it.copy(showDialog = true, dialogType = DialogType.DUPLICATE) }
            }
        }
    }

    private suspend fun addNewMemory(markId: String) {
        withContext(Dispatchers.IO) {
            postmarkRepository.addNewMemoryForMark(markId)
        }
        _uiState.update { it.copy(memoryId = postmarkRepository.getMarkDetails(markId).memoryId) }
    }

    fun savePhotoToMemory(memoryId: Int, photoPath: String) {
        viewModelScope.launch {
            postmarkRepository.updateMarkMemoryPhoto(memoryId, photoPath)
        }
    }

    fun onCloseDialog(){
        _uiState.update { it.copy(showDialog = false) }
    }
}

class ScanViewModelFactory(
    private val application: NatureMarksApplication,
    private val postmarkRepository: PostmarkRepository,
    private val locationRepository: LocationRepository,
    private val mediaStorageRepository: MediaStorageRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScanViewModel(
                application,
                postmarkRepository,
                locationRepository,
                mediaStorageRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}