package com.homepantry.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.homepantry.ui.family.FamilyScreen
import com.homepantry.ui.home.HomeScreen
import com.homepantry.ui.ingredient.IngredientScreen
import com.homepantry.ui.mealplan.MealPlanScreen
import com.homepantry.ui.folder.FolderListScreen
import com.homepantry.ui.folder.FolderDetailScreen
import com.homepantry.ui.filter.FilterDialogScreen
// import com.homepantry.ui.recipe.*

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Recipes : Screen("recipes")
    object RecipeDetail : Screen("recipe/{recipeId}") {
        fun createRoute(recipeId: String) = "recipe/$recipeId"
    }
    object AddRecipe : Screen("add_recipe")
    object EditRecipe : Screen("edit_recipe/{recipeId}") {
        fun createRoute(recipeId: String) = "edit_recipe/$recipeId"
    }
    object Ingredients : Screen("ingredients")
    object MealPlan : Screen("mealplan")
    object Family : Screen("family")
    object FolderList : Screen("folders")
    object FolderDetail : Screen("folder/{folderId}") {
        fun createRoute(folderId: String) = "folder/$folderId"
    }
    object FilterDialog : Screen("filter_dialog")
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onRecipesClick = { navController.navigate(Screen.Recipes.route) },
                onIngredientsClick = { navController.navigate(Screen.Ingredients.route) },
                onMealPlanClick = { navController.navigate(Screen.MealPlan.route) },
                onFamilyClick = { navController.navigate(Screen.Family.route) },
                onFoldersClick = { navController.navigate(Screen.FolderList.route) },
                onSearchClick = { navController.navigate(Screen.Recipes.route) }
            )
        }

        composable(Screen.Recipes.route) {
            RecipeListScreen(
                onRecipeClick = { recipeId ->
                    navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                },
                onAddRecipeClick = {
                    navController.navigate(Screen.AddRecipe.route)
                },
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.RecipeDetail.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            val database = com.homepantry.data.database.HomePantryDatabase.getDatabase(
                androidx.compose.ui.platform.LocalContext.current
            )
            RecipeDetailScreen(
                recipeId = recipeId,
                folderDao = database.folderDao(),
                recipeFolderDao = database.recipeFolderDao(),
                onEditClick = { navController.navigate(Screen.EditRecipe.createRoute(recipeId)) },
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(Screen.AddRecipe.route) {
            AddRecipeScreen(
                onSave = { navController.navigateUp() },
                onCancel = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.EditRecipe.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            EditRecipeScreen(
                recipeId = recipeId,
                onSave = { navController.navigateUp() },
                onCancel = { navController.navigateUp() }
            )
        }

        composable(Screen.Ingredients.route) {
            IngredientScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(Screen.MealPlan.route) {
            MealPlanScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(Screen.Family.route) {
            FamilyScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(Screen.FolderList.route) {
            // 注入 database
            val database = com.homepantry.data.database.HomePantryDatabase.getDatabase(
                androidx.compose.ui.platform.LocalContext.current
            )
            FolderListScreen(
                folderDao = database.folderDao(),
                onFolderClick = { folderId ->
                    navController.navigate(Screen.FolderDetail.createRoute(folderId))
                },
                onCreateFolder = {
                    // 显示创建收藏夹对话框
                },
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.FolderDetail.route,
            arguments = listOf(navArgument("folderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val folderId = backStackEntry.arguments?.getString("folderId") ?: ""
            val database = com.homepantry.data.database.HomePantryDatabase.getDatabase(
                androidx.compose.ui.platform.LocalContext.current
            )
            FolderDetailScreen(
                folderDao = database.folderDao(),
                recipeDao = database.recipeDao(),
                folderId = folderId,
                onRecipeClick = { recipeId ->
                    navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                },
                onBackClick = { navController.navigateUp() }
            )
        }
    }

    composable(Screen.FilterDialog.route) {
        FilterDialogScreen(
            onDismiss = { navController.navigateUp() }
        )
    }

    composable(
