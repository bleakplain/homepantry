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
        loadIngredients()
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
     * 加载食材
     */
    private fun loadIngredients() {
        viewModelScope.launch {
            PerformanceMonitor.recordMethodPerformance("loadIngredients") {
                Logger.enter("RecipeViewModel.loadIngredients", recipeId)

                recipeRepository.getRecipeIngredients(recipeId)
                    .collect { ingredients ->
                        _uiState.update {
                            it.copy(
                                ingredients = ingredients
                            )
                        }
                        Logger.d(TAG, "加载食材成功：${ingredients.size} 个")
                    }

                Logger.exit("RecipeViewModel.loadIngredients")
            }
        }
    }

    /**
     * 创建菜谱
     */
    fun createRecipe(
        name: String,
        description: String?,
        ingredients: List<RecipeIngredient>
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("createRecipe") {
                Logger.enter("RecipeViewModel.createRecipe", name, description, ingredients.size)

                recipeRepository.createRecipe(name, description, ingredients)
                    .onSuccess { recipe ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                recipe = recipe,
                                successMessage = "菜谱创建成功"
                            )
                        }
                        Logger.d("RecipeViewModel.createRecipe", "菜谱创建成功：${recipe.name}")
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "创建失败：${e.message}"
                            )
                        }
                        Logger.e("RecipeViewModel.createRecipe", "菜谱创建失败", e)
                    }

                Logger.exit("RecipeViewModel.createRecipe")
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
                        Logger.d("RecipeViewModel.updateRecipe", "菜谱更新成功：${recipe.name}")
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "更新失败：${e.message}"
                            )
                        }
                        Logger.e("RecipeViewModel.updateRecipe", "菜谱更新失败", e)
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
                        Logger.d("RecipeViewModel.deleteRecipe", "删除成功：$recipeId")
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "删除失败：${e.message}"
                            )
                        }
                        Logger.e("RecipeViewModel.deleteRecipe", "删除失败：$recipeId", e)
                    }

                Logger.exit("RecipeViewModel.deleteRecipe")
            }
        }
    }

    /**
     * 添加食材
     */
    fun addIngredient(ingredient: RecipeIngredient) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("addIngredient") {
                Logger.enter("RecipeViewModel.addIngredient", ingredient.name)

                recipeRepository.addIngredient(ingredient)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                ingredients = it.ingredients + ingredient,
                                successMessage = "添加成功"
                            )
                        }
                        Logger.d("RecipeViewModel.addIngredient", "添加成功：${ingredient.name}")
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "添加失败：${e.message}"
                            )
                        }
                        Logger.e("RecipeViewModel.addIngredient", "添加失败", e)
                    }

                Logger.exit("RecipeViewModel.addIngredient")
            }
        }
    }

    /**
     * 移除食材
     */
    fun removeIngredient(ingredientId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("removeIngredient") {
                Logger.enter("RecipeViewModel.removeIngredient", ingredientId)

                recipeRepository.removeIngredient(ingredientId)
                    .onSuccess {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                ingredients = it.ingredients.filter { it.id != ingredientId },
                                successMessage = "移除成功"
                            )
                        }
                        Logger.d("RecipeViewModel.removeIngredient", "移除成功：$ingredientId")
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "移除失败：${e.message}"
                            )
                        }
                        Logger.e("RecipeViewModel.removeIngredient", "移除失败：$ingredientId", e)
                    }

                Logger.exit("RecipeViewModel.removeIngredient")
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
    val ingredients: List<RecipeIngredient> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null,
    val isDeleted: Boolean = false
)
