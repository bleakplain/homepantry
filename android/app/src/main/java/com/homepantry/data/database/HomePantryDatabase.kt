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
        ShoppingItem::class
    ],
    version = 3,
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
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
