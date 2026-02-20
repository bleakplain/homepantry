package com.homepantry.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.homepantry.data.dao.*
import com.homepantry.data.entity.*
import com.homepantry.data.migration.TableMigrations

@Database(
    entities = [
        // Existing entities
        Recipe::class,
        Ingredient::class,
        RecipeIngredient::class,
        RecipeInstruction::class,
        MealPlan::class,
        Category::class,
        PantryItem::class,
        RecipeRating::class,
        // New entities
        RecipeNote::class,
        NutritionInfo::class,
        UserProfile::class,
        FamilyMember::class,
        ShoppingList::class,
        ShoppingItem::class,
        // Folder entities
        Folder::class,
        RecipeFolder::class,
        // Recipe Filter entities
        RecipeFilter::class,

        // Expiration Reminder entities
        ExpirationReminder::class,
        ExpirationNotification::class
    ],
    version = 19,
    exportSchema = true
)
abstract class HomePantryDatabase : RoomDatabase() {
    // Existing DAOs
    abstract fun recipeDao(): RecipeDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun categoryDao(): CategoryDao
    abstract fun recipeRatingDao(): RecipeRatingDao

    // New DAOs
    abstract fun recipeNoteDao(): RecipeNoteDao
    abstract fun nutritionInfoDao(): NutritionInfoDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun familyMemberDao(): FamilyMemberDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun folderDao(): FolderDao
    abstract fun recipeFolderDao(): RecipeFolderDao
    abstract fun recipeFilterDao(): RecipeFilterDao

    // Expiration Reminder DAOs
    abstract fun pantryItemDao(): PantryItemDao
    abstract fun expirationReminderDao(): ExpirationReminderDao
    abstract fun expirationNotificationDao(): ExpirationNotificationDao

    companion object {
        private const val DATABASE_NAME = "homepantry.db"

        // Database migration: version 2 -> 3
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create new tables
                TableMigrations.createRecipeNotesTable(database)
                TableMigrations.createNutritionInfoTable(database)
                TableMigrations.createUserProfilesTable(database)
                TableMigrations.createFamilyMembersTable(database)
                TableMigrations.createShoppingListsTable(database)
                TableMigrations.createShoppingItemsTable(database)

                // Add columns to existing tables
                TableMigrations.addPantryItemColumns(database)
                TableMigrations.addRecipeColumns(database)
                TableMigrations.addRecipeInstructionColumns(database)
            }
        }

        // Database migration: version 16 -> 17
        private val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create folders table
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

                // Create recipe_folders table
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

                // Create indexes
                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_recipe_folders_recipe_id
                    ON recipe_folders(recipe_id)
                """)

                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_recipe_folders_folder_id
                    ON recipe_folders(folder_id)
                """)

                // Create system default folder
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

        // Database migration: version 17 -> 18
        private val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create recipe_filters table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS recipe_filters (
                        id TEXT PRIMARY KEY NOT NULL,
                        cooking_time_min INTEGER,
                        cooking_time_max INTEGER,
                        difficulty_min TEXT,
                        difficulty_max TEXT,
                        included_ingredients TEXT,
                        excluded_ingredients TEXT,
                        category_ids TEXT,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL
                    )
                """)

                // Create indexes
                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_recipe_filters_created_at
                    ON recipe_filters(created_at)
                """)
            }
        }

        // Database migration: version 18 -> 19
        private val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create expiration_reminders table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS expiration_reminders (
                        id TEXT PRIMARY KEY NOT NULL,
                        pantry_item_id TEXT NOT NULL,
                        reminder_days INTEGER NOT NULL,
                        reminder_time TEXT NOT NULL,
                        reminder_frequency TEXT NOT NULL,
                        is_enabled INTEGER NOT NULL DEFAULT 1,
                        last_notified_date INTEGER,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL,
                        FOREIGN KEY (pantry_item_id) REFERENCES pantry_items(id) ON DELETE CASCADE
                    )
                """)

                // Create expiration_notifications table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS expiration_notifications (
                        id TEXT PRIMARY KEY NOT NULL,
                        pantry_item_id TEXT NOT NULL,
                        notification_date INTEGER NOT NULL,
                        notification_type TEXT NOT NULL,
                        is_read INTEGER NOT NULL DEFAULT 0,
                        is_handled INTEGER NOT NULL DEFAULT 0,
                        created_at INTEGER NOT NULL,
                        FOREIGN KEY (pantry_item_id) REFERENCES pantry_items(id) ON DELETE CASCADE
                    )
                """)

                // Create indexes
                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_expiration_reminders_pantry_item_id
                    ON expiration_reminders(pantry_item_id)
                """)

                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_expiration_reminders_is_enabled_last_notified
                    ON expiration_reminders(is_enabled, last_notified_date)
                """)

                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_expiration_notifications_pantry_item_notification_date
                    ON expiration_notifications(pantry_item_id, notification_date)
                """)

                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_expiration_notifications_is_read_is_handled
                    ON expiration_notifications(is_read, is_handled)
                """)
            }
        }

        @Volatile
        private var INSTANCE: HomePantryDatabase? = null

        fun getDatabase(context: Context): HomePantryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HomePantryDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_2_3)
                    .addMigrations(MIGRATION_16_17)
                    .addMigrations(MIGRATION_17_18)
                    .addMigrations(MIGRATION_18_19)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
