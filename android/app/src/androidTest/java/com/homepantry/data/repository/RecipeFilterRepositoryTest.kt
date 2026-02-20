package com.homepantry.data.repository

import com.homepantry.data.dao.RecipeFilterDao
import com.homepantry.data.entity.DifficultyLevel
import com.homepantry.data.entity.RecipeFilter
import com.homepantry.data.entity.RecipeFilterCriteria
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify

@RunWith(org.junit.runners.JUnit4::class)
class RecipeFilterRepositoryTest {

    @Mock
    private lateinit var recipeFilterDao: RecipeFilterDao

    private lateinit var repository: RecipeFilterRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = RecipeFilterRepository(recipeFilterDao)
    }

    @After
    fun tearDown() {
        // Clean up if needed
    }

    @Test
    fun testCreateFilter() = runTest {
        val criteria = RecipeFilterCriteria(
            cookingTimeMin = 10,
            cookingTimeMax = 30,
            difficultyMin = DifficultyLevel.EASY
            difficultyMax = DifficultyLevel.MEDIUM
        )

        repository.createFilter(criteria)

        verify(recipeFilterDao).insert(any<RecipeFilter>())
    }

    @Test
    fun testUpdateFilter() = runTest {
        val filter = RecipeFilter(id = "test-filter-1")

        repository.updateFilter(filter)

        verify(recipeFilterDao).update(any<RecipeFilter>())
    }

    @Test
    fun testDeleteFilter() = runTest {
        repository.deleteFilter("test-filter-1")

        verify(recipeFilterDao).deleteById(eq("test-filter-1"))
    }

    @Test
    fun testGetFilters() = runTest {
        repository.getAllFilters()

        verify(recipeFilterDao).getAllFilters()
    }

    @Test
    fun testGetLatestFilter() = runTest {
        repository.getLatestFilter()

        verify(recipeFilterDao).getLatestFilter()
    }

    @Test
    fun testApplyFilter() = runTest {
        val criteria = RecipeFilterCriteria(
            cookingTimeMin = 10,
            cookingTimeMax = 30
        )

        repository.applyFilter(criteria)

        verify(recipeFilterDao).filterRecipes(
            eq(criteria.cookingTimeMin),
            eq(criteria.cookingTimeMax),
            eq(criteria.difficultyMin),
            eq(criteria.difficultyMax),
            any(), // categoryIds
            any(), // includedIngredients
            any()  // excludedIngredients
        )
    }

    @Test
    fun testClearAllFilters() = runTest {
        repository.clearAllFilters()

        verify(recipeFilterDao).deleteAll()
    }
}
