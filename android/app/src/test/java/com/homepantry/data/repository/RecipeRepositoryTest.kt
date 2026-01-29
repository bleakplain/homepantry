package com.homepantry.data.repository

import com.homepantry.data.dao.IngredientDao
import com.homepantry.data.dao.RecipeDao
import com.homepantry.data.entity.DifficultyLevel
import com.homepantry.data.entity.Recipe
import com.homepantry.data.entity.RecipeIngredient
import com.homepantry.data.entity.RecipeInstruction
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.eq

class RecipeRepositoryTest {

    @Mock
    private lateinit var recipeDao: RecipeDao

    @Mock
    private lateinit var ingredientDao: IngredientDao

    private lateinit var repository: RecipeRepository

    private val testRecipe = Recipe(
        id = "test-recipe-id",
        name = "番茄炒蛋",
        description = "经典家常菜",
        cookingTime = 15,
        servings = 2,
        difficulty = DifficultyLevel.EASY
    )

    private val testIngredients = listOf(
        RecipeIngredient(
            recipeId = "test-recipe-id",
            ingredientId = "tomato",
            quantity = 2.0
        ),
        RecipeIngredient(
            recipeId = "test-recipe-id",
            ingredientId = "egg",
            quantity = 3.0
        )
    )

    private val testInstructions = listOf(
        RecipeInstruction(
            recipeId = "test-recipe-id",
            stepNumber = 1,
            instruction = "番茄洗净切块"
        ),
        RecipeInstruction(
            recipeId = "test-recipe-id",
            stepNumber = 2,
            instruction = "鸡蛋打散"
        )
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = RecipeRepository(recipeDao, ingredientDao)
    }

    @Test
    fun `insert recipe calls dao insert`() = runTest {
        repository.insertRecipe(testRecipe)

        verify(recipeDao).insertRecipe(eq(testRecipe))
    }

    @Test
    fun `update recipe calls dao update`() = runTest {
        repository.updateRecipe(testRecipe)

        verify(recipeDao).updateRecipe(eq(testRecipe))
    }

    @Test
    fun `delete recipe calls dao delete`() = runTest {
        repository.deleteRecipe(testRecipe)

        verify(recipeDao).deleteRecipe(eq(testRecipe))
    }

    @Test
    fun `get recipe by id calls dao getRecipeById`() = runTest {
        repository.getRecipeById("test-recipe-id")

        verify(recipeDao).getRecipeById(eq("test-recipe-id"))
    }

    @Test
    fun `insert recipe with details calls all dao methods`() = runTest {
        repository.insertRecipeWithDetails(testRecipe, testIngredients, testInstructions)

        verify(recipeDao).insertRecipe(eq(testRecipe))
        verify(recipeDao).insertRecipeIngredient(eq(testIngredients[0]))
        verify(recipeDao).insertRecipeIngredient(eq(testIngredients[1]))
        verify(recipeDao).insertRecipeInstruction(eq(testInstructions[0]))
        verify(recipeDao).insertRecipeInstruction(eq(testInstructions[1]))
    }

    @Test
    fun `update recipe with details deletes and reinserts ingredients and instructions`() = runTest {
        repository.updateRecipeWithDetails(testRecipe, testIngredients, testInstructions)

        verify(recipeDao).updateRecipe(eq(testRecipe))
        verify(recipeDao).deleteRecipeIngredients(eq(testRecipe.id))
        verify(recipeDao).deleteRecipeInstructions(eq(testRecipe.id))
        verify(recipeDao).insertRecipeIngredient(eq(testIngredients[0]))
        verify(recipeDao).insertRecipeInstruction(eq(testInstructions[0]))
    }
}
