package com.homepantry.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.homepantry.data.entity.Folder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FolderDaoTest {

    private lateinit var database: com.homepantry.data.database.HomePantryDatabase
    private lateinit var folderDao: FolderDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            com.homepantry.data.database.HomePantryDatabase::class.java
        ).build()
        folderDao = database.folderDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInsertFolder() = runTest {
        val folder = Folder(
            id = "test-folder-1",
            name = "测试收藏夹",
            icon = "star",
            color = "#FF6B35"
        )

        folderDao.insert(folder)

        val retrieved = folderDao.getFolderById(folder.id)
        assertNotNull(retrieved)
        assertEquals(folder.name, retrieved?.name)
    }

    @Test
    fun testUpdateFolder() = runTest {
        val folder = Folder(
            id = "test-folder-2",
            name = "原始名称"
        )

        folderDao.insert(folder)

        val updated = folder.copy(name = "更新后的名称")
        folderDao.update(updated)

        val retrieved = folderDao.getFolderById(folder.id)
        assertEquals("更新后的名称", retrieved?.name)
    }

    @Test
    fun testDeleteFolder() = runTest {
        val folder = Folder(
            id = "test-folder-3",
            name = "要删除的收藏夹"
        )

        folderDao.insert(folder)
        var retrieved = folderDao.getFolderById(folder.id)
        assertNotNull(retrieved)

        folderDao.deleteById(folder.id)
        retrieved = folderDao.getFolderById(folder.id)
        assertNull(retrieved)
    }

    @Test
    fun testGetMaxSortOrder() = runTest {
        val folder1 = Folder(id = "f1", name = "A", sortOrder = 0)
        val folder2 = Folder(id = "f2", name = "B", sortOrder = 1)
        val folder3 = Folder(id = "f3", name = "C", sortOrder = 2)

        folderDao.insert(folder1)
        folderDao.insert(folder2)
        folderDao.insert(folder3)

        val maxSortOrder = folderDao.getMaxSortOrder()
        assertEquals(2, maxSortOrder)
    }

    @Test
    fun testGetAllFolders() = runTest {
        val folder1 = Folder(id = "f1", name = "收藏夹1", sortOrder = 0)
        val folder2 = Folder(id = "f2", name = "收藏夹2", sortOrder = 1)

        folderDao.insert(folder1)
        folderDao.insert(folder2)

        val folders = folderDao.getAllFoldersOnce()
        assertEquals(2, folders.size)
        assertEquals("收藏夹1", folders[0].name)
        assertEquals("收藏夹2", folders[1].name)
    }

    @Test
    fun testUpdateSortOrder() = runTest {
        val folder1 = Folder(id = "f1", name = "A", sortOrder = 0)
        val folder2 = Folder(id = "f2", name = "B", sortOrder = 1)

        folderDao.insert(folder1)
        folderDao.insert(folder2)

        folderDao.updateSortOrder("f1", 10)

        val folders = folderDao.getAllFoldersOnce()
        assertEquals(10, folders.find { it.id == "f1" }?.sortOrder)
    }

    @Test
    fun testSearchFolders() = runTest {
        val folder1 = Folder(id = "f1", name = "川菜")
        val folder2 = Folder(id = "f2", name = "粤菜")
        val folder3 = Folder(id = "f3", name = "湘菜")

        folderDao.insert(folder1)
        folderDao.insert(folder2)
        folderDao.insert(folder3)

        val results = folderDao.searchFolders("菜").first()
        assertEquals(3, results.size)
    }
}
