package com.homepantry.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.homepantry.ui.home.HomeScreen
import com.homepantry.ui.theme.HomePantryTheme
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displays_app_name() {
        composeTestRule.setContent {
            HomePantryTheme {
                HomeScreen()
            }
        }

        composeTestRule.onNodeWithText("家常味库").assertIsDisplayed()
    }

    @Test
    fun homeScreen_displays_search_bar() {
        composeTestRule.setContent {
            HomePantryTheme {
                HomeScreen()
            }
        }

        composeTestRule.onNodeWithText("搜索菜谱...").assertIsDisplayed()
    }

    @Test
    fun homeScreen_displays_navigation_cards() {
        composeTestRule.setContent {
            HomePantryTheme {
                HomeScreen()
            }
        }

        composeTestRule.onNodeWithText("菜谱").assertIsDisplayed()
        composeTestRule.onNodeWithText("食材").assertIsDisplayed()
        composeTestRule.onNodeWithText("计划").assertIsDisplayed()
        composeTestRule.onNodeWithText("家庭").assertIsDisplayed()
    }

    @Test
    fun homeScreen_displays_today_recommendation_section() {
        composeTestRule.setContent {
            HomePantryTheme {
                HomeScreen()
            }
        }

        composeTestRule.onNodeWithText("今日推荐").assertIsDisplayed()
    }

    @Test
    fun homeScreen_displays_recently_added_section() {
        composeTestRule.setContent {
            HomePantryTheme {
                HomeScreen()
            }
        }

        composeTestRule.onNodeWithText("最近添加").assertIsDisplayed()
    }
}
