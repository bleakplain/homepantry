package com.homepantry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.homepantry.data.database.HomePantryDatabase
import com.homepantry.data.repository.*
import com.homepantry.ui.home.HomeScreen
import com.homepantry.ui.theme.HomePantryTheme
import com.homepantry.viewmodel.RecipeViewModel

class MainActivity : ComponentActivity() {
    private lateinit var database: HomePantryDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize database
        database = HomePantryDatabase.getDatabase(applicationContext)

        // Initialize repositories
        val recipeRepository = RecipeRepository(
            database.recipeDao(),
            database.ingredientDao()
        )
        val ingredientRepository = IngredientRepository(
            database.ingredientDao(),
            database.recipeDao()
        )
        val mealPlanRepository = MealPlanRepository(database.mealPlanDao())
        val categoryRepository = CategoryRepository(database.categoryDao())

        // Initialize ViewModels
        val recipeViewModel = RecipeViewModel(recipeRepository)

        setContent {
            HomePantryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen()
                }
            }
        }
    }
}
