package com.homepantry.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.homepantry.data.dao.*
import com.homepantry.data.entity.*

@Database(
    entities = [
        Recipe::class,
        Ingredient::class,
        RecipeIngredient::class,
        RecipeInstruction::class,
        MealPlan::class,
        Category::class,
        PantryItem::class,
        RecipeRating::class
    ],
    version = 2,
    exportSchema = true
)
abstract class HomePantryDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun categoryDao(): CategoryDao
    abstract fun recipeRatingDao(): RecipeRatingDao

    companion object {
        private const val DATABASE_NAME = "homepantry.db"

        @Volatile
        private var INSTANCE: HomePantryDatabase? = null

        fun getDatabase(context: Context): HomePantryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HomePantryDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
