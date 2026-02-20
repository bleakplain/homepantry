package com.homepantry.viewmodel

import androidx.lifecycle.SavedStateHandle
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
 * 筛选对话框视图模型
 */
class FilterDialogViewModel(
    private val recipeFilterId: String?,
    private val recipeFilterRepository: RecipeFilterRepository
) : ViewModel() {

    companion object {
        private const val TAG = "FilterDialogViewModel"
    }

    private val _uiState = MutableStateFlow(FilterDialogUiState())
    val uiState: StateFlow<FilterDialogUiState> = _uiState.asStateFlow()

    init {
        Logger.d(TAG, "FilterDialogViewModel init for filter: $recipeFilterId")
        loadFilter()
    }

    /**
     * 加载筛选器
     */
    private fun loadFilter() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("loadFilter") {
                Logger.enter("FilterDialogViewModel.loadFilter", recipeFilterId)

                if (recipeFilterId != null) {
                    recipeFilterRepository.getFilterById(recipeFilterId)
                        .collect { filter ->
                            if (filter != null) {
                                val criteria = RecipeFilter.toCriteria(filter)
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        criteria = criteria
                                    )
                                }
                                Logger.d(TAG, "加载筛选器成功：${filter.id}")
                            } else {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        error = "筛选器不存在"
                                    )
                                }
                                Logger.e(TAG, "筛选器不存在：$recipeFilterId")
                            }
                        }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                    Logger.d(TAG, "新建筛选器模式")
                }

                Logger.exit("FilterDialogViewModel.loadFilter")
            }
        }
    }

    /**
     * 应用筛选
     */
    fun applyFilter(criteria: RecipeFilterCriteria) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("applyFilter") {
                Logger.enter("FilterDialogViewModel.applyFilter")

                recipeFilterRepository.applyFilter(criteria)
                    .collect { recipes ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                recipes = recipes
                            )
                        }
                        Logger.d(TAG, "应用筛选成功：${recipes.size} 个菜谱")
                    }

                Logger.exit("FilterDialogViewModel.applyFilter")
            }
        }
    }

    /**
     * 保存筛选器
     */
    fun saveFilter(criteria: RecipeFilterCriteria) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            PerformanceMonitor.recordMethodPerformance("saveFilter") {
                Logger.enter("FilterDialogViewModel.saveFilter", recipeFilterId)

                if (recipeFilterId != null) {
                    recipeFilterRepository.getFilterById(recipeFilterId)
                        .collect { filter ->
                            if (filter != null) {
                                val updatedFilter = filter.copy(
                                    cookingTimeMin = criteria.cookingTimeMin,
                                    cookingTimeMax = criteria.cookingTimeMax,
                                    difficultyMin = criteria.difficultyMin,
                                    difficultyMax = criteria.difficultyMax,
                                    includedIngredients = criteria.includedIngredients.toList(),
                                    excludedIngredients = criteria.excludedIngredients.toList(),
                                    categoryIds = criteria.categoryIds.toList()
                                )
                                recipeFilterRepository.updateFilter(updatedFilter)
                                    .onSuccess {
                                        _uiState.update {
                                            it.copy(
                                                isSaving = false,
                                                successMessage = "筛选器更新成功"
                                            )
                                        }
                                        Logger.d(TAG, "更新筛选器成功：${filter.id}")
                                    }
                                    .onFailure { e ->
                                        _uiState.update {
                                            it.copy(
                                                isSaving = false,
                                                error = "更新失败：${e.message}"
                                            )
                                        }
                                        Logger.e(TAG, "更新筛选器失败：${filter.id}", e)
                                    }
                            }
                        }
                } else {
                    recipeFilterRepository.createFilter(criteria)
                        .onSuccess { filter ->
                            _uiState.update {
                                it.copy(
                                    isSaving = false,
                                    successMessage = "筛选器创建成功"
                                )
                            }
                            Logger.d(TAG, "创建筛选器成功：${filter.id}")
                        }
                        .onFailure { e ->
                            _uiState.update {
                                it.copy(
                                    isSaving = false,
                                    error = "创建失败：${e.message}"
                                )
                            }
                            Logger.e(TAG, "创建筛选器失败", e)
                        }
                }

                Logger.exit("FilterDialogViewModel.saveFilter")
            }
        }
    }

    /**
     * 删除筛选器
     */
    fun deleteFilter() {
        if (recipeFilterId != null) {
            viewModelScope.launch {
                _uiState.update { it.copy(isDeleting = true) }

                PerformanceMonitor.recordMethodPerformance("deleteFilter") {
                    Logger.enter("FilterDialogViewModel.deleteFilter", recipeFilterId)

                    recipeFilterRepository.deleteFilter(recipeFilterId)
                        .onSuccess {
                            _uiState.update {
                                it.copy(
                                    isDeleting = false,
                                    isDeleted = true,
                                    successMessage = "筛选器删除成功"
                                )
                            }
                            Logger.d(TAG, "删除筛选器成功：$recipeFilterId")
                        }
                        .onFailure { e ->
                            _uiState.update {
                                it.copy(
                                    isDeleting = false,
                                    error = "删除失败：${e.message}"
                                )
                            }
                            Logger.e(TAG, "删除筛选器失败：$recipeFilterId", e)
                        }

                    Logger.exit("FilterDialogViewModel.deleteFilter")
                }
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

    /**
     * 关闭对话框
     */
    fun onDismiss() {
        Logger.d(TAG, "关闭筛选对话框")
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "FilterDialogViewModel onCleared")
    }
}

/**
 * 筛选对话框 UI 状态
 */
data class FilterDialogUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
    val criteria: RecipeFilterCriteria = RecipeFilterCriteria(),
    val recipes: List<Recipe> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)
