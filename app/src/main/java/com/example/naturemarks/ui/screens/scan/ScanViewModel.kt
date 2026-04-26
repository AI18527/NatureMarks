package com.example.naturemarks.ui.screens.scan

import android.Manifest
import android.app.Application
import android.graphics.Bitmap
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

class ScanViewModel(
    application: Application,
    private val postmarkRepository: PostmarkRepository,
    private val locationRepository: LocationRepository,
    private val mediaStorageRepository: MediaStorageRepository
) : AndroidViewModel(application) {

    data class ScanUiState(
        val isLoading: Boolean = false,
        val showDialog: Boolean = false,
        val showDuplicateErrorDialog: Boolean = false,
        val showLocationErrorDialog: Boolean = false,
        val mark: PostmarkModel? = null,
        val memoryId: Int? = null,
        val markRes: Int = R.drawable.mark,
        val cameraMode: CameraMode = CameraMode.SCAN
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
                    it.copy(showLocationErrorDialog = true)
                }
            }
        }
    }

    fun prepareForPhotoCapture(){
        _uiState.update {
            it.copy(
                cameraMode = CameraMode.CAPTURE,
                showDialog = false
            )
        }
    }

    fun onPhotoCaptured(bitmap: Bitmap) {
        val markRes = _uiState.value.markRes
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val safeBitmap = bitmap.toMutableBitmap()
            val markBitmap = BitmapHelper.drawableToBitmap(getApplication(), markRes)
            val finalImage = BitmapHelper.overlayMark(safeBitmap, markBitmap)
            finalImage?.let {
                val uri = mediaStorageRepository.savePhotoToGallery(finalImage)
                uri?.let {
                    savePhotoToMemory(uiState.value.memoryId ?: return@let, it.toString())
                    _uiState.update { it.copy(isLoading = false) }
                    withContext(Dispatchers.Main) {
                        _event.emit(ScanEvent.NavigateToGallery)
                    }
                }
            } ?: _event.emit(ScanEvent.NavigateToGallery)
        }
    }

    fun saveMark(mark: PostmarkModel) {
        viewModelScope.launch {
            try {
                postmarkRepository.addMark(mark)
                addNewMemory(mark.imageId)
                _uiState.update { it.copy(showDialog = true) }
            } catch (e: IllegalArgumentException) {
                _uiState.update { it.copy(showDuplicateErrorDialog = true) }
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

    fun hideDuplicateErrorDialog(){
        _uiState.update { it.copy(showDuplicateErrorDialog = false) }
    }

    fun hideLocationErrorDialog(){
        _uiState.update { it.copy(showLocationErrorDialog = false) }
        viewModelScope.launch {
            _event.emit(ScanEvent.NavigateBack)
        }
    }

    fun hideDialog(){
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