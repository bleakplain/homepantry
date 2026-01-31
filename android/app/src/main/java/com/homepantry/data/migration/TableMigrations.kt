package com.homepantry.data.migration

import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Helper object for database table creation and migration operations.
 * Extracts common table creation patterns to improve maintainability.
 */
object TableMigrations {

    /**
     * Create an index on a table column if it doesn't exist.
     */
    fun createIndex(database: SupportSQLiteDatabase, indexName: String, sql: String) {
        database.execSQL("CREATE INDEX IF NOT EXISTS $indexName $sql")
    }

    /**
     * Add a column to a table with a default value.
     */
    fun addColumn(
        database: SupportSQLiteDatabase,
        table: String,
        column: String,
        type: String,
        defaultValue: Any? = null
    ) {
        val defaultClause = when (defaultValue) {
            is String -> "DEFAULT '$defaultValue'"
            is Number -> "DEFAULT $defaultValue"
            is Boolean -> "DEFAULT ${if (defaultValue) 1 else 0}"
            null -> ""
            else -> "DEFAULT $defaultValue"
        }
        database.execSQL("ALTER TABLE $table ADD COLUMN $column $type $defaultClause")
    }

    /**
     * Create recipe_notes table
     */
    fun createRecipeNotesTable(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS recipe_notes (
                id TEXT PRIMARY KEY NOT NULL,
                recipeId TEXT NOT NULL,
                cookingDate INTEGER NOT NULL,
                rating INTEGER NOT NULL,
                note TEXT,
                images TEXT NOT NULL,
                success INTEGER NOT NULL,
                modifications TEXT,
                createdAt INTEGER NOT NULL,
                FOREIGN KEY(recipeId) REFERENCES recipes(id) ON DELETE CASCADE
            )
        """)
        createIndex(database, "index_recipe_notes_recipeId", "ON recipe_notes(recipeId)")
        createIndex(database, "index_recipe_notes_cookingDate", "ON recipe_notes(cookingDate)")
    }

    /**
     * Create nutrition_info table
     */
    fun createNutritionInfoTable(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS nutrition_info (
                id TEXT PRIMARY KEY NOT NULL,
                recipeId TEXT NOT NULL,
                calories INTEGER,
                protein REAL,
                fat REAL,
                carbs REAL,
                fiber REAL,
                sugar REAL,
                sodium INTEGER,
                servingSize INTEGER NOT NULL,
                FOREIGN KEY(recipeId) REFERENCES recipes(id) ON DELETE CASCADE
            )
        """)
        createIndex(database, "index_nutrition_info_recipeId", "ON nutrition_info(recipeId)")
    }

    /**
     * Create user_profiles table
     */
    fun createUserProfilesTable(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS user_profiles (
                id TEXT PRIMARY KEY NOT NULL,
                preferredCuisines TEXT NOT NULL,
                spiceLevel TEXT NOT NULL,
                flavorPreferences TEXT NOT NULL,
                dislikedIngredients TEXT NOT NULL,
                dietaryRestrictions TEXT NOT NULL,
                allergies TEXT NOT NULL,
                healthGoal TEXT,
                weekdayCookingTime INTEGER,
                weekendCookingTime INTEGER,
                dailyRecommendationEnabled INTEGER NOT NULL,
                dailyRecommendationTime TEXT NOT NULL,
                mealPlanReminderEnabled INTEGER NOT NULL,
                shoppingReminderEnabled INTEGER NOT NULL,
                shoppingReminderDay INTEGER NOT NULL,
                expiryReminderEnabled INTEGER NOT NULL,
                expiryReminderDays INTEGER NOT NULL,
                unitSystem TEXT NOT NULL,
                theme TEXT NOT NULL,
                language TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
        """)
    }

    /**
     * Create family_members table
     */
    fun createFamilyMembersTable(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS family_members (
                id TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                relationship TEXT NOT NULL,
                age INTEGER,
                preferredCuisines TEXT NOT NULL,
                dislikedIngredients TEXT NOT NULL,
                allergies TEXT NOT NULL,
                favoriteRecipes TEXT NOT NULL,
                dislikedRecipes TEXT NOT NULL,
                notes TEXT,
                createdAt INTEGER NOT NULL
            )
        """)
    }

    /**
     * Create shopping_lists table
     */
    fun createShoppingListsTable(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS shopping_lists (
                id TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                date INTEGER NOT NULL,
                items TEXT NOT NULL,
                isCompleted INTEGER NOT NULL,
                totalEstimated REAL,
                actualTotal REAL,
                store TEXT,
                mealPlanIds TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                completedAt INTEGER
            )
        """)
        createIndex(database, "index_shopping_lists_date", "ON shopping_lists(date)")
        createIndex(database, "index_shopping_lists_isCompleted", "ON shopping_lists(isCompleted)")
    }

    /**
     * Create shopping_items table
     */
    fun createShoppingItemsTable(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS shopping_items (
                id TEXT PRIMARY KEY NOT NULL,
                listId TEXT NOT NULL,
                name TEXT NOT NULL,
                quantity REAL NOT NULL,
                unit TEXT NOT NULL,
                category TEXT NOT NULL,
                estimatedPrice REAL,
                actualPrice REAL,
                isPurchased INTEGER NOT NULL,
                isChecked INTEGER NOT NULL,
                notes TEXT,
                sortOrder INTEGER NOT NULL,
                recipeIds TEXT NOT NULL,
                FOREIGN KEY(listId) REFERENCES shopping_lists(id) ON DELETE CASCADE
            )
        """)
        createIndex(database, "index_shopping_items_listId", "ON shopping_items(listId)")
        createIndex(database, "index_shopping_items_category", "ON shopping_items(category)")
    }

    /**
     * Add columns to pantry_items table
     */
    fun addPantryItemColumns(database: SupportSQLiteDatabase) {
        addColumn(database, "pantry_items", "storageLocation", "TEXT NOT NULL", "PANTRY")
        addColumn(database, "pantry_items", "name", "TEXT")
    }

    /**
     * Add columns to recipes table
     */
    fun addRecipeColumns(database: SupportSQLiteDatabase) {
        addColumn(database, "recipes", "images", "TEXT NOT NULL", "[]")
        addColumn(database, "recipes", "prepTime", "INTEGER")
        addColumn(database, "recipes", "cookingCount", "INTEGER NOT NULL", 0)
        addColumn(database, "recipes", "averageRating", "REAL NOT NULL", 0.0)
        addColumn(database, "recipes", "lastCookedAt", "INTEGER")
        addColumn(database, "recipes", "updatedAt", "INTEGER NOT NULL", 0)
        addColumn(database, "recipes", "isPublic", "INTEGER NOT NULL", 0)
    }

    /**
     * Add columns to recipe_instructions table
     */
    fun addRecipeInstructionColumns(database: SupportSQLiteDatabase) {
        addColumn(database, "recipe_instructions", "duration", "INTEGER")
        addColumn(database, "recipe_instructions", "temperature", "INTEGER")
        addColumn(database, "recipe_instructions", "isKeyStep", "INTEGER NOT NULL", 0)
        addColumn(database, "recipe_instructions", "reminder", "TEXT")
    }
}
