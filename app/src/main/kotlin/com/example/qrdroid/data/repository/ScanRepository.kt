package com.example.qrdroid.data.repository

import android.content.Context
import com.example.qrdroid.data.local.ScanDatabaseHelper
import com.example.qrdroid.data.model.ScanItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class ScanRepository(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val dbHelper = ScanDatabaseHelper(context)
    private val _scansFlow = MutableStateFlow<List<ScanItem>>(emptyList())
    val scansFlow: Flow<List<ScanItem>> = _scansFlow.asStateFlow()

    init {
        // Initial load
        refresh()
    }

    private fun refresh() {
        val list = dbHelper.getAllScans()
        _scansFlow.value = list
    }

    suspend fun insertScan(item: ScanItem): Long = withContext(ioDispatcher) {
        val insertedId = dbHelper.insertScan(item)
        refresh()
        insertedId
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) = withContext(ioDispatcher) {
        dbHelper.toggleFavorite(id, isFavorite)
        refresh()
    }

    suspend fun deleteScan(id: Long) = withContext(ioDispatcher) {
        dbHelper.deleteScan(id)
        refresh()
    }

    suspend fun clearAll() = withContext(ioDispatcher) {
        dbHelper.clearAllScans()
        refresh()
    }

    suspend fun search(query: String): List<ScanItem> = withContext(ioDispatcher) {
        if (query.isBlank()) {
            dbHelper.getAllScans()
        } else {
            dbHelper.searchScans(query)
        }
    }
}
