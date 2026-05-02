package com.example.naturemarks.ui.screens.markdetails

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.naturemarks.data.postmark.PostmarkRepository
import com.example.naturemarks.database.model.Postmark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MarkDetailsViewModel(
    private val postmarkRepository: PostmarkRepository,
    markId: String
): ViewModel() {
    data class MarkDetailsUiState(
        val postmark: Postmark? = null,
        val memoryId: Int? = null,
        val isDbLoading: Boolean = false,
        val photo: Uri? = null,
        val notes: String = "",
        val edit: Boolean = false
    )

    sealed class MarkDetailsEvent {
        data class SharePhoto(val uri: Uri) : MarkDetailsEvent()
    }
    private val _uiState = MutableStateFlow(MarkDetailsUiState(isDbLoading = true))
    val uiState: StateFlow<MarkDetailsUiState> = _uiState

    private val _event = MutableSharedFlow<MarkDetailsEvent>()
    val event = _event.asSharedFlow()

    init {
        loadMarkDetails(markId)
    }

    fun loadMarkDetails(markId: String) {
        viewModelScope.launch {
            val details = withContext(Dispatchers.IO) {
                postmarkRepository.getMarkDetails(markId)
            }

            _uiState.update {
                it.copy(
                    postmark = details.postmark,
                    memoryId = details.memoryId,
                    isDbLoading = false,
                    notes = details.notes ?: "",
                    photo = details.photoUri
                )
            }
        }
    }

    fun editNotes() {
        _uiState.update { it.copy(edit = true) }
    }

    fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun saveNotes(notes: String) {
        _uiState.update { it.copy(notes = notes, edit = false) }
        viewModelScope.launch {
            uiState.value.memoryId?.let {
                postmarkRepository.updateMarkMemoryNotes(it, notes)
            }
        }
    }

    fun onSharePhoto() {
        uiState.value.photo?.let {
            viewModelScope.launch {
                _event.emit(MarkDetailsEvent.SharePhoto(it))
            }
        }
    }
}

class MarkDetailsViewModelFactory(
    private val postmarkRepository: PostmarkRepository,
    private val markId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom( MarkDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return  MarkDetailsViewModel(postmarkRepository, markId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}