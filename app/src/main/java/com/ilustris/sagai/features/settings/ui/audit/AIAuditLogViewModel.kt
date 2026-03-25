package com.ilustris.sagai.features.settings.ui.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.database.model.AIAuditLog
import com.ilustris.sagai.core.database.source.AIAuditLogDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AIAuditLogViewModel
    @Inject
    constructor(
        private val aiAuditLogDao: AIAuditLogDao,
    ) : ViewModel() {
        private val _logs = MutableStateFlow<List<AIAuditLog>>(emptyList())

        private val _statusFilter = MutableStateFlow<String?>(null)
        val statusFilter: StateFlow<String?> = _statusFilter.asStateFlow()

        val filteredLogs: StateFlow<List<AIAuditLog>> =
            combine(_logs, _statusFilter) { logs, filter ->
                if (filter == null) logs else logs.filter { it.status == filter }
            }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        init {
            viewModelScope.launch {
                aiAuditLogDao
                    .getRecentLogs(100)
                    .catch { e -> e.printStackTrace() }
                    .collect { fetchedLogs ->
                        _logs.value = fetchedLogs
                    }
            }
        }

        fun clearLogs() {
            viewModelScope.launch {
                aiAuditLogDao.clearLogs()
            }
        }

        fun updateFilter(status: String?) {
            _statusFilter.value = status
        }
    }
