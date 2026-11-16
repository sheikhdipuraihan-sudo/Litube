package com.hhst.youtubelite.ui.nativehome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NativeCatalogState(
    val query: String = "",
    val items: List<NativeVideoCard> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
)

class NativeCatalogViewModel(
    private val repository: NativeCatalogRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(NativeCatalogState())
    val state: StateFlow<NativeCatalogState> = _state.asStateFlow()

    fun search(query: String) {
        val normalized = query.trim()
        if (normalized.isEmpty()) return
        _state.value = _state.value.copy(query = normalized, loading = true, error = null)
        viewModelScope.launch {
            runCatching { repository.search(normalized) }
                .onSuccess { items ->
                    _state.value = NativeCatalogState(query = normalized, items = items)
                }
                .onFailure { failure ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = failure.message ?: "Unable to load results",
                    )
                }
        }
    }
}
