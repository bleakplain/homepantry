package com.homepantry.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homepantry.data.entity.Ingredient
import com.homepantry.data.repository.IngredientRepository
import com.homepantry.utils.Logger
import com.homepantry.utils.PerformanceMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 食材视图模型
 */
class IngredientViewModel(
    private val ingredientRepository: IngredientRepository
) : ViewModel() {

    companion object {
        private const val TAG = "IngredientViewModel"
    }

    private val _uiState = MutableStateFlow(IngredientUiState())
    val uiState: StateFlow<IngredientUiState> = _uiState.asStateFlow()

    init {
        Logger.d(TAG, "IngredientViewModel init")
        loadIngredients()
    }

    /**
     * 加载所有食材
     */
    private fun loadIngredients() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("loadIngredients") {
                Logger.enter("IngredientViewModel.loadIngredients")

                ingredientRepository.getAllIngredients()
                    .collect { ingredients ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                ingredients = ingredients
                            )
                        }
                    }

                Logger.exit("IngredientViewModel.loadIngredients")
            }
        }
    }

    /**
     * 创建食材
     */
    fun createIngredient(
        name: String,
        unit: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            PerformanceMonitor.recordMethodPerformance("createIngredient") {
                Logger.enter("IngredientViewModel.createIngredient", name, unit)

                ingredientRepository.createIngredient(name, unit)
                    .onSuccess { ingredient ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                ingredients = it.ingredients + ingredient,
                                successMessage = "食材创建成功"
                            )
                        }
                        Logger.d("IngredientViewModel.createIngredient", "食材创建成功")
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "创建失败：${e.message}"
                            )
                        }
                        Logger.e("IngredientViewModel.createIngredient", "创建失败", e)
                    }
            }
        }
    }

    /**
     * 更新食材
     */
    fun updateIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            PerformanceMonitor.recordMethodPerformance("updateIngredient") {
                Logger.enter("IngredientViewModel.updateIngredient", ingredient.id)

                ingredientRepository.updateIngredient(ingredient)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                ingredients = it.ingredients.map { if (it.id == ingredient.id) ingredient else it },
                                successMessage = "食材更新成功"
                            )
                        }
                        Logger.d("IngredientViewModel.updateIngredient", "食材更新成功")
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "更新失败：${e.message}"
                            )
                        }
                        Logger.e("IngredientViewModel.updateIngredient", "食材更新失败", e)
                    }
            }
        }
    }

    /**
     * 删除食材
     */
    fun deleteIngredient(ingredientId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            PerformanceMonitor.recordMethodPerformance("deleteIngredient") {
                Logger.enter("IngredientViewModel.deleteIngredient", ingredientId)

                ingredientRepository.deleteIngredient(ingredientId)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                ingredients = it.ingredients.filter { it.id != ingredientId },
                                successMessage = "删除成功"
                            )
                        }
                        Logger.d("IngredientViewModel.deleteIngredient", "食材删除成功：$ingredientId")
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "删除失败：${e.message}"
                            )
                        }
                        Logger.e("IngredientViewModel.deleteIngredient", "食材删除失败：$ingredientId", e)
                    }
            }
        }
    }

    /**
     * 搜索食材
     */
    fun searchIngredients(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("searchIngredients") {
                Logger.enter("IngredientViewModel.searchIngredients", query)

                ingredientRepository.searchIngredientsByName(query)
                    .collect { ingredients ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                ingredients = ingredients
                            )
                        }
                        Logger.d("IngredientViewModel.searchIngredients", "搜索食材成功：${ingredients.size} 个")
                    }

                Logger.exit("IngredientViewModel.searchIngredients")
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
        Logger.d(TAG, "IngredientViewModel onCleared")
    }
}

/**
 * 食材 UI 状态
 */
data class IngredientUiState(
    val isLoading: Boolean = false,
    val ingredients: List<Ingredient> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)
