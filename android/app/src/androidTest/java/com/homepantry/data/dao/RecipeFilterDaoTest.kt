package com.homepantry.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.homepantry.data.database.HomePantryDatabase
import com.homepantry.data.entity.DifficultyLevel
import com.homepantry.data.entity.Recipe
import com.homepantry.data.entity.RecipeFilter
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecipeFilterDaoTest {

    private lateinit var database: HomePantryDatabase
    private lateinit var recipeFilterDao: RecipeFilterDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HomePantryDatabase::class.java
        ).allowMainThreadQueries()
            .build()
        recipeFilterDao = database.recipeFilterDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInsertRecipeFilter() = runTest {
        val filter = RecipeFilter(
            id = "test-filter-1",
            cookingTimeMin = 15,
            cookingTimeMax = 30,
            difficultyMin = DifficultyLevel.EASY,
            difficultyMax = DifficultyLevel.MEDIUM,
            includedIngredients = listOf("ingredient-1", "ingredient-2"),
            categoryIds = listOf("category-1", "category-2")
        )

        recipeFilterDao.insert(filter)

        val retrieved = recipeFilterDao.getFilterById("test-filter-1")
        assertNotNull(retrieved)
        assertEquals("test-filter-1", retrieved?.id)
        assertEquals(15, retrieved?.cookingTimeMin)
    }

    @Test
    fun testUpdateRecipeFilter() = runTest {
        val filter = RecipeFilter(
            id = "test-filter-2",
            cookingTimeMin = 15,
            cookingTimeMax = 30
        )

        recipeFilterDao.insert(filter)

        val updated = filter.copy(cookingTimeMin = 10, cookingTimeMax = 20)
        recipeFilterDao.update(updated)

        val retrieved = recipeFilterDao.getFilterById("test-filter-2")
        assertNotNull(retrieved)
        assertEquals(10, retrieved?.cookingTimeMin)
        assertEquals(20, retrieved?.cookingTimeMax)
    }

    @Test
    fun testDeleteRecipeFilter() = runTest {
        val filter = RecipeFilter(
            id = "test-filter-3",
            cookingTimeMin = 15,
            cookingTimeMax = 30
        )

        recipeFilterDao.insert(filter)

        var retrieved = recipeFilterDao.getFilterById("test-filter-3")
        assertNotNull(retrieved)

        recipeFilterDao.deleteById("test-filter-3")

        retrieved = recipeFilterDao.getFilterById("test-filter-3")
        assertNull(retrieved)
    }

    @Test
    fun testGetAllFilters() = runTest {
        val filter1 = RecipeFilter(id = "f1", cookingTimeMin = 10)
        val filter2 = RecipeFilter(id = "f2", cookingTimeMin = 20)
        val filter3 = RecipeFilter(id = "f3", cookingTimeMin = 30)

        recipeFilterDao.insert(filter1)
        recipeFilterDao.insert(filter2)
        recipeFilterDao.insert(filter3)

        val filters = recipeFilterDao.getAllFiltersOnce()
        assertEquals(3, filters.size)
    }

    @Test
    fun testGetLatestFilter() = runTest {
        val filter1 = RecipeFilter(id = "f1", createdAt = 1000)
        val filter2 = RecipeFilter(id = "f2", createdAt = 2000)
        val filter3 = RecipeFilter(id = "f3", createdAt = 3000)

        recipeFilterDao.insert(filter1)
        recipeFilterDao.insert(filter2)
        recipeFilterDao.insert(filter3)

        val latest = recipeFilterDao.getLatestFilter()
        assertNotNull(latest)
        assertEquals("f3", latest?.id)
    }

    @Test
    fun testFilterRecipesByTime() = runTest {
        val recipe1 = Recipe(name = "Recipe 1", cookingTime = 10, difficulty = DifficultyLevel.EASY, createdAt = 3000)
        val recipe2 = Recipe(name = "Recipe 2", cookingTime = 20, difficulty = DifficultyLevel.MEDIUM, createdAt = 2000)
        val recipe3 = Recipe(name = "Recipe 3", cookingTime = 30, difficulty = DifficultyLevel.HARD, createdAt = 1000)

        database.recipeDao().insert(recipe1)
        database.recipeDao().insert(recipe2)
        database.recipeDao().insert(recipe3)

        val recipes = recipeFilterDao.filterRecipes(
            cookingTimeMin = 15,
            cookingTimeMax = 30,
            difficultyMin = null,
            difficultyMax = null,
            categoryIds = null,
            includedIngredients = null,
            excludedIngredients = null
        )

        assertEquals(1, recipes.size)
        assertEquals("Recipe 1", recipes[0].name)
    }

    @Test
    fun testFilterRecipesByDifficulty() = runTest {
        val recipe1 = Recipe(name = "Recipe 1", cookingTime = 20, difficulty = DifficultyLevel.EASY, createdAt = 3000)
        val recipe2 = Recipe(name = "Recipe 2", cookingTime = 20, difficulty = DifficultyLevel.MEDIUM, createdAt = 2000)
        val recipe3 = Recipe(name = "Recipe 3", cookingTime = 20, difficulty = DifficultyLevel.HARD, createdAt = 1000)

        database.recipeDao().insert(recipe1)
        database.recipeDao().insert(recipe2)
        database.recipeDao().insert(recipe3)

        val recipes = recipeFilterDao.filterRecipes(
            cookingTimeMin = null,
            cookingTimeMax = null,
            difficultyMin = DifficultyLevel.MEDIUM,
            difficultyMax = DifficultyLevel.MEDIUM,
            categoryIds = null,
            includedIngredients = null,
            excludedIngredients = null
        )

        assertEquals(1, recipes.size)
        assertEquals("Recipe 2", recipes[0].name)
    }

    @Test
    fun testFilterRecipesByCategory() = runTest {
        val recipe1 = Recipe(name = "Recipe 1", categoryId = "cat1", createdAt = 3000)
        val recipe2 = Recipe(name = "Recipe 2", categoryId = "cat1", createdAt = 2000)
        val recipe3 = Recipe(name = "Recipe 3", categoryId = "cat2", createdAt = 1000)

        database.recipeDao().insert(recipe1)
        database.recipeDao().insert(recipe2)
        database.recipeDao().insert(recipe3)

        val recipes = recipeFilterDao.filterRecipes(
            cookingTimeMin = null,
            cookingTimeMax = null,
            difficultyMin = null,
            difficultyMax = null,
            categoryIds = listOf("cat1"),
            includedIngredients = null,
            excludedIngredients = null
        )

        assertEquals(2, recipes.size)
    }

    @Test
    fun testCountFilterResults() = runTest {
        val recipe1 = Recipe(name = "Recipe 1", cookingTime = 20, difficulty = DifficultyLevel.EASY, categoryId = "cat1", createdAt = 3000)
        val recipe2 = Recipe(name = "Recipe 2", cookingTime = 20, difficulty = DifficultyLevel.MEDIUM, categoryId = "cat1", createdAt = 2000)
        val recipe3 = Recipe(name = "Recipe 3", cookingTime = 20, difficulty = DifficultyLevel.HARD, categoryId = "cat2", createdAt = 1000)

        database.recipeDao().insert(recipe1)
        database.recipeDao().insert(recipe2)
        database.recipeDao().insert(recipe3)

        val count = recipeFilterDao.countFilterResults(
            cookingTimeMin = null,
            cookingTimeMax = null,
            difficultyMin = null,
            difficultyMax = null,
            categoryIds = listOf("cat1"),
            includedIngredients = null,
            excludedIngredients = null
        )

        assertEquals(2, count)
    }
}
