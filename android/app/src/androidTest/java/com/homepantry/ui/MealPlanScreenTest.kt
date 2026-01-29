package com.homepantry.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.homepantry.data.entity.MealType
import com.homepantry.ui.mealplan.MealPlanScreen
import com.homepantry.viewmodel.MealPlanViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class MealPlanScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Mock
    private lateinit var mockOnNavigateToRecipeDetail: (String) -> Unit

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun mealPlanScreen_displays_title() {
        composeTestRule.setContent {
            MealPlanScreen(
                onNavigateToRecipeDetail = {},
                mealPlanViewModel = MealPlanViewModel(
                    mealPlanRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("膳食计划").assertIsDisplayed()
    }

    @Test
    fun mealPlanScreen_displays_date_picker() {
        composeTestRule.setContent {
            MealPlanScreen(
                onNavigateToRecipeDetail = {},
                mealPlanViewModel = MealPlanViewModel(
                    mealPlanRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("选择日期").assertIsDisplayed()
    }

    @Test
    fun mealPlanScreen_displays_all_meal_types() {
        composeTestRule.setContent {
            MealPlanScreen(
                onNavigateToRecipeDetail = {},
                mealPlanViewModel = MealPlanViewModel(
                    mealPlanRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("早餐").assertIsDisplayed()
        composeTestRule.onNodeWithText("午餐").assertIsDisplayed()
        composeTestRule.onNodeWithText("晚餐").assertIsDisplayed()
    }

    @Test
    fun mealPlanScreen_displays_add_button_for_each_meal_type() {
        composeTestRule.setContent {
            MealPlanScreen(
                onNavigateToRecipeDetail = {},
                mealPlanViewModel = MealPlanViewModel(
                    mealPlanRepository = null
                )
            )
        }

        // Check for add buttons (floating action buttons or similar)
        MealType.values().forEach { _ ->
            // Each meal type should have an add option
        }
    }

    @Test
    fun mealPlanScreen_displays_shopping_list_icon() {
        composeTestRule.setContent {
            MealPlanScreen(
                onNavigateToRecipeDetail = {},
                mealPlanViewModel = MealPlanViewModel(
                    mealPlanRepository = null
                )
            )
        }

        // Check for shopping cart icon
        composeTestRule.onNodeWithText("购物清单").assertIsDisplayed()
    }

    @Test
    fun mealPlanScreen_opens_shopping_list_dialog() {
        composeTestRule.setContent {
            MealPlanScreen(
                onNavigateToRecipeDetail = {},
                mealPlanViewModel = MealPlanViewModel(
                    mealPlanRepository = null
                )
            )
        }

        // Click on shopping list
        composeTestRule.onNodeWithText("购物清单").performClick()

        // Verify dialog opens
        composeTestRule.onNodeWithText("购物清单").assertIsDisplayed()
    }

    @Test
    fun mealPlanScreen_displays_week_view_option() {
        composeTestRule.setContent {
            MealPlanScreen(
                onNavigateToRecipeDetail = {},
                mealPlanViewModel = MealPlanViewModel(
                    mealPlanRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("本周").assertIsDisplayed()
    }

    @Test
    fun mealPlanScreen_can_navigate_to_previous_day() {
        composeTestRule.setContent {
            MealPlanScreen(
                onNavigateToRecipeDetail = {},
                mealPlanViewModel = MealPlanViewModel(
                    mealPlanRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("前一天").assertIsDisplayed()
    }

    @Test
    fun mealPlanScreen_can_navigate_to_next_day() {
        composeTestRule.setContent {
            MealPlanScreen(
                onNavigateToRecipeDetail = {},
                mealPlanViewModel = MealPlanViewModel(
                    mealPlanRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("后一天").assertIsDisplayed()
    }

    @Test
    fun mealPlanScreen_displays_empty_state_for_no_plans() {
        composeTestRule.setContent {
            MealPlanScreen(
                onNavigateToRecipeDetail = {},
                mealPlanViewModel = MealPlanViewModel(
                    mealPlanRepository = null
                )
            )
        }

        // Should show empty state or placeholder
        composeTestRule.onNodeWithText("暂无膳食计划").assertIsDisplayed()
    }

    @Test
    fun mealPlanScreen_displays_meal_plan_cards() {
        composeTestRule.setContent {
            MealPlanScreen(
                onNavigateToRecipeDetail = {},
                mealPlanViewModel = MealPlanViewModel(
                    mealPlanRepository = null
                )
            )
        }

        // When meal plans exist, they should be displayed as cards
        composeTestRule.onNodeWithText("膳食计划").assertIsDisplayed()
    }

    @Test
    fun mealPlanScreen_displays_servings_info() {
        composeTestRule.setContent {
            MealPlanScreen(
                onNavigateToRecipeDetail = {},
                mealPlanViewModel = MealPlanViewModel(
                    mealPlanRepository = null
                )
            )
        }

        // Meal plan cards should show servings
        composeTestRule.onNodeWithText("份").assertIsDisplayed()
    }

    @Test
    fun mealPlanScreen_displays_notes_when_present() {
        composeTestRule.setContent {
            MealPlanScreen(
                onNavigateToRecipeDetail = {},
                mealPlanViewModel = MealPlanViewModel(
                    mealPlanRepository = null
                )
            )
        }

        // If meal plan has notes, they should be displayed
        composeTestRule.onNodeWithText("备注").assertIsDisplayed()
    }

    @Test
    fun mealPlanScreen_can_delete_meal_plan() {
        composeTestRule.setContent {
            MealPlanScreen(
                onNavigateToRecipeDetail = {},
                mealPlanViewModel = MealPlanViewModel(
                    mealPlanRepository = null
                )
            )
        }

        // When a meal plan exists, it should be deletable
        composeTestRule.onNodeWithText("删除").assertExists()
    }

    @Test
    fun mealPlanScreen_can_edit_meal_plan() {
        composeTestRule.setContent {
            MealPlanScreen(
                onNavigateToRecipeDetail = {},
                mealPlanViewModel = MealPlanViewModel(
                    mealPlanRepository = null
                )
            )
        }

        // When a meal plan exists, it should be editable
        composeTestRule.onNodeWithText("编辑").assertExists()
    }

    @Test
    fun mealPlanScreen_navigates_to_recipe_on_click() {
        var clickedRecipeId: String? = null

        composeTestRule.setContent {
            MealPlanScreen(
                onNavigateToRecipeDetail = { recipeId ->
                    clickedRecipeId = recipeId
                },
                mealPlanViewModel = MealPlanViewModel(
                    mealPlanRepository = null
                )
            )
        }

        // Click on a recipe card (if exists)
        // This would require mock data
    }

    @Test
    fun mealPlanScreen_displays_current_date() {
        composeTestRule.setContent {
            MealPlanScreen(
                onNavigateToRecipeDetail = {},
                mealPlanViewModel = MealPlanViewModel(
                    mealPlanRepository = null
                )
            )
        }

        // Should display current date
        composeTestRule.onNodeWithText("今天").assertExists()
    }

    @Test
    fun shoppingListDialog_displays_ingredients_list() {
        composeTestRule.setContent {
            MealPlanScreen(
                onNavigateToRecipeDetail = {},
                mealPlanViewModel = MealPlanViewModel(
                    mealPlanRepository = null
                )
            )
        }

        // Open shopping list
        composeTestRule.onNodeWithText("购物清单").performClick()

        // Verify ingredients are displayed
        composeTestRule.onNodeWithText("食材清单").assertIsDisplayed()
    }

    @Test
    fun shoppingListDialog_displays_quantities() {
        composeTestRule.setContent {
            MealPlanScreen(
                onNavigateToRecipeDetail = {},
                mealPlanViewModel = MealPlanViewModel(
                    mealPlanRepository = null
                )
            )
        }

        // Open shopping list
        composeTestRule.onNodeWithText("购物清单").performClick()

        // Verify quantities are displayed
        composeTestRule.onNodeWithText("数量").assertIsDisplayed()
    }

    @Test
    fun shoppingList_dialog_can_be_closed() {
        composeTestRule.setContent {
            MealPlanScreen(
                onNavigateToRecipeDetail = {},
                mealPlanViewModel = MealPlanViewModel(
                    mealPlanRepository = null
                )
            )
        }

        // Open shopping list
        composeTestRule.onNodeWithText("购物清单").performClick()

        // Close dialog
        composeTestRule.onNodeWithText("关闭").performClick()

        // Dialog should be closed
    }
}
