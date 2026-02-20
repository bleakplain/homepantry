package com.homepantry.data.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.homepantry.data.entity.RecipeFolder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecipeFolderDaoTest {

    private lateinit var database: com.homepantry.data.database.HomePantryDatabase
    private lateinit var recipeFolderDao: RecipeFolderDao
    private lateinit var folderDao: FolderDao
    private lateinit var recipeDao: RecipeDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            com.homepantry.data.database.HomePantryDatabase::class.java
        ).build()
        recipeFolderDao = database.recipeFolderDao()
        folderDao = database.folderDao()
        recipeDao = database.recipeDao()

        // 创建测试数据
        val folder = Folder(
            id = "test-folder",
            name = "测试收藏夹"
        )
        folderDao.insert(folder)

        val recipe = com.homepantry.data.entity.Recipe(
            name = "测试菜谱",
            cookingTime = 30,
            servings = 2,
            difficulty = com.homepantry.data.entity.DifficultyLevel.EASY
        )
        recipeDao.insert(recipe)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInsertRecipeFolder() = runTest {
        val recipeFolder = RecipeFolder(
            id = "test-rf-1",
            recipeId = "test-recipe",
            folderId = "test-folder"
        )

        recipeFolderDao.insert(recipeFolder)

        val exists = recipeFolderDao.exists("test-recipe", "test-folder")
        assertTrue(exists > 0)
    }

    @Test
    fun testInsertDuplicateThrowsException() = runTest {
        val recipeFolder1 = RecipeFolder(
            id = "test-rf-2",
            recipeId = "test-recipe",
            folderId = "test-folder"
        )

        val recipeFolder2 = RecipeFolder(
            id = "test-rf-3",
            recipeId = "test-recipe",
            folderId = "test-folder"
        )

        recipeFolderDao.insert(recipeFolder1)

        var exception: Exception? = null
        try {
            recipeFolderDao.insert(recipeFolder2)
        } catch (e: SQLiteConstraintException) {
            exception = e
        }

        assertNotNull(exception)
    }

    @Test
    fun testDeleteRecipeFolder() = runTest {
        val recipeFolder = RecipeFolder(
            id = "test-rf-4",
            recipeId = "test-recipe",
            folderId = "test-folder"
        )

        recipeFolderDao.insert(recipeFolder)

        var exists = recipeFolderDao.exists("test-recipe", "test-folder")
        assertTrue(exists > 0)

        recipeFolderDao.delete(recipeFolder)

        exists = recipeFolderDao.exists("test-recipe", "test-folder")
        assertEquals(0, exists)
    }

    @Test
    fun testDeleteByFolderId() = runTest {
        val recipeFolder1 = RecipeFolder(
            id = "test-rf-5",
            recipeId = "test-recipe-1",
            folderId = "test-folder"
        )
        val recipeFolder2 = RecipeFolder(
            id = "test-rf-6",
            recipeId = "test-recipe-2",
            folderId = "test-folder"
        )

        recipeFolderDao.insertAll(listOf(recipeFolder1, recipeFolder2))

        var count = recipeFolderDao.exists("test-recipe-1", "test-folder")
        assertTrue(count > 0)

        recipeFolderDao.deleteByFolderId("test-folder")

        count = recipeFolderDao.exists("test-recipe-1", "test-folder")
        assertEquals(0, count)
    }

    @Test
    fun testGetRecipeIdsByFolderId() = runTest {
        val recipeFolder1 = RecipeFolder(
            id = "test-rf-7",
            recipeId = "test-recipe-1",
            folderId = "test-folder"
        )
        val recipeFolder2 = RecipeFolder(
            id = "test-rf-8",
            recipeId = "test-recipe-2",
            folderId = "test-folder"
        )

        recipeFolderDao.insertAll(listOf(recipeFolder1, recipeFolder2))

        val recipeIds = recipeFolderDao.getRecipeIdsByFolderId("test-folder")
        assertEquals(2, recipeIds.size)
        assertTrue(recipeIds.contains("test-recipe-1"))
        assertTrue(recipeIds.contains("test-recipe-2"))
    }

    @Test
    fun testGetFolderIdsByRecipeId() = runTest {
        val recipeFolder1 = RecipeFolder(
            id = "test-rf-9",
            recipeId = "test-recipe",
            folderId = "test-folder-1"
        )
        val recipeFolder2 = RecipeFolder(
            id = "test-rf-10",
            recipeId = "test-recipe",
            folderId = "test-folder-2"
        )

        recipeFolderDao.insertAll(listOf(recipeFolder1, recipeFolder2))

        val folderIds = recipeFolderDao.getFolderIdsByRecipeId("test-recipe")
        assertEquals(2, folderIds.size)
        assertTrue(folderIds.contains("test-folder-1"))
        assertTrue(folderIds.contains("test-folder-2"))
    }
}
