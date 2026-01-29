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

    init {
        loadRecipes()
    }

    private fun loadRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAllRecipes().collect { recipeList ->
                _recipes.value = recipeList
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
            val recipe = Recipe(
                name = name,
                description = description,
                cookingTime = cookingTime,
                servings = servings,
                difficulty = difficulty
            )
            repository.insertRecipeWithDetails(recipe, ingredients, instructions)
        }
    }

    fun updateRecipe(
        recipe: Recipe,
        ingredients: List<RecipeIngredient>,
        instructions: List<RecipeInstruction>
    ) {
        viewModelScope.launch {
            repository.updateRecipeWithDetails(recipe, ingredients, instructions)
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            repository.deleteRecipe(recipe)
        }
    }
}
