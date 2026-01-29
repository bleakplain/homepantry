package com.homepantry.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homepantry.data.entity.Recipe
import com.homepantry.data.entity.RecipeIngredient
import com.homepantry.data.entity.RecipeInstruction
import com.homepantry.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
    val selectedRecipe: StateFlow<Recipe?> = _selectedRecipe.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadRecipes()
    }

    private fun loadRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getAllRecipes().collect { recipeList ->
                    _recipes.value = recipeList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "加载菜谱失败: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun searchRecipes(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            loadRecipes()
        } else {
            viewModelScope.launch {
                repository.searchRecipes(query).collect { results ->
                    _recipes.value = results
                }
            }
        }
    }

    fun selectRecipe(recipeId: String) {
        viewModelScope.launch {
            _selectedRecipe.value = repository.getRecipeById(recipeId)
        }
    }

    fun clearSelectedRecipe() {
        _selectedRecipe.value = null
    }

    fun addRecipe(
        name: String,
        description: String?,
        cookingTime: Int,
        servings: Int,
        difficulty: com.homepantry.data.entity.DifficultyLevel,
        ingredients: List<RecipeIngredient>,
        instructions: List<RecipeInstruction>
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val recipe = Recipe(
                    name = name,
                    description = description,
                    cookingTime = cookingTime,
                    servings = servings,
                    difficulty = difficulty
                )
                repository.insertRecipeWithDetails(recipe, ingredients, instructions)
                _successMessage.value = "菜谱添加成功"
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "添加菜谱失败: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun updateRecipe(
        recipe: Recipe,
        ingredients: List<RecipeIngredient>,
        instructions: List<RecipeInstruction>
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.updateRecipeWithDetails(recipe, ingredients, instructions)
                _successMessage.value = "菜谱更新成功"
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "更新菜谱失败: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.deleteRecipe(recipe)
                _successMessage.value = "菜谱删除成功"
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "删除菜谱失败: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}
