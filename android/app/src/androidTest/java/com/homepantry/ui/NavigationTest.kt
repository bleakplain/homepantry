package com.homepantry.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.homepantry.navigation.AppNavigation
import com.homepantry.ui.theme.HomePantryTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun app_displays_home_screen_on_launch() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            HomePantryTheme {
                AppNavigation(navController = navController)
            }
        }

        // Verify home screen elements are displayed
        composeTestRule.onNodeWithText("家常味库").assertExists()
        composeTestRule.onNodeWithText("菜谱").assertExists()
        composeTestRule.onNodeWithText("食材").assertExists()
        composeTestRule.onNodeWithText("计划").assertExists()
        composeTestRule.onNodeWithText("家庭").assertExists()
    }

    @Test
    fun clicking_recipes_navigates_to_recipes_screen() {
        lateinit var navController: androidx.navigation.NavController

        composeTestRule.setContent {
            navController = rememberNavController()
            HomePantryTheme {
                AppNavigation(navController = navController)
            }
        }

        // Click on recipes card
        composeTestRule.onNodeWithText("菜谱").performClick()

        // Verify navigation to recipes screen
        assert(navController.currentDestination?.route == "recipes")
    }

    @Test
    fun clicking_ingredients_navigates_to_ingredients_screen() {
        lateinit var navController: androidx.navigation.NavController

        composeTestRule.setContent {
            navController = rememberNavController()
            HomePantryTheme {
                AppNavigation(navController = navController)
            }
        }

        // Click on ingredients card
        composeTestRule.onNodeWithText("食材").performClick()

        // Verify navigation to ingredients screen
        assert(navController.currentDestination?.route == "ingredients")
    }

    @Test
    fun clicking_meal_plan_navigates_to_meal_plan_screen() {
        lateinit var navController: androidx.navigation.NavController

        composeTestRule.setContent {
            navController = rememberNavController()
            HomePantryTheme {
                AppNavigation(navController = navController)
            }
        }

        // Click on meal plan card
        composeTestRule.onNodeWithText("计划").performClick()

        // Verify navigation to meal plan screen
        assert(navController.currentDestination?.route == "mealplan")
    }

    @Test
    fun clicking_family_navigates_to_family_screen() {
        lateinit var navController: androidx.navigation.NavController

        composeTestRule.setContent {
            navController = rememberNavController()
            HomePantryTheme {
                AppNavigation(navController = navController)
            }
        }

        // Click on family card
        composeTestRule.onNodeWithText("家庭").performClick()

        // Verify navigation to family screen
        assert(navController.currentDestination?.route == "family")
    }
}
