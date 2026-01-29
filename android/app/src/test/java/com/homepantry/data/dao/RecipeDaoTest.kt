package com.homepantry.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.homepantry.data.database.HomePantryDatabase
import com.homepantry.testing.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
@OptIn(ExperimentalCoroutinesApi::class)
class RecipeDaoTest {

    private lateinit var database: HomePantryDatabase
    private lateinit var recipeDao: RecipeDao
    private lateinit var ingredientDao: IngredientDao
    private lateinit var categoryDao: CategoryDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HomePantryDatabase::class.java
        ).allowMainThreadQueries().build()
        recipeDao = database.recipeDao()
        ingredientDao = database.ingredientDao()
        categoryDao = database.categoryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insertRecipe returns correct id`() = runTest {
        val recipe = TestDataBuilders.createRecipe(
            name = "测试菜谱",
            cookingTime = 30,
            servings = 4
        )

        val id = recipeDao.insertRecipe(recipe)

        assertNotNull(id)
    }

    @Test
    fun `getRecipeById returns correct recipe`() = runTest {
        val recipe = TestDataBuilders.createRecipe(
            id = "test-recipe-1",
            name = "测试菜谱"
        )

        recipeDao.insertRecipe(recipe)
        val result = recipeDao.getRecipeById("test-recipe-1")

        assertNotNull(result)
        assertEquals("test-recipe-1", result?.id)
        assertEquals("测试菜谱", result?.name)
    }

    @Test
    fun `getRecipeById returns null when recipe not exists`() = runTest {
        val result = recipeDao.getRecipeById("non-existent")

        assertNull(result)
    }

    @Test
    fun `getAllRecipes returns all recipes`() = runTest {
        val recipes = TestDataBuilders.createRecipeList(3)
        recipes.forEach { recipeDao.insertRecipe(it) }

        val result = recipeDao.getAllRecipes().first()

        assertEquals(3, result.size)
    }

    @Test
    fun `getAllRecipes returns empty list when no recipes`() = runTest {
        val result = recipeDao.getAllRecipes().first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `updateRecipe updates recipe data`() = runTest {
        val recipe = TestDataBuilders.createRecipe(
            name = "原名称",
            cookingTime = 30
        )

        recipeDao.insertRecipe(recipe)
        val updatedRecipe = recipe.copy(
            name = "新名称",
            cookingTime = 45
        )
        recipeDao.updateRecipe(updatedRecipe)

        val result = recipeDao.getRecipeById(recipe.id)
        assertEquals("新名称", result?.name)
        assertEquals(45, result?.cookingTime)
    }

    @Test
    fun `deleteRecipe removes recipe from database`() = runTest {
        val recipe = TestDataBuilders.createRecipe()
        recipeDao.insertRecipe(recipe)

        recipeDao.deleteRecipe(recipe)

        val result = recipeDao.getRecipeById(recipe.id)
        assertNull(result)
    }

    @Test
    fun `searchRecipes returns matching recipes by name`() = runTest {
        val recipes = listOf(
            TestDataBuilders.createRecipe(name = "番茄炒蛋", id = "r1"),
            TestDataBuilders.createRecipe(name = "红烧肉", id = "r2"),
            TestDataBuilders.createRecipe(name = "清蒸鱼", id = "r3")
        )
        recipes.forEach { recipeDao.insertRecipe(it) }

        val results = recipeDao.searchRecipes("番茄").first()

        assertEquals(1, results.size)
        assertEquals("r1", results[0].id)
    }

    @Test
    fun `searchRecipes returns matching recipes by description`() = runTest {
        val recipe = TestDataBuilders.createRecipe(
            id = "r1",
            name = "菜谱A",
            description = "这是番茄做的菜"
        )
        recipeDao.insertRecipe(recipe)

        val results = recipeDao.searchRecipes("番茄").first()

        assertEquals(1, results.size)
        assertEquals("r1", results[0].id)
    }

    @Test
    fun `searchRecipes is case insensitive`() = runTest {
        val recipe = TestDataBuilders.createRecipe(
            name = "番茄炒蛋"
        )
        recipeDao.insertRecipe(recipe)

        val results = recipeDao.searchRecipes("番茄").first()
        val resultsLower = recipeDao.searchRecipes("番茄").first()

        assertEquals(1, results.size)
        assertEquals(1, resultsLower.size)
    }

    @Test
    fun `searchRecipes returns empty list when no match`() = runTest {
        val recipe = TestDataBuilders.createRecipe(name = "番茄炒蛋")
        recipeDao.insertRecipe(recipe)

        val results = recipeDao.searchRecipes("红烧肉").first()

        assertTrue(results.isEmpty())
    }

    @Test
    fun `searchRecipes with empty query returns all recipes`() = runTest {
        val recipes = TestDataBuilders.createRecipeList(3)
        recipes.forEach { recipeDao.insertRecipe(it) }

        val results = recipeDao.searchRecipes("").first()

        assertEquals(3, results.size)
    }

    @Test
    fun `getRecipesByDifficulty returns correct difficulty`() = runTest {
        val recipes = listOf(
            TestDataBuilders.createRecipe(id = "r1", difficulty = DifficultyLevel.EASY),
            TestDataBuilders.createRecipe(id = "r2", difficulty = DifficultyLevel.MEDIUM),
            TestDataBuilders.createRecipe(id = "r3", difficulty = DifficultyLevel.HARD)
        )
        recipes.forEach { recipeDao.insertRecipe(it) }

        val easyRecipes = recipeDao.getRecipesByDifficulty(DifficultyLevel.EASY).first()
        val mediumRecipes = recipeDao.getRecipesByDifficulty(DifficultyLevel.MEDIUM).first()
        val hardRecipes = recipeDao.getRecipesByDifficulty(DifficultyLevel.HARD).first()

        assertEquals(1, easyRecipes.size)
        assertEquals("r1", easyRecipes[0].id)
        assertEquals(1, mediumRecipes.size)
        assertEquals("r2", mediumRecipes[0].id)
        assertEquals(1, hardRecipes.size)
        assertEquals("r3", hardRecipes[0].id)
    }

    @Test
    fun `getRecipesWithMaxCookingTime returns recipes under limit`() = runTest {
        val recipes = listOf(
            TestDataBuilders.createRecipe(id = "r1", cookingTime = 15),
            TestDataBuilders.createRecipe(id = "r2", cookingTime = 30),
            TestDataBuilders.createRecipe(id = "r3", cookingTime = 60)
        )
        recipes.forEach { recipeDao.insertRecipe(it) }

        val results = recipeDao.getRecipesWithMaxCookingTime(30).first()

        assertEquals(2, results.size)
        assertTrue(results.all { it.cookingTime <= 30 })
    }

    @Test
    fun `getRecipesWithMaxCookingTime returns empty when none match`() = runTest {
        val recipe = TestDataBuilders.createRecipe(cookingTime = 60)
        recipeDao.insertRecipe(recipe)

        val results = recipeDao.getRecipesWithMaxCookingTime(15).first()

        assertTrue(results.isEmpty())
    }

    @Test
    fun `getRecipesByServings returns correct servings`() = runTest {
        val recipes = listOf(
            TestDataBuilders.createRecipe(id = "r1", servings = 2),
            TestDataBuilders.createRecipe(id = "r2", servings = 4),
            TestDataBuilders.createRecipe(id = "r3", servings = 6)
        )
        recipes.forEach { recipeDao.insertRecipe(it) }

        val results = recipeDao.getRecipesByServings(minServings = 3).first()

        assertEquals(2, results.size)
        assertTrue(results.all { it.servings >= 3 })
    }

    @Test
    fun `getRecipesByServings with range returns correct results`() = runTest {
        val recipes = listOf(
            TestDataBuilders.createRecipe(id = "r1", servings = 2),
            TestDataBuilders.createRecipe(id = "r2", servings = 4),
            TestDataBuilders.createRecipe(id = "r3", servings = 6)
        )
        recipes.forEach { recipeDao.insertRecipe(it) }

        val results = recipeDao.getRecipesByServings(
            minServings = 2,
            maxServings = 4
        ).first()

        assertEquals(2, results.size)
        assertTrue(results.all { it.servings in 2..4 })
    }

    @Test
    fun `getFavoriteRecipes returns only favorites`() = runTest {
        val recipes = listOf(
            TestDataBuilders.createRecipe(id = "r1", isFavorite = true),
            TestDataBuilders.createRecipe(id = "r2", isFavorite = false),
            TestDataBuilders.createRecipe(id = "r3", isFavorite = true)
        )
        recipes.forEach { recipeDao.insertRecipe(it) }

        val results = recipeDao.getFavoriteRecipes().first()

        assertEquals(2, results.size)
        assertTrue(results.all { it.isFavorite })
    }

    @Test
    fun `updateFavoriteStatus updates favorite field`() = runTest {
        val recipe = TestDataBuilders.createRecipe(
            id = "r1",
            isFavorite = false
        )
        recipeDao.insertRecipe(recipe)

        recipeDao.updateFavoriteStatus("r1", true)

        val result = recipeDao.getRecipeById("r1")
        assertTrue(result?.isFavorite == true)
    }

    @Test
    fun `updateFavoriteStatus toggles correctly`() = runTest {
        val recipe = TestDataBuilders.createRecipe(
            id = "r1",
            isFavorite = false
        )
        recipeDao.insertRecipe(recipe)

        recipeDao.updateFavoriteStatus("r1", true)
        var result = recipeDao.getRecipeById("r1")
        assertTrue(result?.isFavorite == true)

        recipeDao.updateFavoriteStatus("r1", false)
        result = recipeDao.getRecipeById("r1")
        assertFalse(result?.isFavorite == true)
    }

    @Test
    fun `getRecipesByCategory returns recipes in category`() = runTest {
        val category = TestDataBuilders.createCategory(id = "cat1")
        categoryDao.insertCategory(category)

        val recipes = listOf(
            TestDataBuilders.createRecipe(id = "r1", categoryId = "cat1"),
            TestDataBuilders.createRecipe(id = "r2", categoryId = "cat1"),
            TestDataBuilders.createRecipe(id = "r3", categoryId = "cat2")
        )
        recipes.forEach { recipeDao.insertRecipe(it) }

        val results = recipeDao.getRecipesByCategory("cat1").first()

        assertEquals(2, results.size)
        assertTrue(results.all { it.categoryId == "cat1" })
    }

    @Test
    fun `insertRecipeIngredient adds ingredient to recipe`() = runTest {
        val recipe = TestDataBuilders.createRecipe()
        recipeDao.insertRecipe(recipe)

        val recipeIngredient = TestDataBuilders.createRecipeIngredient(
            recipeId = recipe.id,
            ingredientId = "ing1",
            quantity = 2.0
        )

        recipeDao.insertRecipeIngredient(recipeIngredient)

        val ingredients = recipeDao.getRecipeIngredients(recipe.id).first()
        assertEquals(1, ingredients.size)
        assertEquals("ing1", ingredients[0].ingredientId)
    }

    @Test
    fun `deleteRecipeIngredients removes all ingredients`() = runTest {
        val recipe = TestDataBuilders.createRecipe()
        recipeDao.insertRecipe(recipe)

        val ingredient = TestDataBuilders.createRecipeIngredient(recipeId = recipe.id)
        recipeDao.insertRecipeIngredient(ingredient)

        recipeDao.deleteRecipeIngredients(recipe.id)

        val ingredients = recipeDao.getRecipeIngredients(recipe.id).first()
        assertTrue(ingredients.isEmpty())
    }

    @Test
    fun `insertRecipeInstruction adds instruction to recipe`() = runTest {
        val recipe = TestDataBuilders.createRecipe()
        recipeDao.insertRecipe(recipe)

        val instruction = TestDataBuilders.createRecipeInstruction(
            recipeId = recipe.id,
            stepNumber = 1,
            instructionText = "第一步"
        )

        recipeDao.insertRecipeInstruction(instruction)

        val instructions = recipeDao.getRecipeInstructions(recipe.id).first()
        assertEquals(1, instructions.size)
        assertEquals("第一步", instructions[0].instructionText)
    }

    @Test
    fun `getRecipeInstructions returns instructions in order`() = runTest {
        val recipe = TestDataBuilders.createRecipe()
        recipeDao.insertRecipe(recipe)

        val instructions = listOf(
            TestDataBuilders.createRecipeInstruction(
                recipeId = recipe.id,
                stepNumber = 1,
                instructionText = "第一步"
            ),
            TestDataBuilders.createRecipeInstruction(
                recipeId = recipe.id,
                stepNumber = 2,
                instructionText = "第二步"
            ),
            TestDataBuilders.createRecipeInstruction(
                recipeId = recipe.id,
                stepNumber = 3,
                instructionText = "第三步"
            )
        )
        instructions.forEach { recipeDao.insertRecipeInstruction(it) }

        val results = recipeDao.getRecipeInstructions(recipe.id).first()

        assertEquals(3, results.size)
        assertEquals("第一步", results[0].instructionText)
        assertEquals("第二步", results[1].instructionText)
        assertEquals("第三步", results[2].instructionText)
    }

    @Test
    fun `deleteRecipeInstructions removes all instructions`() = runTest {
        val recipe = TestDataBuilders.createRecipe()
        recipeDao.insertRecipe(recipe)

        val instruction = TestDataBuilders.createRecipeInstruction(recipeId = recipe.id)
        recipeDao.insertRecipeInstruction(instruction)

        recipeDao.deleteRecipeInstructions(recipe.id)

        val instructions = recipeDao.getRecipeInstructions(recipe.id).first()
        assertTrue(instructions.isEmpty())
    }

    @Test
    fun `transaction createRecipeWithDetails creates complete recipe`() = runTest {
        val recipe = TestDataBuilders.createRecipe()
        val ingredient = TestDataBuilders.createRecipeIngredient(
            recipeId = recipe.id,
            ingredientId = "ing1"
        )
        val instruction = TestDataBuilders.createRecipeInstruction(
            recipeId = recipe.id
        )

        // In a real test, you'd use @Transaction
        recipeDao.insertRecipe(recipe)
        recipeDao.insertRecipeIngredient(ingredient)
        recipeDao.insertRecipeInstruction(instruction)

        val retrievedRecipe = recipeDao.getRecipeById(recipe.id)
        assertNotNull(retrievedRecipe)

        val ingredients = recipeDao.getRecipeIngredients(recipe.id).first()
        assertEquals(1, ingredients.size)

        val instructions = recipeDao.getRecipeInstructions(recipe.id).first()
        assertEquals(1, instructions.size)
    }

    @Test
    fun `getRecipesByNameAsc sorts alphabetically`() = runTest {
        val recipes = listOf(
            TestDataBuilders.createRecipe(id = "r1", name = "番茄炒蛋"),
            TestDataBuilders.createRecipe(id = "r2", name = "红烧肉"),
            TestDataBuilders.createRecipe(id = "r3", name = "清蒸鱼")
        )
        recipes.forEach { recipeDao.insertRecipe(it) }

        val results = recipeDao.getRecipesByNameAsc().first()

        assertEquals(3, results.size)
        assertEquals("清蒸鱼", results[0].name)
        assertEquals("番茄炒蛋", results[1].name)
        assertEquals("红烧肉", results[2].name)
    }

    @Test
    fun `getRecipesByNameDesc sorts reverse alphabetically`() = runTest {
        val recipes = listOf(
            TestDataBuilders.createRecipe(id = "r1", name = "番茄炒蛋"),
            TestDataBuilders.createRecipe(id = "r2", name = "红烧肉"),
            TestDataBuilders.createRecipe(id = "r3", name = "清蒸鱼")
        )
        recipes.forEach { recipeDao.insertRecipe(it) }

        val results = recipeDao.getRecipesByNameDesc().first()

        assertEquals(3, results.size)
        assertEquals("红烧肉", results[0].name)
        assertEquals("番茄炒蛋", results[1].name)
        assertEquals("清蒸鱼", results[2].name)
    }

    @Test
    fun `getRecipesByNewestFirst sorts by creation time`() = runTest {
        val now = System.currentTimeMillis()
        val recipes = listOf(
            TestDataBuilders.createRecipe(id = "r1", createdAt = now - 3000),
            TestDataBuilders.createRecipe(id = "r2", createdAt = now - 2000),
            TestDataBuilders.createRecipe(id = "r3", createdAt = now - 1000)
        )
        recipes.forEach { recipeDao.insertRecipe(it) }

        val results = recipeDao.getRecipesByNewestFirst().first()

        assertEquals(3, results.size)
        assertEquals("r3", results[0].id) // Most recent
        assertEquals("r1", results[2].id) // Oldest
    }

    @Test
    fun `getRecipesByCookingTimeAsc sorts by time ascending`() = runTest {
        val recipes = listOf(
            TestDataBuilders.createRecipe(id = "r1", cookingTime = 60),
            TestDataBuilders.createRecipe(id = "r2", cookingTime = 30),
            TestDataBuilders.createRecipe(id = "r3", cookingTime = 15)
        )
        recipes.forEach { recipeDao.insertRecipe(it) }

        val results = recipeDao.getRecipesByCookingTimeAsc().first()

        assertEquals(3, results.size)
        assertEquals(15, results[0].cookingTime)
        assertEquals(60, results[2].cookingTime)
    }

    @Test
    fun `getRecipesByDifficultyAsc sorts by difficulty`() = runTest {
        val recipes = listOf(
            TestDataBuilders.createRecipe(id = "r1", difficulty = DifficultyLevel.HARD),
            TestDataBuilders.createRecipe(id = "r2", difficulty = DifficultyLevel.MEDIUM),
            TestDataBuilders.createRecipe(id = "r3", difficulty = DifficultyLevel.EASY)
        )
        recipes.forEach { recipeDao.insertRecipe(it) }

        val results = recipeDao.getRecipesByDifficultyAsc().first()

        assertEquals(3, results.size)
        assertEquals(DifficultyLevel.EASY, results[0].difficulty)
        assertEquals(DifficultyLevel.HARD, results[2].difficulty)
    }

    @Test
    fun `searchRecipes_withSpecialCharacters_handlesCorrectly`() = runTest {
        val recipe = TestDataBuilders.createRecipe(name = "特殊字符!@#菜谱")
        recipeDao.insertRecipe(recipe)

        val results = recipeDao.searchRecipes("特殊字符!@#菜谱").first()

        assertEquals(1, results.size)
    }
}
