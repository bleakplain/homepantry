package com.homepantry.data.repository

import com.homepantry.data.dao.IngredientDao
import com.homepantry.data.dao.RecipeDao
import com.homepantry.data.entity.Ingredient
import com.homepantry.data.entity.IngredientCategory
import com.homepantry.data.entity.PantryItem
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

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

    // Basic ingredient CRUD tests
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

    // Basic pantry item CRUD tests
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
    fun `remove pantry item by id calls dao deletePantryItemById`() = runTest {
        repository.removePantryItemById("pantry-item-1")

        verify(ingredientDao).deletePantryItemById(eq("pantry-item-1"))
    }

    // Expiry and cleanup tests
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

    // Search and filter tests
    @Test
    fun `search ingredients calls dao searchIngredients`() = runTest {
        repository.searchIngredients("番茄")

        verify(ingredientDao).searchIngredients(eq("番茄"))
    }

    @Test
    fun `search ingredients with empty query`() = runTest {
        repository.searchIngredients("")

        verify(ingredientDao).searchIngredients(eq(""))
    }

    @Test
    fun `search ingredients with special characters`() = runTest {
        repository.searchIngredients("特殊字符!@#")

        verify(ingredientDao).searchIngredients(eq("特殊字符!@#"))
    }

    @Test
    fun `get ingredients by category calls dao`() = runTest {
        repository.getIngredientsByCategory(IngredientCategory.VEGETABLE)

        verify(ingredientDao).getIngredientsByCategory(eq(IngredientCategory.VEGETABLE))
    }

    @Test
    fun `get pantry items by ingredient id calls dao`() = runTest {
        repository.getPantryItemsByIngredientId("tomato")

        verify(ingredientDao).getPantryItemsByIngredientId(eq("tomato"))
    }

    // Get all tests
    @Test
    fun `get all ingredients calls dao`() = runTest {
        val flow = flowOf<List<Ingredient>>(emptyList())
        whenever(ingredientDao.getAllIngredients()).thenReturn(flow)

        repository.getAllIngredients().first()

        verify(ingredientDao).getAllIngredients()
    }

    @Test
    fun `get all pantry items calls dao`() = runTest {
        val flow = flowOf<List<PantryItem>>(emptyList())
        whenever(ingredientDao.getAllPantryItems()).thenReturn(flow)

        repository.getAllPantryItems().first()

        verify(ingredientDao).getAllPantryItems()
    }

    // Edge cases for ingredients
    @Test
    fun `ingredient with very long name is handled`() = runTest {
        val longNameIngredient = testIngredient.copy(
            name = "这是一个非常非常非常非常非常长的食材名称"
        )
        repository.insertIngredient(longNameIngredient)

        verify(ingredientDao).insertIngredient(eq(longNameIngredient))
    }

    @Test
    fun `ingredient with empty unit is handled`() = runTest {
        val noUnitIngredient = testIngredient.copy(unit = "")
        repository.insertIngredient(noUnitIngredient)

        verify(ingredientDao).insertIngredient(eq(noUnitIngredient))
    }

    @Test
    fun `ingredient with all categories is handled`() = runTest {
        IngredientCategory.values().forEach { category ->
            val ingredient = testIngredient.copy(
                id = "ing-$category",
                category = category
            )
            repository.insertIngredient(ingredient)

            verify(ingredientDao).insertIngredient(eq(ingredient))
        }
    }

    // Edge cases for pantry items
    @Test
    fun `pantry item with zero quantity`() = runTest {
        val zeroQuantityItem = testPantryItem.copy(quantity = 0.0)
        repository.addPantryItem(zeroQuantityItem)

        verify(ingredientDao).insertPantryItem(eq(zeroQuantityItem))
    }

    @Test
    fun `pantry item with negative quantity`() = runTest {
        val negativeQuantityItem = testPantryItem.copy(quantity = -1.0)
        repository.addPantryItem(negativeQuantityItem)

        verify(ingredientDao).insertPantryItem(eq(negativeQuantityItem))
    }

    @Test
    fun `pantry item with very large quantity`() = runTest {
        val largeQuantityItem = testPantryItem.copy(quantity = 9999.99)
        repository.addPantryItem(largeQuantityItem)

        verify(ingredientDao).insertPantryItem(eq(largeQuantityItem))
    }

    @Test
    fun `pantry item with null expiry date`() = runTest {
        val noExpiryItem = testPantryItem.copy(expiryDate = null)
        repository.addPantryItem(noExpiryItem)

        verify(ingredientDao).insertPantryItem(eq(noExpiryItem))
    }

    @Test
    fun `pantry item with past expiry date`() = runTest {
        val pastExpiryItem = testPantryItem.copy(
            expiryDate = System.currentTimeMillis() - 86400000
        )
        repository.addPantryItem(pastExpiryItem)

        verify(ingredientDao).insertPantryItem(eq(pastExpiryItem))
    }

    @Test
    fun `pantry item with future expiry date`() = runTest {
        val futureExpiryItem = testPantryItem.copy(
            expiryDate = System.currentTimeMillis() + 86400000 * 365
        )
        repository.addPantryItem(futureExpiryItem)

        verify(ingredientDao).insertPantryItem(eq(futureExpiryItem))
    }

    @Test
    fun `pantry item with empty unit`() = runTest {
        val noUnitItem = testPantryItem.copy(unit = "")
        repository.addPantryItem(noUnitItem)

        verify(ingredientDao).insertPantryItem(eq(noUnitItem))
    }

    @Test
    fun `pantry item with special characters in unit`() = runTest {
        val specialUnitItem = testPantryItem.copy(unit = "kg/袋")
        repository.addPantryItem(specialUnitItem)

        verify(ingredientDao).insertPantryItem(eq(specialUnitItem))
    }

    // Multiple pantry items for same ingredient
    @Test
    fun `multiple pantry items for same ingredient is handled`() = runTest {
        val item1 = testPantryItem.copy(id = "item-1", quantity = 2.0)
        val item2 = testPantryItem.copy(id = "item-2", quantity = 3.0)
        val item3 = testPantryItem.copy(id = "item-3", quantity = 5.0)

        repository.addPantryItem(item1)
        repository.addPantryItem(item2)
        repository.addPantryItem(item3)

        verify(ingredientDao).insertPantryItem(eq(item1))
        verify(ingredientDao).insertPantryItem(eq(item2))
        verify(ingredientDao).insertPantryItem(eq(item3))
    }

    // Delete operations with empty/null values
    @Test
    fun `delete ingredient with empty id`() = runTest {
        repository.deleteIngredient(testIngredient.copy(id = ""))

        verify(ingredientDao).deleteIngredient(any())
    }

    @Test
    fun `get ingredient with empty id`() = runTest {
        repository.getIngredientById("")

        verify(ingredientDao).getIngredientById(eq(""))
    }

    @Test
    fun `get pantry items with empty ingredient id`() = runTest {
        repository.getPantryItemsByIngredientId("")

        verify(ingredientDao).getPantryItemsByIngredientId(eq(""))
    }

    // Timestamp edge cases
    @Test
    fun `clean expired items with zero timestamp`() = runTest {
        repository.cleanExpiredItems(0)

        verify(ingredientDao).deleteExpiredItems(eq(0))
    }

    @Test
    fun `get expiring items with zero timestamp`() = runTest {
        repository.getExpiringItems(0)

        verify(ingredientDao).getExpiringItems(eq(0))
    }

    @Test
    fun `get expiring items with negative timestamp`() = runTest {
        repository.getExpiringItems(-1)

        verify(ingredientDao).getExpiringItems(eq(-1))
    }

    @Test
    fun `get expiring items with very large timestamp`() = runTest {
        val largeTimestamp = System.currentTimeMillis() + 86400000 * 10000
        repository.getExpiringItems(largeTimestamp)

        verify(ingredientDao).getExpiringItems(eq(largeTimestamp))
    }

    // Decimal quantities
    @Test
    fun `pantry item with decimal quantity`() = runTest {
        val decimalItem = testPantryItem.copy(quantity = 2.5)
        repository.addPantryItem(decimalItem)

        verify(ingredientDao).insertPantryItem(eq(decimalItem))
    }

    @Test
    fun `pantry item with very small decimal quantity`() = runTest {
        val smallDecimalItem = testPantryItem.copy(quantity = 0.001)
        repository.addPantryItem(smallDecimalItem)

        verify(ingredientDao).insertPantryItem(eq(smallDecimalItem))
    }

    // Ingredient name edge cases
    @Test
    fun `ingredient with numeric name`() = runTest {
        val numericIngredient = testIngredient.copy(name = "12345")
        repository.insertIngredient(numericIngredient)

        verify(ingredientDao).insertIngredient(eq(numericIngredient))
    }

    @Test
    fun `ingredient with emoji name`() = runTest {
        val emojiIngredient = testIngredient.copy(name = "番茄🍅")
        repository.insertIngredient(emojiIngredient)

        verify(ingredientDao).insertPantryItem(eq(emojiIngredient))
    }

    @Test
    fun `ingredient with mixed language name`() = runTest {
        val mixedIngredient = testIngredient.copy(name = "Tomato番茄")
        repository.insertIngredient(mixedIngredient)

        verify(ingredientDao).insertIngredient(eq(mixedIngredient))
    }

    // Update operations
    @Test
    fun `update pantry item to zero quantity`() = runTest {
        val updatedItem = testPantryItem.copy(quantity = 0.0)
        repository.updatePantryItem(updatedItem)

        verify(ingredientDao).updatePantryItem(eq(updatedItem))
    }

    @Test
    fun `update pantry item to expired`() = runTest {
        val expiredItem = testPantryItem.copy(
            expiryDate = System.currentTimeMillis() - 1000
        )
        repository.updatePantryItem(expiredItem)

        verify(ingredientDao).updatePantryItem(eq(expiredItem))
    }

    @Test
    fun `update ingredient category`() = runTest {
        val updatedIngredient = testIngredient.copy(
            category = IngredientCategory.MEAT
        )
        repository.updateIngredient(updatedIngredient)

        verify(ingredientDao).updateIngredient(eq(updatedIngredient))
    }

    @Test
    fun `update ingredient to all categories sequentially`() = runTest {
        IngredientCategory.values().forEach { category ->
            val updated = testIngredient.copy(category = category)
            repository.updateIngredient(updated)
            verify(ingredientDao).updateIngredient(eq(updated))
        }
    }
}
