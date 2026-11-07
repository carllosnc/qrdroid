package com.example.qrdroid.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qrdroid.data.model.ScanItem
import com.example.qrdroid.data.model.ScanType
import com.example.qrdroid.data.repository.ScanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HistoryFilter(val label: String) {
    ALL("Todos"),
    FAVORITES("⭐ Favoritos"),
    QR_CODES("QR Codes"),
    BARCODES("Códigos de Barras"),
    PAYMENTS("PIX & Boletos")
}

data class HistoryUiState(
    val scans: List<ScanItem> = emptyList(),
    val searchQuery: String = "",
    val activeFilter: HistoryFilter = HistoryFilter.ALL,
    val selectedItem: ScanItem? = null
)

class HistoryViewModel(
    private val repository: ScanRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _activeFilter = MutableStateFlow(HistoryFilter.ALL)
    private val _selectedItem = MutableStateFlow<ScanItem?>(null)

    val uiState = combine(
        repository.scansFlow,
        _searchQuery,
        _activeFilter,
        _selectedItem
    ) { scans, query, filter, selected ->
        val filtered = scans.filter { item ->
            // Filter by search query
            val matchesQuery = query.isBlank() ||
                    item.displayValue.contains(query, ignoreCase = true) ||
                    item.rawValue.contains(query, ignoreCase = true) ||
                    item.formatName.contains(query, ignoreCase = true)

            // Filter by category
            val matchesFilter = when (filter) {
                HistoryFilter.ALL -> true
                HistoryFilter.FAVORITES -> item.isFavorite
                HistoryFilter.QR_CODES -> item.formatName.contains("QR", ignoreCase = true)
                HistoryFilter.BARCODES -> !item.formatName.contains("QR", ignoreCase = true)
                HistoryFilter.PAYMENTS -> item.valueType == ScanType.PIX || item.valueType == ScanType.BOLETO
            }

            matchesQuery && matchesFilter
        }

        HistoryUiState(
            scans = filtered,
            searchQuery = query,
            activeFilter = filter,
            selectedItem = selected
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterSelected(filter: HistoryFilter) {
        _activeFilter.value = filter
    }

    fun selectItem(item: ScanItem?) {
        _selectedItem.value = item
    }

    fun toggleFavorite(item: ScanItem) {
        viewModelScope.launch {
            repository.toggleFavorite(item.id, !item.isFavorite)
            if (_selectedItem.value?.id == item.id) {
                _selectedItem.value = _selectedItem.value?.copy(isFavorite = !item.isFavorite)
            }
        }
    }

    fun deleteItem(item: ScanItem) {
        viewModelScope.launch {
            repository.deleteScan(item.id)
            if (_selectedItem.value?.id == item.id) {
                _selectedItem.value = null
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}
