package com.homepantry.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
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
class CategoryDaoTest {

    private lateinit var database: HomePantryDatabase
    private lateinit var categoryDao: CategoryDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HomePantryDatabase::class.java
        ).allowMainThreadQueries().build()
        categoryDao = database.categoryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insertCategory adds category to database`() = runTest {
        val category = TestDataBuilders.createCategory(
            id = "cat-1",
            name = "家常菜"
        )

        categoryDao.insertCategory(category)

        val result = categoryDao.getAllCategories().first()
        assertEquals(1, result.size)
        assertEquals("cat-1", result[0].id)
        assertEquals("家常菜", result[0].name)
    }

    @Test
    fun `insertCategory with existing id updates category`() = runTest {
        val originalCategory = TestDataBuilders.createCategory(
            id = "cat-1",
            name = "原名称",
            color = "#FF0000"
        )
        categoryDao.insertCategory(originalCategory)

        val updatedCategory = TestDataBuilders.createCategory(
            id = "cat-1",
            name = "新名称",
            color = "#00FF00"
        )
        categoryDao.insertCategory(updatedCategory)

        val result = categoryDao.getAllCategories().first()
        assertEquals(1, result.size)
        assertEquals("新名称", result[0].name)
        assertEquals("#00FF00", result[0].color)
    }

    @Test
    fun `getCategoryById returns correct category`() = runTest {
        val category = TestDataBuilders.createCategory(
            id = "cat-1",
            name = "川菜"
        )
        categoryDao.insertCategory(category)

        val result = categoryDao.getCategoryById("cat-1")

        assertNotNull(result)
        assertEquals("cat-1", result?.id)
        assertEquals("川菜", result?.name)
    }

    @Test
    fun `getCategoryById returns null when category not exists`() = runTest {
        val result = categoryDao.getCategoryById("non-existent")

        assertNull(result)
    }

    @Test
    fun `getAllCategories returns empty list when no categories`() = runTest {
        val result = categoryDao.getAllCategories().first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAllCategories returns all categories`() = runTest {
        val categories = (1..3).map { index ->
            TestDataBuilders.createCategory(
                id = "cat-$index",
                name = "分类$index",
                sortOrder = index
            )
        }
        categories.forEach { categoryDao.insertCategory(it) }

        val result = categoryDao.getAllCategories().first()

        assertEquals(3, result.size)
    }

    @Test
    fun `getAllCategories orders by sortOrder ascending`() = runTest {
        val categories = listOf(
            TestDataBuilders.createCategory(id = "cat1", name = "第三", sortOrder = 3),
            TestDataBuilders.createCategory(id = "cat2", name = "第一", sortOrder = 1),
            TestDataBuilders.createCategory(id = "cat3", name = "第二", sortOrder = 2)
        )
        categories.forEach { categoryDao.insertCategory(it) }

        val result = categoryDao.getAllCategories().first()

        assertEquals(3, result.size)
        assertEquals("第一", result[0].name)
        assertEquals("第二", result[1].name)
        assertEquals("第三", result[2].name)
    }

    @Test
    fun `getAllCategories emits updates when category added`() = runTest {
        val category1 = TestDataBuilders.createCategory(id = "cat1", name = "分类1")
        val category2 = TestDataBuilders.createCategory(id = "cat2", name = "分类2")

        categoryDao.insertCategory(category1)

        val firstResult = categoryDao.getAllCategories().first()
        assertEquals(1, firstResult.size)

        categoryDao.insertCategory(category2)

        val secondResult = categoryDao.getAllCategories().first()
        assertEquals(2, secondResult.size)
    }

    @Test
    fun `updateCategory updates category data`() = runTest {
        val category = TestDataBuilders.createCategory(
            id = "cat-1",
            name = "原名称",
            icon = "🍳",
            color = "#FF5722",
            sortOrder = 0
        )
        categoryDao.insertCategory(category)

        val updated = category.copy(
            name = "新名称",
            icon = "🥗",
            color = "#4CAF50",
            sortOrder = 5
        )
        categoryDao.updateCategory(updated)

        val result = categoryDao.getCategoryById("cat-1")
        assertEquals("新名称", result?.name)
        assertEquals("🥗", result?.icon)
        assertEquals("#4CAF50", result?.color)
        assertEquals(5, result?.sortOrder)
    }

    @Test
    fun `updateCategory with non-existing id does nothing`() = runTest {
        val category = TestDataBuilders.createCategory(id = "cat-1", name = "分类1")
        categoryDao.insertCategory(category)

        val nonExistingCategory = TestDataBuilders.createCategory(
            id = "non-existent",
            name = "不存在的分类"
        )
        categoryDao.updateCategory(nonExistingCategory)

        val result = categoryDao.getAllCategories().first()
        assertEquals(1, result.size)
        assertEquals("cat-1", result[0].id)
    }

    @Test
    fun `deleteCategory removes category from database`() = runTest {
        val category = TestDataBuilders.createCategory(id = "cat-1", name = "要删除的分类")
        categoryDao.insertCategory(category)

        categoryDao.deleteCategory(category)

        val result = categoryDao.getCategoryById("cat-1")
        assertNull(result)
    }

    @Test
    fun `deleteCategory removes from getAllCategories result`() = runTest {
        val category1 = TestDataBuilders.createCategory(id = "cat1", name = "分类1")
        val category2 = TestDataBuilders.createCategory(id = "cat2", name = "分类2")
        categoryDao.insertCategory(category1)
        categoryDao.insertCategory(category2)

        categoryDao.deleteCategory(category1)

        val result = categoryDao.getAllCategories().first()
        assertEquals(1, result.size)
        assertEquals("cat2", result[0].id)
    }

    @Test
    fun `deleteCategory with non-existing id does nothing`() = runTest {
        val category1 = TestDataBuilders.createCategory(id = "cat1", name = "分类1")
        categoryDao.insertCategory(category1)

        val nonExistingCategory = TestDataBuilders.createCategory(
            id = "non-existent",
            name = "不存在的分类"
        )
        categoryDao.deleteCategory(nonExistingCategory)

        val result = categoryDao.getAllCategories().first()
        assertEquals(1, result.size)
        assertEquals("cat1", result[0].id)
    }

    @Test
    fun `category with null icon is handled correctly`() = runTest {
        val category = TestDataBuilders.createCategory(
            id = "cat-1",
            name = "无图标分类",
            icon = null
        )
        categoryDao.insertCategory(category)

        val result = categoryDao.getCategoryById("cat-1")
        assertNotNull(result)
        assertEquals("cat-1", result?.id)
        assertEquals(null, result?.icon)
    }

    @Test
    fun `category with null color is handled correctly`() = runTest {
        val category = TestDataBuilders.createCategory(
            id = "cat-1",
            name = "无颜色分类",
            color = null
        )
        categoryDao.insertCategory(category)

        val result = categoryDao.getCategoryById("cat-1")
        assertNotNull(result)
        assertEquals("cat-1", result?.id)
        assertEquals(null, result?.color)
    }

    @Test
    fun `multiple categories with same sortOrder maintain insertion order`() = runTest {
        val categories = listOf(
            TestDataBuilders.createCategory(id = "cat1", name = "分类1", sortOrder = 1),
            TestDataBuilders.createCategory(id = "cat2", name = "分类2", sortOrder = 1),
            TestDataBuilders.createCategory(id = "cat3", name = "分类3", sortOrder = 1)
        )
        categories.forEach { categoryDao.insertCategory(it) }

        val result = categoryDao.getAllCategories().first()

        assertEquals(3, result.size)
        assertTrue(result.all { it.sortOrder == 1 })
    }

    @Test
    fun `category names can contain special characters`() = runTest {
        val category = TestDataBuilders.createCategory(
            id = "cat-1",
            name = "特殊字符!@#$%^&*()"
        )
        categoryDao.insertCategory(category)

        val result = categoryDao.getCategoryById("cat-1")
        assertEquals("特殊字符!@#$%^&*()", result?.name)
    }

    @Test
    fun `category icons can contain emojis`() = runTest {
        val categories = listOf(
            TestDataBuilders.createCategory(id = "cat1", name = "菜1", icon = "🍳"),
            TestDataBuilders.createCategory(id = "cat2", name = "菜2", icon = "🥗"),
            TestDataBuilders.createCategory(id = "cat3", name = "菜3", icon = "🍜"),
            TestDataBuilders.createCategory(id = "cat4", name = "菜4", icon = "🍰")
        )
        categories.forEach { categoryDao.insertCategory(it) }

        val result = categoryDao.getAllCategories().first()

        assertEquals(4, result.size)
        assertEquals("🍳", result[0].icon)
        assertEquals("🥗", result[1].icon)
        assertEquals("🍜", result[2].icon)
        assertEquals("🍰", result[3].icon)
    }

    @Test
    fun `category color codes are stored correctly`() = runTest {
        val colors = listOf(
            "#FF5722",
            "#4CAF50",
            "#2196F3",
            "#FFC107",
            "#9C27B0"
        )

        colors.forEachIndexed { index, color ->
            val category = TestDataBuilders.createCategory(
                id = "cat$index",
                color = color
            )
            categoryDao.insertCategory(category)
        }

        colors.forEachIndexed { index, color ->
            val result = categoryDao.getCategoryById("cat$index")
            assertEquals(color, result?.color)
        }
    }

    @Test
    fun `negative sortOrder is handled correctly`() = runTest {
        val categories = listOf(
            TestDataBuilders.createCategory(id = "cat1", sortOrder = -1),
            TestDataBuilders.createCategory(id = "cat2", sortOrder = 0),
            TestDataBuilders.createCategory(id = "cat3", sortOrder = 1)
        )
        categories.forEach { categoryDao.insertCategory(it) }

        val result = categoryDao.getAllCategories().first()

        assertEquals(3, result.size)
        assertEquals("cat1", result[0].id)  // -1 comes first
        assertEquals("cat2", result[1].id)  // 0 comes second
        assertEquals("cat3", result[2].id)  // 1 comes third
    }
}
