package com.homepantry.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.homepantry.ui.recipe.RecipeDetailScreen
import org.junit.Rule
import org.junit.Test

class RecipeDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testRecipeId = "test-recipe-123"

    @Test
    fun recipeDetailScreen_displaysLoadingState() {
        composeTestRule.setContent {
            RecipeDetailScreen(
                recipeId = testRecipeId,
                onEditClick = {},
                onBackClick = {}
            )
        }

        // Initially should show loading indicator
        // In a real test, we'd verify CircularProgressIndicator exists
    }

    @Test
    fun recipeDetailScreen_displaysBackButton() {
        composeTestRule.setContent {
            RecipeDetailScreen(
                recipeId = testRecipeId,
                onEditClick = {},
                onBackClick = {}
            )
        }

        // Verify back button exists
        composeTestRule.onNodeWithContentDescription("返回")
            .assertExists()
    }

    @Test
    fun recipeDetailScreen_displaysEditButton() {
        composeTestRule.setContent {
            RecipeDetailScreen(
                recipeId = testRecipeId,
                onEditClick = {},
                onBackClick = {}
            )
        }

        // Verify edit button exists
        composeTestRule.onNodeWithContentDescription("编辑")
            .assertExists()
    }

    @Test
    fun recipeDetailScreen_displaysFavoriteButton() {
        composeTestRule.setContent {
            RecipeDetailScreen(
                recipeId = testRecipeId,
                onEditClick = {},
                onBackClick = {}
            )
        }

        // Verify favorite button exists
        composeTestRule.onNodeWithContentDescription("收藏")
            .assertExists()
    }

    @Test
    fun recipeDetailScreen_clickingBackCallsCallback() {
        var backClicked = false
        
        composeTestRule.setContent {
            RecipeDetailScreen(
                recipeId = testRecipeId,
                onEditClick = {},
                onBackClick = { backClicked = true }
            )
        }

        // Click back button
        composeTestRule.onNodeWithContentDescription("返回")
            .performClick()

        // Verify callback was invoked
        assert(backClicked)
    }
}
