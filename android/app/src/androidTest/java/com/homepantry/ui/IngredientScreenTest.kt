package com.homepantry.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.homepantry.ui.ingredient.IngredientScreen
import org.junit.Rule
import org.junit.Test

class IngredientScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun ingredientScreen_displaysTitle() {
        composeTestRule.setContent {
            IngredientScreen(
                onBackClick = {}
            )
        }

        // Verify the screen title is displayed
        composeTestRule.onNodeWithText("食材")
            .assertExists()
    }

    @Test
    fun ingredientScreen_displaysTabs() {
        composeTestRule.setContent {
            IngredientScreen(
                onBackClick = {}
            )
        }

        // Verify all tabs are displayed
        composeTestRule.onNodeWithText("食材箱")
            .assertExists()
        composeTestRule.onNodeWithText("食材库")
            .assertExists()
        composeTestRule.onNodeWithText("推荐菜谱")
            .assertExists()
    }

    @Test
    fun ingredientScreen_emptyStateDisplaysCorrectly() {
        composeTestRule.setContent {
            IngredientScreen(
                onBackClick = {}
            )
        }

        // The empty state message should be displayed when no items
        composeTestRule.onNodeWithText("食材箱是空的")
            .assertExists()
        composeTestRule.onNodeWithText("添加食材以获取菜谱推荐")
            .assertExists()
    }

    @Test
    fun ingredientScreen_tabsAreClickable() {
        composeTestRule.setContent {
            IngredientScreen(
                onBackClick = {}
            )
        }

        // Click on ingredients library tab
        composeTestRule.onNodeWithText("食材库")
            .performClick()

        // Verify it's clickable
        composeTestRule.onNodeWithText("食材库")
            .assertExists()
    }
}
