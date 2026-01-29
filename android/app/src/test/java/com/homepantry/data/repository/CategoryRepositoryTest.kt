package com.homepantry.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.homepantry.data.dao.CategoryDao
import com.homepantry.data.database.HomePantryDatabase
import com.homepantry.testing.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
@OptIn(ExperimentalCoroutinesApi::class)
class CategoryRepositoryTest {

    private lateinit var database: HomePantryDatabase
    private lateinit var categoryDao: CategoryDao
    private lateinit var repository: CategoryRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HomePantryDatabase::class.java
        ).allowMainThreadQueries().build()
        categoryDao = database.categoryDao()
        repository = CategoryRepository(categoryDao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `getAllCategories returns empty list initially`() = runTest {
        val result = repository.getAllCategories().first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAllCategories returns all categories`() = runTest {
        val categories = (1..3).map { index ->
            TestDataBuilders.createCategory(
                id = "cat-$index",
                name = "分类$index"
            )
        }
        categories.forEach { repository.insertCategory(it) }

        val result = repository.getAllCategories().first()

        assertEquals(3, result.size)
    }

    @Test
    fun `getAllCategories emits updates when data changes`() = runTest {
        val category = TestDataBuilders.createCategory(id = "cat1", name = "分类1")

        repository.insertCategory(category)

        val result = repository.getAllCategories().first()

        assertEquals(1, result.size)
        assertEquals("cat1", result[0].id)
    }

    @Test
    fun `getCategoryById returns correct category`() = runTest {
        val category = TestDataBuilders.createCategory(
            id = "cat-1",
            name = "川菜"
        )
        repository.insertCategory(category)

        val result = repository.getCategoryById("cat-1")

        assertNotNull(result)
        assertEquals("cat-1", result?.id)
        assertEquals("川菜", result?.name)
    }

    @Test
    fun `getCategoryById returns null when not exists`() = runTest {
        val result = repository.getCategoryById("non-existent")

        assertNull(result)
    }

    @Test
    fun `insertCategory adds category to database`() = runTest {
        val category = TestDataBuilders.createCategory(
            id = "cat-1",
            name = "家常菜"
        )

        repository.insertCategory(category)

        val result = repository.getAllCategories().first()
        assertEquals(1, result.size)
        assertEquals("家常菜", result[0].name)
    }

    @Test
    fun `updateCategory updates existing category`() = runTest {
        val category = TestDataBuilders.createCategory(
            id = "cat-1",
            name = "原名称"
        )
        repository.insertCategory(category)

        val updated = category.copy(name = "新名称", sortOrder = 5)
        repository.updateCategory(updated)

        val result = repository.getCategoryById("cat-1")
        assertEquals("新名称", result?.name)
        assertEquals(5, result?.sortOrder)
    }

    @Test
    fun `deleteCategory removes category from database`() = runTest {
        val category = TestDataBuilders.createCategory(id = "cat-1", name = "要删除的")
        repository.insertCategory(category)

        repository.deleteCategory(category)

        val result = repository.getCategoryById("cat-1")
        assertNull(result)
    }

    @Test
    fun `multiple operations work correctly`() = runTest {
        // Insert multiple categories
        val cat1 = TestDataBuilders.createCategory(id = "cat1", name = "分类1", sortOrder = 1)
        val cat2 = TestDataBuilders.createCategory(id = "cat2", name = "分类2", sortOrder = 2)
        val cat3 = TestDataBuilders.createCategory(id = "cat3", name = "分类3", sortOrder = 3)

        repository.insertCategory(cat1)
        repository.insertCategory(cat2)
        repository.insertCategory(cat3)

        var result = repository.getAllCategories().first()
        assertEquals(3, result.size)

        // Update one category
        val updatedCat2 = cat2.copy(name = "更新后的分类2")
        repository.updateCategory(updatedCat2)

        var cat2Result = repository.getCategoryById("cat2")
        assertEquals("更新后的分类2", cat2Result?.name)

        // Delete one category
        repository.deleteCategory(cat1)

        result = repository.getAllCategories().first()
        assertEquals(2, result.size)
        assertEquals("cat2", result[0].id)
        assertEquals("cat3", result[1].id)
    }

    @Test
    fun `repository handles categories with special characters`() = runTest {
        val category = TestDataBuilders.createCategory(
            id = "cat-special",
            name = "特殊字符!@#$%^&*()"
        )

        repository.insertCategory(category)

        val result = repository.getCategoryById("cat-special")
        assertEquals("特殊字符!@#$%^&*()", result?.name)
    }

    @Test
    fun `repository handles categories with null values`() = runTest {
        val category = TestDataBuilders.createCategory(
            id = "cat-1",
            name = "无图标无颜色",
            icon = null,
            color = null
        )

        repository.insertCategory(category)

        val result = repository.getCategoryById("cat-1")
        assertNotNull(result)
        assertEquals(null, result?.icon)
        assertEquals(null, result?.color)
    }

    @Test
    fun `repository maintains sortOrder ordering`() = runTest {
        val categories = listOf(
            TestDataBuilders.createCategory(id = "cat3", name = "第三", sortOrder = 3),
            TestDataBuilders.createCategory(id = "cat1", name = "第一", sortOrder = 1),
            TestDataBuilders.createCategory(id = "cat2", name = "第二", sortOrder = 2)
        )
        categories.forEach { repository.insertCategory(it) }

        val result = repository.getAllCategories().first()

        assertEquals(3, result.size)
        assertEquals("第一", result[0].name)
        assertEquals("第二", result[1].name)
        assertEquals("第三", result[2].name)
    }

    @Test
    fun `getAllCategories categories are sorted by sortOrder`() = runTest {
        val categories = listOf(
            TestDataBuilders.createCategory(id = "cat-a", name = "A类", sortOrder = 2),
            TestDataBuilders.createCategory(id = "cat-b", name = "B类", sortOrder = 1),
            TestDataBuilders.createCategory(id = "cat-c", name = "C类", sortOrder = 3)
        )
        categories.forEach { repository.insertCategory(it) }

        val result = repository.getAllCategories().first()

        assertEquals("B类", result[0].name)  // sortOrder 1
        assertEquals("A类", result[1].name)  // sortOrder 2
        assertEquals("C类", result[2].name)  // sortOrder 3
    }

    @Test
    fun `repository handles emoji icons correctly`() = runTest {
        val category = TestDataBuilders.createCategory(
            id = "cat-1",
            name = "甜品",
            icon = "🍰"
        )

        repository.insertCategory(category)

        val result = repository.getCategoryById("cat-1")
        assertEquals("🍰", result?.icon)
    }

    @Test
    fun `repository handles color codes correctly`() = runTest {
        val category = TestDataBuilders.createCategory(
            id = "cat-1",
            name = "红色分类",
            color = "#FF5722"
        )

        repository.insertCategory(category)

        val result = repository.getCategoryById("cat-1")
        assertEquals("#FF5722", result?.color)
    }

    @Test
    fun `repository flow updates when category is added`() = runTest {
        val initialResult = repository.getAllCategories().first()
        assertTrue(initialResult.isEmpty())

        val category = TestDataBuilders.createCategory(id = "cat1", name = "新分类")
        repository.insertCategory(category)

        val updatedResult = repository.getAllCategories().first()
        assertEquals(1, updatedResult.size)
    }

    @Test
    fun `repository flow updates when category is deleted`() = runTest {
        val category = TestDataBuilders.createCategory(id = "cat1", name = "要删除的")
        repository.insertCategory(category)

        val beforeDelete = repository.getAllCategories().first()
        assertEquals(1, beforeDelete.size)

        repository.deleteCategory(category)

        val afterDelete = repository.getAllCategories().first()
        assertTrue(afterDelete.isEmpty())
    }

    @Test
    fun `repository flow updates when category is modified`() = runTest {
        val category = TestDataBuilders.createCategory(
            id = "cat1",
            name = "原名称"
        )
        repository.insertCategory(category)

        val beforeUpdate = repository.getAllCategories().first()
        assertEquals("原名称", beforeUpdate[0].name)

        val updated = category.copy(name = "新名称")
        repository.updateCategory(updated)

        val afterUpdate = repository.getAllCategories().first()
        assertEquals("新名称", afterUpdate[0].name)
    }

    @Test
    fun `repository handles negative sortOrder values`() = runTest {
        val categories = listOf(
            TestDataBuilders.createCategory(id = "cat1", sortOrder = -1),
            TestDataBuilders.createCategory(id = "cat2", sortOrder = 0),
            TestDataBuilders.createCategory(id = "cat3", sortOrder = 1)
        )
        categories.forEach { repository.insertCategory(it) }

        val result = repository.getAllCategories().first()

        assertEquals(-1, result[0].sortOrder)
        assertEquals(0, result[1].sortOrder)
        assertEquals(1, result[2].sortOrder)
    }
}
