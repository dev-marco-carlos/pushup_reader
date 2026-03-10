package com.pushup.notificationreader.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pushup.notificationreader.data.AppDatabase
import com.pushup.notificationreader.data.NotificationEntity
import com.pushup.notificationreader.data.NotificationRepository
import com.pushup.notificationreader.util.TextExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NotificationRepository
    private val textExporter: TextExporter

    private val _notifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    val notifications: StateFlow<List<NotificationEntity>> = _notifications

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _exportResult = MutableStateFlow<ExportResult?>(null)
    val exportResult: StateFlow<ExportResult?> = _exportResult

    init {
        val db = AppDatabase.getInstance(application)
        repository = NotificationRepository(db.notificationDao())
        textExporter = TextExporter(application)

        viewModelScope.launch {
            repository.allNotifications
                .catch { e ->
                    _error.value = "Erro ao carregar notificacoes: ${e.message}"
                }
                .collect { list ->
                    _notifications.value = list
                }
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            try {
                repository.deleteAll()
            } catch (e: Exception) {
                _error.value = "Erro ao limpar notificacoes: ${e.message}"
            }
        }
    }

    fun exportToTextFile() {
        viewModelScope.launch {
            try {
                val notifications = repository.getAll()
                val filePath = textExporter.export(notifications)
                _exportResult.value = ExportResult(success = true, path = filePath)
            } catch (e: Exception) {
                _exportResult.value = ExportResult(success = false, errorMessage = e.message)
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearExportResult() {
        _exportResult.value = null
    }

    data class ExportResult(
        val success: Boolean,
        val path: String? = null,
        val errorMessage: String? = null
    )
}
