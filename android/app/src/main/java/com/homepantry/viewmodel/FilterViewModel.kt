package com.homepantry.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homepantry.data.entity.RecipeFilter
import com.homepantry.data.entity.RecipeFilterCriteria
import com.homepantry.data.repository.RecipeFilterRepository
import com.homepantry.utils.Logger
import com.homepantry.utils.PerformanceMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 菜谱筛选视图模型
 */
class FilterViewModel(
    private val recipeFilterRepository: RecipeFilterRepository
) : ViewModel() {

    companion object {
        private const val TAG = "FilterViewModel"
    }

    private val _uiState = MutableStateFlow(FilterUiState())
    val uiState: StateFlow<FilterUiState> = _uiState.asStateFlow()

    init {
        Logger.d(TAG, "FilterViewModel init")
        loadFilters()
    }

    /**
     * 加载所有筛选器
     */
    private fun loadFilters() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("loadFilters") {
                Logger.enter("FilterViewModel.loadFilters")

                recipeFilterRepository.getAllFilters()
                    .collect { filters ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                filters = filters
                            )
                        }
                    }

                Logger.exit("FilterViewModel.loadFilters")
            }
        }
    }

    /**
     * 应用筛选条件
     */
    fun applyFilter(criteria: RecipeFilterCriteria) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("applyFilter") {
                Logger.enter("FilterViewModel.applyFilter")

                recipeFilterRepository.applyFilter(criteria)
                    .collect { recipes ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                filteredRecipes = recipes
                            )
                        }
                    }

                Logger.exit("FilterViewModel.applyFilter")
            }
        }
    }

    /**
     * 清除筛选
     */
    fun clearFilters() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("clearFilters") {
                Logger.enter("FilterViewModel.clearFilters")

                recipeFilterRepository.clearAllFilters()
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                filters = emptyList(),
                                filteredRecipes = emptyList()
                            )
                        }
                        Logger.d(TAG, "清除所有筛选器成功")
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "清除失败：${e.message}"
                            )
                        }
                        Logger.e(TAG, "清除所有筛选器失败", e)
                    }

                Logger.exit("FilterViewModel.clearFilters")
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
        Logger.d(TAG, "FilterViewModel onCleared")
    }
}

/**
 * 菜谱筛选 UI 状态
 */
data class FilterUiState(
    val isLoading: Boolean = false,
    val filters: List<RecipeFilter> = emptyList(),
    val filteredRecipes: List<Recipe> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)
