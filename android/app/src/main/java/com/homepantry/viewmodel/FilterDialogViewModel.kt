package com.homepantry.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homepantry.data.dao.IngredientDao
import com.homepantry.data.dao.CategoryDao
import com.homepantry.data.entity.Ingredient
import com.homepantry.data.entity.Category
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 筛选对话框 ViewModel
 */
class FilterDialogViewModel(
    private val ingredientDao: IngredientDao,
    private val categoryDao: CategoryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(FilterDialogUiState())
    val uiState: StateFlow<FilterDialogUiState> = _uiState.asStateFlow()

    init {
        loadIngredients()
        loadCategories()
    }

    /**
     * 加载所有食材
     */
    private fun loadIngredients() {
        viewModelScope.launch {
            try {
                val ingredients = ingredientDao.getAllIngredients()
                _uiState.update { it.copy(ingredients = ingredients) }
            } catch (e: Exception) {
                _uiState.update { state -> state.copy(error = "加载食材失败：${e.message}") }
            }
        }
    }

    /**
     * 加载所有分类
     */
    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val categories = categoryDao.getAllCategories()
                _uiState.update { it.copy(categories = categories) }
            } catch (e: Exception) {
                _uiState.update { state -> state.copy(error = "加载分类失败：${e.message}") }
            }
        }
    }

    /**
     * 搜索食材
     */
    fun searchIngredients(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(searchQuery = query) }
        }
    }

    /**
     * 切换食材（包含/排除）
     */
    fun toggleIngredient(ingredientId: String, type: IngredientType) {
        viewModelScope.launch {
            val currentState = _uiState.value
            
            when (type) {
                IngredientType.INCLUDED -> {
                    val included = currentState.includedIngredientIds + ingredientId
                    val excluded = currentState.excludedIngredientIds - ingredientId
                    _uiState.update { it.copy(includedIngredientIds = included, excludedIngredientIds = excluded) }
                }
                IngredientType.EXCLUDED -> {
                    val included = currentState.includedIngredientIds - ingredientId
                    val excluded = currentState.excludedIngredientIds + ingredientId
                    _uiState.update { it.copy(includedIngredientIds = included, excludedIngredientIds = excluded) }
                }
            }
        }
    }

    /**
     * 切换分类
     */
    fun toggleCategory(categoryId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val selected = if (currentState.selectedCategoryIds.contains(categoryId)) {
                currentState.selectedCategoryIds - categoryId
            } else {
                currentState.selectedCategoryIds + categoryId
            }
            _uiState.update { it.copy(selectedCategoryIds = selected) }
        }
    }

    /**
     * 清除选择
     */
    fun clearSelections() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    includedIngredientIds = emptySet(),
                    excludedIngredientIds = emptySet(),
                    selectedCategoryIds = emptySet(),
                    searchQuery = ""
                )
            }
        }
    }

    /**
     * 清除消息
     */
    fun clearMessages() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Filter Dialog UI State
     */
    data class FilterDialogUiState(
        val ingredients: List<Ingredient> = emptyList(),
        val categories: List<Category> = emptyList(),
        val includedIngredientIds: Set<String> = emptySet(),
        val excludedIngredientIds: Set<String> = emptySet(),
        val selectedCategoryIds: Set<String> = emptySet(),
        val searchQuery: String = "",
        val isLoading: Boolean = false,
        val error: String? = null
    )

    enum class IngredientType {
        INCLUDED, EXCLUDED
    }
}
