package com.ilustris.sagai.features.settings.ui.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.database.model.AIAuditLog
import com.ilustris.sagai.features.settings.domain.audit.repository.AIAuditLogFilters
import com.ilustris.sagai.features.settings.domain.audit.usecase.AIAuditLogUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

sealed interface AuditLogListItem {
    data class DateHeader(
        val date: String,
    ) : AuditLogListItem

    data class LogEntry(
        val log: AIAuditLog,
    ) : AuditLogListItem
}

@HiltViewModel
class AIAuditLogViewModel
    @Inject
    constructor(
        private val aiAuditLogUseCase: AIAuditLogUseCase,
    ) : ViewModel() {
        private val loadedLogs = mutableListOf<AIAuditLog>()
        private var currentOffset = 0
        private var observationJob: Job? = null

        private val filters = MutableStateFlow(AIAuditLogFilters())

        private val _statusFilter = MutableStateFlow<String?>(null)
        val statusFilter: StateFlow<String?> = _statusFilter.asStateFlow()

        private val _dataTypeFilter = MutableStateFlow<String?>(null)
        val dataTypeFilter: StateFlow<String?> = _dataTypeFilter.asStateFlow()

        private val _modelFilter = MutableStateFlow<String?>(null)
        val modelFilter: StateFlow<String?> = _modelFilter.asStateFlow()

        private val _availableDataTypes = MutableStateFlow<List<String>>(emptyList())
        val availableDataTypes: StateFlow<List<String>> = _availableDataTypes.asStateFlow()

        private val _availableModels = MutableStateFlow<List<String>>(emptyList())
        val availableModels: StateFlow<List<String>> = _availableModels.asStateFlow()

        private val _listItems = MutableStateFlow<List<AuditLogListItem>>(emptyList())
        val listItems: StateFlow<List<AuditLogListItem>> = _listItems.asStateFlow()

        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

        private val _isLoadingMore = MutableStateFlow(false)
        val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

        private val _hasMore = MutableStateFlow(true)
        val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

        private val _loadingSuggestionId = MutableStateFlow<Int?>(null)
        val loadingSuggestionId: StateFlow<Int?> = _loadingSuggestionId.asStateFlow()

        private val _pipelineInsight = MutableStateFlow<String?>(null)
        val pipelineInsight = _pipelineInsight.asStateFlow()

        private val _isPipelineInsightLoading = MutableStateFlow(false)
        val isPipelineInsightLoading: StateFlow<Boolean> = _isPipelineInsightLoading.asStateFlow()

        fun setScreenActive(active: Boolean) {
            observationJob?.cancel()
            observationJob = null

            if (!active) {
                releaseLoadedLogs()
                return
            }

            observationJob =
                viewModelScope.launch {
                    refreshFilterOptions()
                    refreshLogs()

                    aiAuditLogUseCase
                        .observeLogCount()
                        .drop(1)
                        .onEach { refreshLogs() }
                        .launchIn(this)
                }
    }

    fun loadMore() {
            if (_isLoading.value || _isLoadingMore.value || !_hasMore.value) return

            viewModelScope.launch {
                _isLoadingMore.value = true
                aiAuditLogUseCase
                    .getLogsPage(filters.value, PAGE_SIZE, currentOffset)
                    .onSuccessAsync { page ->
                        appendPage(page)
                    }
                _isLoadingMore.value = false
            }
        }

        fun clearLogs() {
            viewModelScope.launch {
                aiAuditLogUseCase.clearLogs()
                _pipelineInsight.emit(null)
                releaseLoadedLogs()
                refreshFilterOptions()
            }
        }

        fun updateStatusFilter(status: String?) {
            if (_statusFilter.value == status) return
            _statusFilter.value = status
            applyFiltersAndRefresh()
        }

        fun updateDataTypeFilter(dataType: String?) {
            if (_dataTypeFilter.value == dataType) return
            _dataTypeFilter.value = dataType
            applyFiltersAndRefresh()
        }

        fun updateModelFilter(model: String?) {
            if (_modelFilter.value == model) return
            _modelFilter.value = model
            applyFiltersAndRefresh()
        }

        fun requestSuggestion(log: AIAuditLog) {
            if (_loadingSuggestionId.value != null || log.suggestion != null) return
            _loadingSuggestionId.value = log.id

            viewModelScope.launch {
                aiAuditLogUseCase.generateSuggestion(log)
                refreshLogs()
                _loadingSuggestionId.value = null
            }
        }

        fun requestGlobalInsight() {
            if (_isPipelineInsightLoading.value) return
            _isPipelineInsightLoading.value = true
            viewModelScope.launch {
                aiAuditLogUseCase
                    .getRecentLogsForInsight(INSIGHT_LOG_LIMIT)
                    .onSuccessAsync { logs ->
                        aiAuditLogUseCase
                            .generateGlobalInsight(logs)
                            .onSuccessAsync {
                                _pipelineInsight.value = it
                                _isPipelineInsightLoading.value = false
                            }.onFailure {
                                _isPipelineInsightLoading.value = false
                            }
                    }.onFailure {
                        _isPipelineInsightLoading.value = false
                    }
            }
        }

        private fun applyFiltersAndRefresh() {
            filters.value =
                AIAuditLogFilters(
                    status = _statusFilter.value,
                    dataType = _dataTypeFilter.value,
                    model = _modelFilter.value,
                )
            viewModelScope.launch { refreshLogs() }
        }

        private suspend fun refreshLogs() {
            _isLoading.value = true
            aiAuditLogUseCase
                .getLogsPage(filters.value, PAGE_SIZE, 0)
                .onSuccessAsync { page ->
                    loadedLogs.clear()
                    appendPage(page, resetOffset = true)
                }
            _isLoading.value = false
        }

        private suspend fun refreshFilterOptions() {
            aiAuditLogUseCase.getDistinctDataTypes().onSuccessAsync { _availableDataTypes.value = it }
            aiAuditLogUseCase.getDistinctModels().onSuccessAsync { _availableModels.value = it }
        }

        private fun appendPage(
            page: List<AIAuditLog>,
            resetOffset: Boolean = false,
        ) {
            if (resetOffset) {
                currentOffset = 0
            }
            loadedLogs.addAll(page)
            currentOffset += page.size
            _hasMore.value = page.size == PAGE_SIZE
            publishListItems()
        }

        private fun publishListItems() {
            if (loadedLogs.isEmpty()) {
                _listItems.value = emptyList()
                return
            }

            val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val items = mutableListOf<AuditLogListItem>()
            var currentDate: String? = null

            loadedLogs.forEach { log ->
                val dateStr = dateFormatter.format(Date(log.timestamp))
                if (dateStr != currentDate) {
                    currentDate = dateStr
                    items.add(AuditLogListItem.DateHeader(dateStr))
                }
                items.add(AuditLogListItem.LogEntry(log))
            }

        _listItems.value = items
    }

    private fun releaseLoadedLogs() {
        loadedLogs.clear()
        currentOffset = 0
        _hasMore.value = true
        _listItems.value = emptyList()
    }

    private companion object {
        const val PAGE_SIZE = 40
        const val INSIGHT_LOG_LIMIT = 50
    }
    }
