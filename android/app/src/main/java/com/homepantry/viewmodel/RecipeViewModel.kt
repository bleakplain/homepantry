package com.homepantry.viewmodel

import androidx.lifecycle.viewModelScope
import com.homepantry.data.entity.Recipe
import com.homepantry.data.entity.RecipeIngredient
import com.homepantry.data.entity.RecipeInstruction
import com.homepantry.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RecipeViewModel(private val repository: RecipeRepository) : BaseViewModel() {

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
    val selectedRecipe: StateFlow<Recipe?> = _selectedRecipe.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadRecipes()
    }

    private fun loadRecipes() {
        viewModelScope.launch {
            setLoading(true)
            try {
                repository.getAllRecipes().collect { recipeList ->
                    _recipes.value = recipeList
                    setLoading(false)
                }
            } catch (e: Exception) {
                setError("加载菜谱失败: ${e.message}")
                setLoading(false)
            }
        }
    }

    fun searchRecipes(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            loadRecipes()
        } else {
            launchInBackground {
                repository.searchRecipes(query).collect { results ->
                    _recipes.value = results
                }
            }
        }
    }

    fun selectRecipe(recipeId: String) {
        launchInBackground {
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
        execute("菜谱添加成功") {
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
        execute("菜谱更新成功") {
            repository.updateRecipeWithDetails(recipe, ingredients, instructions)
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        execute("菜谱删除成功") {
            repository.deleteRecipe(recipe)
        }
    }
}
