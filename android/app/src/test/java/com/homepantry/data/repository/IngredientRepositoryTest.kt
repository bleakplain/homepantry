package com.homepantry.data.repository

import com.homepantry.data.dao.IngredientDao
import com.homepantry.data.dao.RecipeDao
import com.homepantry.data.entity.Ingredient
import com.homepantry.data.entity.IngredientCategory
import com.homepantry.data.entity.PantryItem
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.eq

class IngredientRepositoryTest {

    @Mock
    private lateinit var ingredientDao: IngredientDao

    @Mock
    private lateinit var recipeDao: RecipeDao

    private lateinit var repository: IngredientRepository

    private val testIngredient = Ingredient(
        id = "tomato",
        name = "番茄",
        unit = "个",
        category = IngredientCategory.VEGETABLE
    )

    private val testPantryItem = PantryItem(
        id = "pantry-item-1",
        ingredientId = "tomato",
        quantity = 5.0,
        expiryDate = System.currentTimeMillis() + 86400000, // 1 day from now
        purchasedDate = System.currentTimeMillis()
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = IngredientRepository(ingredientDao, recipeDao)
    }

    @Test
    fun `insert ingredient calls dao insert`() = runTest {
        repository.insertIngredient(testIngredient)

        verify(ingredientDao).insertIngredient(eq(testIngredient))
    }

    @Test
    fun `update ingredient calls dao update`() = runTest {
        repository.updateIngredient(testIngredient)

        verify(ingredientDao).updateIngredient(eq(testIngredient))
    }

    @Test
    fun `delete ingredient calls dao delete`() = runTest {
        repository.deleteIngredient(testIngredient)

        verify(ingredientDao).deleteIngredient(eq(testIngredient))
    }

    @Test
    fun `get ingredient by id calls dao getIngredientById`() = runTest {
        repository.getIngredientById("tomato")

        verify(ingredientDao).getIngredientById(eq("tomato"))
    }

    @Test
    fun `add pantry item calls dao insert`() = runTest {
        repository.addPantryItem(testPantryItem)

        verify(ingredientDao).insertPantryItem(eq(testPantryItem))
    }

    @Test
    fun `update pantry item calls dao update`() = runTest {
        repository.updatePantryItem(testPantryItem)

        verify(ingredientDao).updatePantryItem(eq(testPantryItem))
    }

    @Test
    fun `remove pantry item calls dao delete`() = runTest {
        repository.removePantryItem(testPantryItem)

        verify(ingredientDao).deletePantryItem(eq(testPantryItem))
    }

    @Test
    fun `clean expired items calls dao deleteExpiredItems`() = runTest {
        val expiryTime = System.currentTimeMillis()
        repository.cleanExpiredItems()

        verify(ingredientDao).deleteExpiredItems(eq(expiryTime))
    }

    @Test
    fun `get expiring items calls dao getExpiringItems`() = runTest {
        val expiryTime = System.currentTimeMillis()
        repository.getExpiringItems(expiryTime)

        verify(ingredientDao).getExpiringItems(eq(expiryTime))
    }
}
