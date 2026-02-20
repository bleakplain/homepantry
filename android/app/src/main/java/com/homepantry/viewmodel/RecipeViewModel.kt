package com.homepantry.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homepantry.data.entity.Recipe
import com.homepantry.data.entity.RecipeIngredient
import com.homepantry.data.repository.RecipeRepository
import com.homepantry.utils.Logger
import com.homepantry.utils.PerformanceMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 菜谱视图模型
 */
class RecipeViewModel(
    private val recipeId: String,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    companion object {
        private const val TAG = "RecipeViewModel"
    }

    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    init {
        Logger.d(TAG, "RecipeViewModel init for recipe: $recipeId")
        loadRecipe()
    }

    /**
     * 加载菜谱
     */
    private fun loadRecipe() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("loadRecipe") {
                Logger.enter("RecipeViewModel.loadRecipe", recipeId)

                recipeRepository.getRecipeById(recipeId)
                    .collect { recipe ->
                        if (recipe != null) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    recipe = recipe
                                )
                            }
                            Logger.d(TAG, "加载菜谱成功：${recipe.name}")
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "菜谱不存在"
                                )
                            }
                            Logger.e(TAG, "菜谱不存在：$recipeId")
                        }
                    }

                Logger.exit("RecipeViewModel.loadRecipe")
            }
        }
    }

    /**
     * 更新菜谱
     */
    fun updateRecipe(recipe: Recipe) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("updateRecipe") {
                Logger.enter("RecipeViewModel.updateRecipe", recipe.id)

                recipeRepository.updateRecipe(recipe)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                recipe = recipe,
                                successMessage = "菜谱更新成功"
                            )
                        }
                        Logger.d(TAG, "菜谱更新成功：${recipe.name}")
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "更新失败：${e.message}"
                            )
                        }
                        Logger.e(TAG, "菜谱更新失败：${recipe.name}", e)
                    }

                Logger.exit("RecipeViewModel.updateRecipe")
            }
        }
    }

    /**
     * 删除菜谱
     */
    fun deleteRecipe() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("deleteRecipe") {
                Logger.enter("RecipeViewModel.deleteRecipe", recipeId)

                recipeRepository.deleteRecipe(recipeId)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isDeleted = true,
                                successMessage = "删除成功"
                            )
                        }
                        Logger.d(TAG, "删除菜谱成功：$recipeId")
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "删除失败：${e.message}"
                            )
                        }
                        Logger.e(TAG, "删除菜谱失败：$recipeId", e)
                    }

                Logger.exit("RecipeViewModel.deleteRecipe")
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
        Logger.d(TAG, "RecipeViewModel onCleared")
    }
}

/**
 * 菜谱 UI 状态
 */
data class RecipeUiState(
    val isLoading: Boolean = false,
    val recipe: Recipe? = null,
    val error: String? = null,
    val successMessage: String? = null,
    val isDeleted: Boolean = false
)
