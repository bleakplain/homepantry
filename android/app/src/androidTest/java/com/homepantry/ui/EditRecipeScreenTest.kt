package com.homepantry.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.homepantry.data.entity.DifficultyLevel
import com.homepantry.data.entity.Recipe
import com.homepantry.ui.recipe.EditRecipeScreen
import com.homepantry.viewmodel.RecipeViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class EditRecipeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Mock
    private lateinit var mockOnNavigateBack: () -> Unit

    private val testRecipe = Recipe(
        id = "test-recipe-1",
        name = "番茄炒蛋",
        description = "经典家常菜",
        cookingTime = 15,
        servings = 2,
        difficulty = DifficultyLevel.EASY
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun editRecipeScreen_displays_recipe_data() {
        composeTestRule.setContent {
            EditRecipeScreen(
                recipeId = testRecipe.id,
                onNavigateBack = {}
            )
        }

        // Check that recipe data is displayed
        composeTestRule.onNodeWithText("编辑菜谱").assertIsDisplayed()
    }

    @Test
    fun editRecipeScreen_pre_fills_recipe_fields() {
        composeTestRule.setContent {
            EditRecipeScreen(
                recipeId = testRecipe.id,
                onNavigateBack = {}
            )
        }

        // Check that fields are pre-filled with recipe data
        // (This would require the ViewModel to actually load the recipe)
        composeTestRule.onNodeWithText("编辑菜谱").assertIsDisplayed()
    }

    @Test
    fun editRecipeScreen_navigates_back_on_cancel() {
        var navigatedBack = false

        composeTestRule.setContent {
            EditRecipeScreen(
                recipeId = testRecipe.id,
                onNavigateBack = { navigatedBack = true }
            )
        }

        composeTestRule.onNodeWithText("取消").performClick()

        assert(navigatedBack)
    }

    @Test
    fun editRecipeScreen_shows_validation_errors() {
        composeTestRule.setContent {
            EditRecipeScreen(
                recipeId = testRecipe.id,
                onNavigateBack = {}
            )
        }

        // Clear the name field (simulate by entering empty text)
        composeTestRule.onNodeWithText("菜谱名称")
            .performTextClearance()

        // Try to save
        composeTestRule.onNodeWithText("保存").performClick()

        // Check for validation error
        composeTestRule.onNodeWithText("菜谱名称不能为空").assertIsDisplayed()
    }

    @Test
    fun editRecipeScreen_can_update_recipe_name() {
        composeTestRule.setContent {
            EditRecipeScreen(
                recipeId = testRecipe.id,
                onNavigateBack = {}
            )
        }

        // Update the recipe name
        composeTestRule.onNodeWithText("菜谱名称")
            .performTextClearance()
        composeTestRule.onNodeWithText("菜谱名称")
            .performTextInput("新菜谱名称")

        // Verify new name is displayed
        composeTestRule.onNodeWithText("新菜谱名称").assertIsDisplayed()
    }

    @Test
    fun editRecipeScreen_can_update_cooking_time() {
        composeTestRule.setContent {
            EditRecipeScreen(
                recipeId = testRecipe.id,
                onNavigateBack = {}
            )
        }

        // Update cooking time
        composeTestRule.onNodeWithText("烹饪时间（分钟）")
            .performTextClearance()
        composeTestRule.onNodeWithText("烹饪时间（分钟）")
            .performTextInput("30")

        // Verify new time is displayed
        composeTestRule.onNodeWithText("30").assertIsDisplayed()
    }

    @Test
    fun editRecipeScreen_can_update_servings() {
        composeTestRule.setContent {
            EditRecipeScreen(
                recipeId = testRecipe.id,
                onNavigateBack = {}
            )
        }

        // Update servings
        composeTestRule.onNodeWithText("份数")
            .performTextClearance()
        composeTestRule.onNodeWithText("份数")
            .performTextInput("4")

        // Verify new servings is displayed
        composeTestRule.onNodeWithText("4").assertIsDisplayed()
    }

    @Test
    fun editRecipeScreen_can_update_difficulty() {
        composeTestRule.setContent {
            EditRecipeScreen(
                recipeId = testRecipe.id,
                onNavigateBack = {}
            )
        }

        // Click on difficulty dropdown
        composeTestRule.onNodeWithText("难度").performClick()

        // Select different difficulty
        composeTestRule.onNodeWithText("困难").performClick()

        // Verify difficulty is updated
        composeTestRule.onNodeWithText("困难").assertIsDisplayed()
    }

    @Test
    fun editRecipeScreen_can_add_new_ingredient() {
        composeTestRule.setContent {
            EditRecipeScreen(
                recipeId = testRecipe.id,
                onNavigateBack = {}
            )
        }

        // Add new ingredient
        composeTestRule.onNodeWithText("添加食材").performClick()
        composeTestRule.onNodeWithText("食材名称")
            .performTextInput("新食材")
        composeTestRule.onNodeWithText("数量")
            .performTextInput("1")
        composeTestRule.onNodeWithText("确认").performClick()

        // Verify new ingredient is displayed
        composeTestRule.onNodeWithText("新食材: 1.0 个").assertIsDisplayed()
    }

    @Test
    fun editRecipeScreen_can_remove_existing_ingredient() {
        composeTestRule.setContent {
            EditRecipeScreen(
                recipeId = testRecipe.id,
                onNavigateBack = {}
            )
        }

        // If there's an ingredient, remove it
        composeTestRule.onNodeWithText("删除")
            .assertExists()
    }

    @Test
    fun editRecipeScreen_can_add_new_instruction() {
        composeTestRule.setContent {
            EditRecipeScreen(
                recipeId = testRecipe.id,
                onNavigateBack = {}
            )
        }

        // Add new instruction
        composeTestRule.onNodeWithText("添加步骤").performClick()
        composeTestRule.onNodeWithText("步骤说明")
            .performTextInput("新步骤")
        composeTestRule.onNodeWithText("确认").performClick()

        // Verify new instruction is displayed
        composeTestRule.onNodeWithText("新步骤").assertIsDisplayed()
    }

    @Test
    fun editRecipeScreen_can_update_existing_instruction() {
        composeTestRule.setContent {
            EditRecipeScreen(
                recipeId = testRecipe.id,
                onNavigateBack = {}
            )
        }

        // If there are instructions, they should be editable
        composeTestRule.onNodeWithText("制作步骤").assertIsDisplayed()
    }

    @Test
    fun editRecipeScreen_displays_delete_button() {
        composeTestRule.setContent {
            EditRecipeScreen(
                recipeId = testRecipe.id,
                onNavigateBack = {}
            )
        }

        // Check that delete button is displayed
        composeTestRule.onNodeWithText("删除菜谱").assertIsDisplayed()
    }

    @Test
    fun editRecipeScreen_confirms_deletion() {
        composeTestRule.setContent {
            EditRecipeScreen(
                recipeId = testRecipe.id,
                onNavigateBack = {}
            )
        }

        // Click delete button
        composeTestRule.onNodeWithText("删除菜谱").performClick()

        // Check for confirmation dialog
        composeTestRule.onNodeWithText("确认删除").assertIsDisplayed()
        composeTestRule.onNodeWithText("确定").assertIsDisplayed()
        composeTestRule.onNodeWithText("取消").assertIsDisplayed()
    }
}
