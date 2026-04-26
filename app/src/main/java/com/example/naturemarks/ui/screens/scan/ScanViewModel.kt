package com.example.naturemarks.ui.screens.scan

import android.Manifest
import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.graphics.createBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.naturemarks.R
import com.example.naturemarks.app.NatureMarksApplication
import com.example.naturemarks.data.postmark.MarkLocation
import com.example.naturemarks.data.postmark.PostmarkRepository
import com.example.naturemarks.data.postmark.PostmarkModel
import com.example.naturemarks.ui.screens.scan.camera.CameraMode
import com.example.naturemarks.util.BitmapHelper
import com.example.naturemarks.util.MarkImageHelper
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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
    }
    private val _uiState = MutableStateFlow(ScanUiState(isLoading = false))
    val uiState: StateFlow<ScanUiState> = _uiState

    private val _event = MutableSharedFlow<ScanEvent>()
    val event = _event.asSharedFlow()

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

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
        checkIfWithinRadius(mark.location){ isInside ->
            if (isInside){
                saveMark(mark)
            }
            else {
                _uiState.update {
                    it.copy(showLocationErrorDialog = true)
                }
            }
        }
    }

    @RequiresPermission(allOf = [
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ])
    fun checkIfWithinRadius(
        markLocation: MarkLocation,
        radiusMeters: Float = 10f,
        onResult: (Boolean) -> Unit
    ) {
        val locationRequest = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        fusedLocationClient.getCurrentLocation(locationRequest, null)
            .addOnSuccessListener { currentLocation ->
                currentLocation?.let {
                    val targetLocation = Location("").apply {
                        latitude = markLocation.latitude
                        longitude = markLocation.longitude
                    }
                    val distance =
                        Location("").apply{latitude = 41.760278
                        longitude = 23.434722}
                        .distanceTo(targetLocation)
                    val result = distance <= radiusMeters + it.accuracy

                    Log.d("LOCATION", "${it.latitude}, ${it.longitude}")
                    onResult(result)
                } ?: onResult(false)
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
            val safeBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val markBitmap = BitmapHelper.getBitmapFromImage(getApplication(), markRes)
            val finalImage = overlayMark(safeBitmap, markBitmap)
            finalImage?.let {
                val uri = savePhotoToGallery(getApplication(), finalImage)
                uri?.let {
                    savePhotoToMemory(uiState.value.memoryId ?: return@let, it.toString())
                    _uiState.update { it.copy(isLoading = false) }
                    withContext(Dispatchers.Main) {
                        _event.emit(ScanEvent.NavigateToGallery)
                    }
                }
            }
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

    fun overlayMark(
        photo: Bitmap,
        mark: Bitmap
    ): Bitmap? {
        val result = photo.config?.let {
            createBitmap(photo.width, photo.height, it)
        }

        val canvas = result?.let { Canvas(it) }
        canvas?.drawBitmap(photo, 0f, 0f, null)

        // Position postmark
        val left = photo.width - mark.width - 32f
        val top = photo.height - mark.height - 32f

        canvas?.drawBitmap(mark, left, top, null)

        return result
    }

    fun savePhotoToGallery(context: Context, bitmap: Bitmap): Uri? {
        val filename = "Mark_${System.currentTimeMillis()}.jpg"

        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/NatureMarks")
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            val stream = resolver.openOutputStream(it)
            stream?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            }
        }
        return uri
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
    }

    fun hideDialog(){
        _uiState.update { it.copy(showDialog = false) }
    }
}

class ScanViewModelFactory(
    private val application: NatureMarksApplication,
    private val postmarkRepository: PostmarkRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScanViewModel(application, postmarkRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}