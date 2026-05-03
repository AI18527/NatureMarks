package com.example.naturemarks.ui.screens.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.naturemarks.data.postmark.PostmarkRepository
import com.example.naturemarks.ui.model.PostmarkUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GalleryViewModel(
    private val postmarkRepository: PostmarkRepository
): ViewModel() {

    data class GalleryUiState(
        val isLoading: Boolean = false,
        val marks: List<PostmarkUiModel> = emptyList(),
        val groupedByCity: Boolean = false
    )

    data class CityGroup(
        val city: String,
        val items: List<PostmarkUiModel>
    )

    private val _uiState = MutableStateFlow(
        GalleryUiState(isLoading = true)
    )
    val uiState: StateFlow<GalleryUiState> = _uiState

    init {
        loadMarks()
    }

    fun loadMarks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val marks = postmarkRepository.getAllMarks()?.reversed() ?: emptyList()

            _uiState.update { it.copy(isLoading = false, marks = marks) }
        }
    }

    fun toggleGroupByCity() {
        _uiState.update {
            it.copy(groupedByCity = !it.groupedByCity)
        }
    }

    fun getGroupedMarks(): List<CityGroup> {
        return _uiState.value.marks
            .groupBy { it.city }
            .map { (city, marks) ->
                CityGroup(
                    city = city,
                    items = marks
                )
            }
    }
}

class GalleryViewModelFactory(
    private val repository: PostmarkRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GalleryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GalleryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


