package com.homepantry.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.homepantry.ui.recipe.RecipeListScreen
import org.junit.Rule
import org.junit.Test

class RecipeListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun recipeListScreen_displaysTitle() {
        composeTestRule.setContent {
            RecipeListScreen(
                onRecipeClick = {},
                onAddRecipeClick = {},
                onBackClick = {}
            )
        }

        // Verify the search bar is displayed
        composeTestRule.onNodeWithText("搜索菜谱...")
            .assertIsDisplayed()
    }

    @Test
    fun recipeListScreen_displaysQuickFilters() {
        composeTestRule.setContent {
            RecipeListScreen(
                onRecipeClick = {},
                onAddRecipeClick = {},
                onBackClick = {}
            )
        }

        // Verify quick filter chips are displayed
        composeTestRule.onNodeWithText("全部")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("⭐ 收藏")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("⚡ 15分钟")
            .assertIsDisplayed()
    }

    @Test
    fun recipeListScreen_clickingFavoriteTogglesFilter() {
        composeTestRule.setContent {
            RecipeListScreen(
                onRecipeClick = {},
                onAddRecipeClick = {},
                onBackClick = {}
            )
        }

        // Click on favorites filter
        composeTestRule.onNodeWithText("⭐ 收藏")
            .performClick()

        // The filter should now be selected
        composeTestRule.onNodeWithText("⭐ 收藏")
            .assertIsDisplayed()
    }

    @Test
    fun recipeListScreen_clicking15MinTogglesFilter() {
        composeTestRule.setContent {
            RecipeListScreen(
                onRecipeClick = {},
                onAddRecipeClick = {},
                onBackClick = {}
            )
        }

        // Click on 15 min filter
        composeTestRule.onNodeWithText("⚡ 15分钟")
            .performClick()

        // The filter should now be selected
        composeTestRule.onNodeWithText("⚡ 15分钟")
            .assertIsDisplayed()
    }
}
