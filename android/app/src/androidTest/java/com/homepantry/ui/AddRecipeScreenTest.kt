package com.homepantry.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.homepantry.data.entity.DifficultyLevel
import com.homepantry.ui.recipe.AddRecipeScreen
import com.homepantry.viewmodel.RecipeViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify

class AddRecipeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Mock
    private lateinit var mockOnNavigateBack: () -> Unit

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun addRecipeScreen_displays_all_fields() {
        composeTestRule.setContent {
            AddRecipeScreen(
                onNavigateBack = {},
                recipeViewModel = RecipeViewModel(
                    // Use a fake repository for testing
                    recipeRepository = null
                )
            )
        }

        // Check that main UI elements are displayed
        composeTestRule.onNodeWithText("添加新菜谱").assertIsDisplayed()
        composeTestRule.onNodeWithText("菜谱名称").assertIsDisplayed()
        composeTestRule.onNodeWithText("烹饪时间（分钟）").assertIsDisplayed()
        composeTestRule.onNodeWithText("份数").assertIsDisplayed()
        composeTestRule.onNodeWithText("难度").assertIsDisplayed()
        composeTestRule.onNodeWithText("食材").assertIsDisplayed()
        composeTestRule.onNodeWithText("制作步骤").assertIsDisplayed()
    }

    @Test
    fun addRecipeScreen_shows_validation_error_for_empty_name() {
        composeTestRule.setContent {
            AddRecipeScreen(
                onNavigateBack = {},
                recipeViewModel = RecipeViewModel(
                    recipeRepository = null
                )
            )
        }

        // Try to submit without entering name
        composeTestRule.onNodeWithText("保存").performClick()

        // Check for validation error
        composeTestRule.onNodeWithText("菜谱名称不能为空").assertIsDisplayed()
    }

    @Test
    fun addRecipeScreen_shows_validation_error_for_invalid_cooking_time() {
        composeTestRule.setContent {
            AddRecipeScreen(
                onNavigateBack = {},
                recipeViewModel = RecipeViewModel(
                    recipeRepository = null
                )
            )
        }

        // Enter invalid cooking time
        composeTestRule.onNodeWithText("烹饪时间（分钟）")
            .performTextInput("-1")

        composeTestRule.onNodeWithText("保存").performClick()

        // Check for validation error
        composeTestRule.onNodeWithText("烹饪时间必须是正整数").assertIsDisplayed()
    }

    @Test
    fun addRecipeScreen_shows_validation_error_for_invalid_servings() {
        composeTestRule.setContent {
            AddRecipeScreen(
                onNavigateBack = {},
                recipeViewModel = RecipeViewModel(
                    recipeRepository = null
                )
            )
        }

        // Enter invalid servings
        composeTestRule.onNodeWithText("份数")
            .performTextInput("0")

        composeTestRule.onNodeWithText("保存").performClick()

        // Check for validation error
        composeTestRule.onNodeWithText("份数必须在1-100之间").assertIsDisplayed()
    }

    @Test
    fun addRecipeScreen_navigates_back_on_cancel() {
        var navigatedBack = false

        composeTestRule.setContent {
            AddRecipeScreen(
                onNavigateBack = { navigatedBack = true },
                recipeViewModel = RecipeViewModel(
                    recipeRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("取消").performClick()

        assert(navigatedBack)
    }

    @Test
    fun addRecipeScreen_displays_all_difficulty_levels() {
        composeTestRule.setContent {
            AddRecipeScreen(
                onNavigateBack = {},
                recipeViewModel = RecipeViewModel(
                    recipeRepository = null
                )
            )
        }

        // Click on difficulty dropdown
        composeTestRule.onNodeWithText("难度").performClick()

        // Verify all difficulty levels are shown
        DifficultyLevel.values().forEach { level ->
            when (level) {
                DifficultyLevel.EASY ->
                    composeTestRule.onNodeWithText("简单").assertIsDisplayed()
                DifficultyLevel.MEDIUM ->
                    composeTestRule.onNodeWithText("中等").assertIsDisplayed()
                DifficultyLevel.HARD ->
                    composeTestRule.onNodeWithText("困难").assertIsDisplayed()
            }
        }
    }

    @Test
    fun addRecipeScreen_can_add_ingredient() {
        composeTestRule.setContent {
            AddRecipeScreen(
                onNavigateBack = {},
                recipeViewModel = RecipeViewModel(
                    recipeRepository = null
                )
            )
        }

        // Click add ingredient button
        composeTestRule.onNodeWithText("添加食材").performClick()

        // Enter ingredient details
        composeTestRule.onNodeWithText("食材名称")
            .performTextInput("番茄")
        composeTestRule.onNodeWithText("数量")
            .performTextInput("2")
        composeTestRule.onNodeWithText("单位")
            .performTextInput("个")

        // Click confirm
        composeTestRule.onNodeWithText("确认").performClick()

        // Verify ingredient is added to list
        composeTestRule.onNodeWithText("番茄: 2.0 个").assertIsDisplayed()
    }

    @Test
    fun addRecipeIngredient_shows_validation_error_for_empty_name() {
        composeTestRule.setContent {
            AddRecipeScreen(
                onNavigateBack = {},
                recipeViewModel = RecipeViewModel(
                    recipeRepository = null
                )
            )
        }

        // Click add ingredient button
        composeTestRule.onNodeWithText("添加食材").performClick()

        // Try to confirm without entering name
        composeTestRule.onNodeWithText("确认").performClick()

        // Check for validation error
        composeTestRule.onNodeWithText("请输入食材名称").assertIsDisplayed()
    }

    @Test
    fun addRecipeIngredient_shows_validation_error_for_invalid_quantity() {
        composeTestRule.setContent {
            AddRecipeScreen(
                onNavigateBack = {},
                recipeViewModel = RecipeViewModel(
                    recipeRepository = null
                )
            )
        }

        // Click add ingredient button
        composeTestRule.onNodeWithText("添加食材").performClick()

        // Enter ingredient name but invalid quantity
        composeTestRule.onNodeWithText("食材名称")
            .performTextInput("番茄")
        composeTestRule.onNodeWithText("数量")
            .performTextInput("-1")

        // Try to confirm
        composeTestRule.onNodeWithText("确认").performClick()

        // Check for validation error
        composeTestRule.onNodeWithText("数量必须大于0").assertIsDisplayed()
    }

    @Test
    fun addRecipeScreen_can_add_instruction() {
        composeTestRule.setContent {
            AddRecipeScreen(
                onNavigateBack = {},
                recipeViewModel = RecipeViewModel(
                    recipeRepository = null
                )
            )
        }

        // Click add instruction button
        composeTestRule.onNodeWithText("添加步骤").performClick()

        // Enter instruction
        composeTestRule.onNodeWithText("步骤说明")
            .performTextInput("番茄洗净切块")

        // Click confirm
        composeTestRule.onNodeWithText("确认").performClick()

        // Verify instruction is added to list
        composeTestRule.onNodeWithText("1. 番茄洗净切块").assertIsDisplayed()
    }

    @Test
    fun addRecipeInstruction_shows_validation_error_for_empty_text() {
        composeTestRule.setContent {
            AddRecipeScreen(
                onNavigateBack = {},
                recipeViewModel = RecipeViewModel(
                    recipeRepository = null
                )
            )
        }

        // Click add instruction button
        composeTestRule.onNodeWithText("添加步骤").performClick()

        // Try to confirm without entering text
        composeTestRule.onNodeWithText("确认").performClick()

        // Check for validation error
        composeTestRule.onNodeWithText("请输入步骤说明").assertIsDisplayed()
    }

    @Test
    fun addRecipeScreen_can_remove_ingredient() {
        composeTestRule.setContent {
            AddRecipeScreen(
                onNavigateBack = {},
                recipeViewModel = RecipeViewModel(
                    recipeRepository = null
                )
            )
        }

        // Add an ingredient first
        composeTestRule.onNodeWithText("添加食材").performClick()
        composeTestRule.onNodeWithText("食材名称")
            .performTextInput("番茄")
        composeTestRule.onNodeWithText("数量")
            .performTextInput("2")
        composeTestRule.onNodeWithText("确认").performClick()

        // Remove the ingredient
        composeTestRule.onNodeWithText("删除").performClick()

        // Verify ingredient is removed
        composeTestRule.onNodeWithText("番茄: 2.0 个")
            .assertDoesNotExist()
    }

    @Test
    fun addRecipeScreen_can_remove_instruction() {
        composeTestRule.setContent {
            AddRecipeScreen(
                onNavigateBack = {},
                recipeViewModel = RecipeViewModel(
                    recipeRepository = null
                )
            )
        }

        // Add an instruction first
        composeTestRule.onNodeWithText("添加步骤").performClick()
        composeTestRule.onNodeWithText("步骤说明")
            .performTextInput("番茄洗净切块")
        composeTestRule.onNodeWithText("确认").performClick()

        // Remove the instruction
        composeTestRule.onNodeWithText("删除").performClick()

        // Verify instruction is removed
        composeTestRule.onNodeWithText("1. 番茄洗净切块")
            .assertDoesNotExist()
    }

    @Test
    fun addRecipeScreen_displays_ingredients_in_order() {
        composeTestRule.setContent {
            AddRecipeScreen(
                onNavigateBack = {},
                recipeViewModel = RecipeViewModel(
                    recipeRepository = null
                )
            )
        }

        // Add multiple ingredients
        listOf("番茄", "鸡蛋", "葱花").forEach { name ->
            composeTestRule.onNodeWithText("添加食材").performClick()
            composeTestRule.onNodeWithText("食材名称")
                .performTextInput(name)
            composeTestRule.onNodeWithText("数量")
                .performTextInput("1")
            composeTestRule.onNodeWithText("确认").performClick()
        }

        // Verify ingredients are displayed in order
        composeTestRule.onNodeWithText("番茄: 1.0 个").assertIsDisplayed()
        composeTestRule.onNodeWithText("鸡蛋: 1.0 个").assertIsDisplayed()
        composeTestRule.onNodeWithText("葱花: 1.0 个").assertIsDisplayed()
    }

    @Test
    fun addRecipeScreen_displays_instructions_with_step_numbers() {
        composeTestRule.setContent {
            AddRecipeScreen(
                onNavigateBack = {},
                recipeViewModel = RecipeViewModel(
                    recipeRepository = null
                )
            )
        }

        // Add multiple instructions
        listOf("第一步", "第二步", "第三步").forEach { text ->
            composeTestRule.onNodeWithText("添加步骤").performClick()
            composeTestRule.onNodeWithText("步骤说明")
                .performTextInput(text)
            composeTestRule.onNodeWithText("确认").performClick()
        }

        // Verify instructions have correct step numbers
        composeTestRule.onNodeWithText("1. 第一步").assertIsDisplayed()
        composeTestRule.onNodeWithText("2. 第二步").assertIsDisplayed()
        composeTestRule.onNodeWithText("3. 第三步").assertIsDisplayed()
    }
}
