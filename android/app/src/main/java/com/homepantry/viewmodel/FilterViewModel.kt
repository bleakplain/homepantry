package com.homepantry.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homepantry.data.dao.RecipeFilterDao
import com.homepantry.data.entity.DifficultyLevel
import com.homepantry.data.entity.RecipeFilter
import com.homepantry.data.entity.RecipeFilterCriteria
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 筛选 ViewModel
 */
class FilterViewModel(private val recipeFilterDao: RecipeFilterDao) : ViewModel() {

    private val _uiState = MutableStateFlow(FilterUiState())
    val uiState: StateFlow<FilterUiState> = _uiState.asStateFlow()

    init {
        loadLatestFilter()
    }

    /**
     * 加载最新的筛选器
     */
    private fun loadLatestFilter() {
        viewModelScope.launch {
            recipeFilterDao.getLatestFilter()?.let { filter ->
                _uiState.update { it.copy(currentCriteria = filter.toCriteria()) }
            }
        }
    }

    /**
     * 应用烹饪时间范围
     */
    fun applyCookingTimeRange(min: Int?, max: Int?) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    currentCriteria = state.currentCriteria.copy(
                        cookingTimeMin = min,
                        cookingTimeMax = max
                    )
                )
            }
        }
    }

    /**
     * 应用难度范围
     */
    fun applyDifficultyRange(min: DifficultyLevel?, max: DifficultyLevel?) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    currentCriteria = state.currentCriteria.copy(
                        difficultyMin = min,
                        difficultyMax = max
                    )
                )
            }
        }
    }

    /**
     * 添加食材到包含列表
     */
    fun addIncludedIngredient(ingredientId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    currentCriteria = state.currentCriteria.copy(
                        includedIngredients = state.currentCriteria.includedIngredients + ingredientId
                    )
                )
            }
        }
    }

    /**
     * 从包含列表移除食材
     */
    fun removeIncludedIngredient(ingredientId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    currentCriteria = state.currentCriteria.copy(
                        includedIngredients = state.currentCriteria.includedIngredients - ingredientId
                    )
                )
            }
        }
    }

    /**
     * 添加食材到排除列表
     */
    fun addExcludedIngredient(ingredientId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    currentCriteria = state.currentCriteria.copy(
                        excludedIngredients = state.currentCriteria.excludedIngredients + ingredientId
                    )
                )
            }
        }
    }

    /**
     * 从排除列表移除食材
     */
    fun removeExcludedIngredient(ingredientId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    currentCriteria = state.currentCriteria.copy(
                        excludedIngredients = state.currentCriteria.excludedIngredients - ingredientId
                    )
                )
            }
        }
    }

    /**
     * 添加分类
     */
    fun addCategory(categoryId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    currentCriteria = state.currentCriteria.copy(
                        categoryIds = state.currentCriteria.categoryIds + categoryId
                    )
                )
            }
        }
    }

    /**
     * 移除分类
     */
    fun removeCategory(categoryId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    currentCriteria = state.currentCriteria.copy(
                        categoryIds = state.currentCriteria.categoryIds - categoryId
                    )
                )
            }
        }
    }

    /**
     * 清除所有筛选条件
     */
    fun clearFilters() {
        viewModelScope.launch {
            _uiState.update { it.copy(currentCriteria = RecipeFilterCriteria()) }
        }
    }

    /**
     * 应用筛选条件
     */
    fun applyFilter() {
        viewModelScope.launch {
            val criteria = _uiState.value.currentCriteria
            if (criteria.isEmpty()) {
                return
            }

            try {
                val filter = RecipeFilter(
                    id = java.util.UUID.randomUUID().toString(),
                    cookingTimeMin = criteria.cookingTimeMin,
                    cookingTimeMax = criteria.cookingTimeMax,
                    difficultyMin = criteria.difficultyMin,
                    difficultyMax = criteria.difficultyMax,
                    includedIngredients = criteria.includedIngredients.toList(),
                    excludedIngredients = criteria.excludedIngredients.toList(),
                    categoryIds = criteria.categoryIds.toList()
                )
                recipeFilterDao.insert(filter)
                _uiState.update { it.copy(successMessage = "筛选条件已保存") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "保存筛选条件失败：${e.message}") }
            }
        }
    }

    /**
     * 清除消息
     */
    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    /**
     * Filter UI State
     */
    data class FilterUiState(
        val currentCriteria: RecipeFilterCriteria = RecipeFilterCriteria(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val successMessage: String? = null
    )
}
