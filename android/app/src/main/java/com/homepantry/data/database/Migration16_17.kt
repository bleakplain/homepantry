package com.homepantry.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 数据库迁移：添加收藏分类功能
 * 版本：16 -> 17
 */
val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 创建 folders 表
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS folders (
                id TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                icon TEXT,
                color TEXT,
                sort_order INTEGER NOT NULL DEFAULT 0,
                is_system INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
        """)

        // 创建 recipe_folders 表
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS recipe_folders (
                id TEXT PRIMARY KEY NOT NULL,
                recipe_id TEXT NOT NULL,
                folder_id TEXT NOT NULL,
                added_at INTEGER NOT NULL,
                FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE,
                FOREIGN KEY (folder_id) REFERENCES folders(id) ON DELETE CASCADE,
                UNIQUE (recipe_id, folder_id)
            )
        """)

        // 创建索引
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_recipe_folders_recipe_id
            ON recipe_folders(recipe_id)
        """)

        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_recipe_folders_folder_id
            ON recipe_folders(folder_id)
        """)

        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_folders_sort_order
            ON folders(sort_order)
        """)

        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_folders_name
            ON folders(name)
        """)

        // 创建系统默认收藏夹
        database.execSQL("""
            INSERT INTO folders (id, name, icon, color, sort_order, is_system, created_at, updated_at)
            VALUES (
                'default',
                '我的收藏',
                'star',
                '#FFD700',
                0,
                1,
                ${System.currentTimeMillis()},
                ${System.currentTimeMillis()}
            )
        """)
    }
}
