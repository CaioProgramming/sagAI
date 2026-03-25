package com.ilustris.sagai.features.settings.ui.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.database.model.AIAuditLog
import com.ilustris.sagai.features.settings.domain.audit.usecase.AIAuditLogUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AIAuditLogViewModel
    @Inject
    constructor(
        private val aiAuditLogUseCase: AIAuditLogUseCase,
    ) : ViewModel() {
        private val _logs = MutableStateFlow<List<AIAuditLog>>(emptyList())

        private val _statusFilter = MutableStateFlow<String?>(null)
        val statusFilter: StateFlow<String?> = _statusFilter.asStateFlow()

        private val _dataTypeFilter = MutableStateFlow<String?>(null)
        val dataTypeFilter: StateFlow<String?> = _dataTypeFilter.asStateFlow()

        private val _modelFilter = MutableStateFlow<String?>(null)
        val modelFilter: StateFlow<String?> = _modelFilter.asStateFlow()

        val availableDataTypes: StateFlow<List<String>> =
            _logs
                .map { logs -> logs.map { it.dataType }.distinct() }
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        val availableModels: StateFlow<List<String>> =
            _logs
                .map { logs -> logs.map { it.model }.distinct() }
                .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        private val _loadingSuggestionId = MutableStateFlow<Int?>(null)
        val loadingSuggestionId: StateFlow<Int?> = _loadingSuggestionId.asStateFlow()

        private val _pipelineInsight =
            MutableStateFlow<RequestResult<String>>(RequestResult.Success(""))
        val pipelineInsight: StateFlow<RequestResult<String>> = _pipelineInsight.asStateFlow()

        private val _isPipelineInsightLoading = MutableStateFlow(false)
        val isPipelineInsightLoading: StateFlow<Boolean> = _isPipelineInsightLoading.asStateFlow()

        val filteredLogs: StateFlow<List<AIAuditLog>> =
            combine(
                _logs,
                _statusFilter,
                _dataTypeFilter,
                _modelFilter,
            ) { logs, status, dataType, model ->
                logs.filter {
                    (status == null || it.status == status) &&
                        (dataType == null || it.dataType == dataType) &&
                        (model == null || it.model == model)
                }
            }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        init {
            viewModelScope.launch {
                aiAuditLogUseCase
                    .getRecentLogs(100)
                    .catch { e -> e.printStackTrace() }
                    .collect { fetchedLogs ->
                        _logs.value = fetchedLogs
                    }
            }
        }

        fun clearLogs() {
            viewModelScope.launch {
                aiAuditLogUseCase.clearLogs()
            }
        }

    fun updateStatusFilter(status: String?) {
            _statusFilter.value = status
        }

        fun updateDataTypeFilter(dataType: String?) {
            _dataTypeFilter.value = dataType
        }

        fun updateModelFilter(model: String?) {
            _modelFilter.value = model
        }

        fun requestSuggestion(log: AIAuditLog) {
            if (_loadingSuggestionId.value != null || log.suggestion != null) return
            _loadingSuggestionId.value = log.id

            viewModelScope.launch {
                aiAuditLogUseCase.generateSuggestion(log)
                _loadingSuggestionId.value = null
            }
        }

        fun requestGlobalInsight() {
            if (_isPipelineInsightLoading.value) return
            _isPipelineInsightLoading.value = true
            viewModelScope.launch {
                aiAuditLogUseCase
                    .generateGlobalInsight(_logs.value)
                    .onEach {
                        _pipelineInsight.value = it
                    _isPipelineInsightLoading.value = false
                }.collect {}
        }
    }
    }
