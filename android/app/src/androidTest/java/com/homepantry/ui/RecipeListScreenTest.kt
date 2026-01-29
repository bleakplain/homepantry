package com.homepantry.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.homepantry.ui.recipe.RecipeListScreen
import com.homepantry.ui.theme.HomePantryTheme
import org.junit.Rule
import org.junit.Test

class RecipeListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun recipeListScreen_displays_title() {
        composeTestRule.setContent {
            HomePantryTheme {
                RecipeListScreen(
                    onRecipeClick = {},
                    onAddRecipeClick = {},
                    onBackClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("菜谱").assertIsDisplayed()
    }

    @Test
    fun recipeListScreen_displays_search_placeholder() {
        composeTestRule.setContent {
            HomePantryTheme {
                RecipeListScreen(
                    onRecipeClick = {},
                    onAddRecipeClick = {},
                    onBackClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("搜索菜谱...").assertIsDisplayed()
    }

    @Test
    fun recipeListScreen_displays_filter_chips() {
        composeTestRule.setContent {
            HomePantryTheme {
                RecipeListScreen(
                    onRecipeClick = {},
                    onAddRecipeClick = {},
                    onBackClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("全部").assertExists()
        composeTestRule.onNodeWithText("家常菜").assertExists()
        composeTestRule.onNodeWithText("汤品").assertExists()
        composeTestRule.onNodeWithText("甜点").assertExists()
    }

    @Test
    fun recipeListScreen_displays_empty_state() {
        composeTestRule.setContent {
            HomePantryTheme {
                RecipeListScreen(
                    onRecipeClick = {},
                    onAddRecipeClick = {},
                    onBackClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("还没有菜谱").assertIsDisplayed()
        composeTestRule.onNodeWithText("添加你的第一个菜谱开始吧").assertIsDisplayed()
    }
}
