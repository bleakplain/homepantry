package com.homepantry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import com.homepantry.data.database.HomePantryDatabase
import com.homepantry.data.repository.*
import com.homepantry.navigation.AppNavigation
import com.homepantry.ui.theme.HomePantryTheme
import com.homepantry.viewmodel.RecipeViewModel

class MainActivity : ComponentActivity() {
    private lateinit var database: HomePantryDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize shared preferences
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

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
            val darkTheme = remember {
                mutableStateOf(
                    sharedPreferences.getBoolean("dark_theme", false)
                )
            }

            HomePantryTheme(darkTheme = darkTheme.value) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(navController = navController)
                }
            }
        }
    }
}
