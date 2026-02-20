package com.homepantry.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homepantry.data.entity.CookingRecord
import com.homepantry.data.repository.CookingRecordRepository
import com.homepantry.utils.Logger
import com.homepantry.utils.PerformanceMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 烹饪记录视图模型
 */
class CookingRecordViewModel(
    private val recipeId: String,
    private val cookingRecordRepository: CookingRecordRepository
) : ViewModel() {

    companion object {
        private const val TAG = "CookingRecordViewModel"
    }

    private val _uiState = MutableStateFlow(CookingRecordUiState())
    val uiState: StateFlow<CookingRecordUiState> = _uiState.asStateFlow()

    init {
        Logger.d(TAG, "CookingRecordViewModel init for recipe: $recipeId")
        loadCookingRecords()
    }

    /**
     * 加载烹饪记录
     */
    private fun loadCookingRecords() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("loadCookingRecords") {
                Logger.enter("CookingRecordViewModel.loadCookingRecords", recipeId)

                cookingRecordRepository.getCookingRecordsByRecipe(recipeId)
                    .collect { records ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                records = records
                            )
                        }
                        Logger.d(TAG, "加载烹饪记录成功：${records.size} 个")
                    }

                Logger.exit("CookingRecordViewModel.loadCookingRecords")
            }
        }
    }

    /**
     * 创建烹饪记录
     */
    fun createCookingRecord(
        cookingDate: Long,
        cookingTime: Int,
        notes: String?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("createCookingRecord") {
                Logger.enter("CookingRecordViewModel.createCookingRecord", recipeId, cookingDate)

                val record = CookingRecord(
                    id = "cooking_record_${java.util.UUID.randomUUID().toString()}",
                    recipeId = recipeId,
                    cookingDate = cookingDate,
                    cookingTime = cookingTime,
                    notes = notes,
                    createdAt = System.currentTimeMillis()
                )

                cookingRecordRepository.createCookingRecord(record)
                    .onSuccess { createdRecord ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                records = it.records + createdRecord,
                                successMessage = "记录创建成功"
                            )
                        }
                        Logger.d(TAG, "烹饪记录创建成功：${createdRecord.id}")
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "创建失败：${e.message}"
                            )
                        }
                        Logger.e(TAG, "烹饪记录创建失败", e)
                    }

                Logger.exit("CookingRecordViewModel.createCookingRecord")
            }
        }
    }

    /**
     * 删除烹饪记录
     */
    fun deleteCookingRecord(recordId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("deleteCookingRecord") {
                Logger.enter("CookingRecordViewModel.deleteCookingRecord", recordId)

                cookingRecordRepository.deleteCookingRecord(recordId)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                records = it.records.filter { it.id != recordId },
                                successMessage = "删除成功"
                            )
                        }
                        Logger.d(TAG, "烹饪记录删除成功：$recordId")
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "删除失败：${e.message}"
                            )
                        }
                        Logger.e(TAG, "烹饪记录删除失败：$recordId", e)
                    }

                Logger.exit("CookingRecordViewModel.deleteCookingRecord")
            }
        }
    }

    /**
     * 清除错误消息
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * 清除成功消息
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "CookingRecordViewModel onCleared")
    }
}

/**
 * 烹饪记录 UI 状态
 */
data class CookingRecordUiState(
    val isLoading: Boolean = false,
    val records: List<CookingRecord> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)
