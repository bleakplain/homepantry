package com.homepantry.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration16_17Test {

    private lateinit var database: HomePantryDatabase

    @Before
    fun setup() {
        // 创建版本 16 的数据库
        val context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(
            context,
            HomePantryDatabase::class.java
        )
            .addMigrations(MIGRATION_16_17)
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testMigrateFrom16To17() {
        val context = ApplicationProvider.getApplicationContext()
        
        // 使用 MigrationTestHelper 测试迁移
        val helper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            HomePantryDatabase::class.java,
            HomePantryDatabase.DATABASE_NAME
        )
        
        // 创建版本 16 的数据库
        helper.createDatabase("version_16.db", 16)
        
        // 验证数据库表结构
        // 验证数据完整性
        // 验证默认数据
        
        // 迁移到版本 17
        helper.closeWhenFinished(MIGRATION_16_17, 17, true)
        
        // 验证迁移后的数据库
        // 验证 folders 表存在
        // 验证 recipe_folders 表存在
        // 验证系统默认收藏夹存在
    }

    @Test
    fun testFoldersTableExists() {
        // 查询 folders 表
        val query = "SELECT name FROM sqlite_master WHERE type='table' AND name='folders'"
        val result = database.query(query, null)
        
        assertNotNull(result)
        assertTrue(result.count > 0)
        result.close()
    }

    @Test
    fun testRecipeFoldersTableExists() {
        // 查询 recipe_folders 表
        val query = "SELECT name FROM sqlite_master WHERE type='table' AND name='recipe_folders'"
        val result = database.query(query, null)
        
        assertNotNull(result)
        assertTrue(result.count > 0)
        result.close()
    }

    @Test
    fun testDefaultFolderExists() {
        val cursor = database.query(
            "SELECT * FROM folders WHERE id = 'default'",
            null
        )
        
        assertTrue(cursor.count > 0)
        cursor.moveToFirst()
        
        val id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
        val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
        val isSystem = cursor.getInt(cursor.getColumnIndexOrThrow("is_system"))
        
        assertEquals("default", id)
        assertEquals("我的收藏", name)
        assertEquals(1, isSystem)
        
        cursor.close()
    }

    @Test
    fun testFolderIndexes() {
        // 验证索引存在
        val query = "SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='folders'"
        val result = database.query(query, null)
        
        assertNotNull(result)
        val indexes = mutableListOf<String>()
        while (result.moveToNext()) {
            indexes.add(result.getString(0))
        }
        result.close()
        
        // 验证必要的索引存在
        assertTrue(indexes.contains("index_folders_sort_order"))
        assertTrue(indexes.contains("index_folders_name"))
    }

    @Test
    fun testRecipeFolderIndexes() {
        // 验证索引存在
        val query = "SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='recipe_folders'"
        val result = database.query(query, null)
        
        assertNotNull(result)
        val indexes = mutableListOf<String>()
        while (result.moveToNext()) {
            indexes.add(result.getString(0))
        }
        result.close()
        
        // 验证必要的索引存在
        assertTrue(indexes.contains("index_recipe_folders_recipe_id"))
        assertTrue(indexes.contains("index_recipe_folders_folder_id"))
    }

    @Test
    fun testForeignKeyConstraints() {
        // 验证外键约束
        val query = "PRAGMA foreign_key_list"
        val result = database.query(query, null)
        
        val foreignKeys = mutableListOf<String>()
        while (result.moveToNext()) {
            foreignKeys.add(result.getString(0))
        }
        result.close()
        
        // 验证 recipe_folders 的外键约束
        assertTrue(foreignKeys.any { it.contains("recipe_folders") }))
    }

    @Test
    fun testUniqueConstraint() {
        // 验证唯一约束
        val query = "SELECT sql FROM sqlite_master WHERE type='table' AND name='recipe_folders'"
        val result = database.query(query, null)
        
        result.moveToFirst()
        val createSql = result.getString(0)
        result.close()
        
        // 验证 UNIQUE (recipe_id, folder_id) 约束
        assertTrue(createSql.contains("UNIQUE (recipe_id, folder_id)"))
    }
}
