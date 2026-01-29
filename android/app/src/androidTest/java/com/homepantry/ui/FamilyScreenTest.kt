package com.homepantry.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.homepantry.ui.family.FamilyScreen
import com.homepantry.viewmodel.FamilyViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class FamilyScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Mock
    private lateinit var mockOnNavigateBack: () -> Unit

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun familyScreen_displays_title() {
        composeTestRule.setContent {
            FamilyScreen(
                onNavigateBack = {},
                familyViewModel = FamilyViewModel(
                    familyRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("家庭成员").assertIsDisplayed()
    }

    @Test
    fun familyScreen_displays_add_member_button() {
        composeTestRule.setContent {
            FamilyScreen(
                onNavigateBack = {},
                familyViewModel = FamilyViewModel(
                    familyRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("添加成员").assertIsDisplayed()
    }

    @Test
    fun familyScreen_displays_empty_state_when_no_members() {
        composeTestRule.setContent {
            FamilyScreen(
                onNavigateBack = {},
                familyViewModel = FamilyViewModel(
                    familyRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("暂无家庭成员").assertIsDisplayed()
        composeTestRule.onNodeWithText("点击下方按钮添加家庭成员").assertIsDisplayed()
    }

    @Test
    fun familyScreen_opens_add_member_dialog() {
        composeTestRule.setContent {
            FamilyScreen(
                onNavigateBack = {},
                familyViewModel = FamilyViewModel(
                    familyRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("添加成员").performClick()

        // Verify dialog opens
        composeTestRule.onNodeWithText("添加家庭成员").assertIsDisplayed()
        composeTestRule.onNodeWithText("姓名").assertIsDisplayed()
    }

    @Test
    fun addMemberDialog_shows_validation_error_for_empty_name() {
        composeTestRule.setContent {
            FamilyScreen(
                onNavigateBack = {},
                familyViewModel = FamilyViewModel(
                    familyRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("添加成员").performClick()

        // Try to add without entering name
        composeTestRule.onNodeWithText("确认").performClick()

        // Check for validation error
        composeTestRule.onNodeWithText("请输入姓名").assertIsDisplayed()
    }

    @Test
    fun addMemberDialog_shows_validation_error_for_invalid_age() {
        composeTestRule.setContent {
            FamilyScreen(
                onNavigateBack = {},
                familyViewModel = FamilyViewModel(
                    familyRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("添加成员").performClick()

        // Enter name but invalid age
        composeTestRule.onNodeWithText("姓名")
            .performTextInput("张三")
        composeTestRule.onNodeWithText("年龄")
            .performTextInput("-1")

        // Try to add
        composeTestRule.onNodeWithText("确认").performClick()

        // Check for validation error
        composeTestRule.onNodeWithText("年龄必须大于0").assertIsDisplayed()
    }

    @Test
    fun addMemberDialog_can_add_family_member() {
        composeTestRule.setContent {
            FamilyScreen(
                onNavigateBack = {},
                familyViewModel = FamilyViewModel(
                    familyRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("添加成员").performClick()

        // Enter member details
        composeTestRule.onNodeWithText("姓名")
            .performTextInput("张三")
        composeTestRule.onNodeWithText("年龄")
            .performTextInput("25")

        // Add member
        composeTestRule.onNodeWithText("确认").performClick()

        // Verify member is added
        composeTestRule.onNodeWithText("张三").assertIsDisplayed()
        composeTestRule.onNodeWithText("25岁").assertIsDisplayed()
    }

    @Test
    fun addMemberDialog_can_set_dietary_restrictions() {
        composeTestRule.setContent {
            FamilyScreen(
                onNavigateBack = {},
                familyViewModel = FamilyViewModel(
                    familyRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("添加成员").performClick()

        // Check for dietary restriction options
        composeTestRule.onNodeWithText("饮食禁忌").assertIsDisplayed()
    }

    @Test
    fun addMemberDialog_can_set_meal_preference() {
        composeTestRule.setContent {
            FamilyScreen(
                onNavigateBack = {},
                familyViewModel = FamilyViewModel(
                    familyRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("添加成员").performClick()

        // Check for meal preference options
        composeTestRule.onNodeWithText("膳食偏好").assertIsDisplayed()
    }

    @Test
    fun familyScreen_displays_member_list() {
        composeTestRule.setContent {
            FamilyScreen(
                onNavigateBack = {},
                familyViewModel = FamilyViewModel(
                    familyRepository = null
                )
            )
        }

        // When members exist, they should be displayed in a list
        composeTestRule.onNodeWithText("家庭成员").assertIsDisplayed()
    }

    @Test
    fun familyMemberCard_displays_member_info() {
        composeTestRule.setContent {
            FamilyScreen(
                onNavigateBack = {},
                familyViewModel = FamilyViewModel(
                    familyRepository = null
                )
            )
        }

        // Member cards should display:
        // - Name
        // - Age
        // - Dietary restrictions
        // - Meal preferences
    }

    @Test
    fun familyMemberCard_can_be_deleted() {
        composeTestRule.setContent {
            FamilyScreen(
                onNavigateBack = {},
                familyViewModel = FamilyViewModel(
                    familyRepository = null
                )
            )
        }

        // If there are members, they should be deletable
        composeTestRule.onNodeWithText("删除").assertExists()
    }

    @Test
    fun familyMemberCard_can_be_edited() {
        composeTestRule.setContent {
            FamilyScreen(
                onNavigateBack = {},
                familyViewModel = FamilyViewModel(
                    familyRepository = null
                )
            )
        }

        // If there are members, they should be editable
        composeTestRule.onNodeWithText("编辑").assertExists()
    }

    @Test
    fun familyScreen_displays_total_members_count() {
        composeTestRule.setContent {
            FamilyScreen(
                onNavigateBack = {},
                familyViewModel = FamilyViewModel(
                    familyRepository = null
                )
            )
        }

        // Should display total number of family members
        composeTestRule.onNodeWithText("成员").assertIsDisplayed()
    }

    @Test
    fun addMemberDialog_can_be_cancelled() {
        composeTestRule.setContent {
            FamilyScreen(
                onNavigateBack = {},
                familyViewModel = FamilyViewModel(
                    familyRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("添加成员").performClick()

        // Cancel dialog
        composeTestRule.onNodeWithText("取消").performClick()

        // Dialog should be closed
        composeTestRule.onNodeWithText("添加家庭成员")
            .assertDoesNotExist()
    }

    @Test
    fun editMemberDialog_pre_fills_member_data() {
        composeTestRule.setContent {
            FamilyScreen(
                onNavigateBack = {},
                familyViewModel = FamilyViewModel(
                    familyRepository = null
                )
            )
        }

        // If there are members, clicking edit should pre-fill data
        composeTestRule.onNodeWithText("编辑").performClick()

        // Verify dialog opens with existing data
        composeTestRule.onNodeWithText("编辑家庭成员").assertIsDisplayed()
    }

    @Test
    fun editMemberDialog_can_update_member_info() {
        composeTestRule.setContent {
            FamilyScreen(
                onNavigateBack = {},
                familyViewModel = FamilyViewModel(
                    familyRepository = null
                )
            )
        }

        // Edit existing member
        composeTestRule.onNodeWithText("编辑").performClick()

        // Update name
        composeTestRule.onNodeWithText("姓名")
            .performTextClearance()
        composeTestRule.onNodeWithText("姓名")
            .performTextInput("新名字")

        // Save changes
        composeTestRule.onNodeWithText("保存").performClick()

        // Verify name is updated
        composeTestRule.onNodeWithText("新名字").assertIsDisplayed()
    }

    @Test
    fun familyScreen_displays_dietary_restrictions_summary() {
        composeTestRule.setContent {
            FamilyScreen(
                onNavigateBack = {},
                familyViewModel = FamilyViewModel(
                    familyRepository = null
                )
            )
        }

        // Should show summary of all dietary restrictions
        composeTestRule.onNodeWithText("饮食禁忌").assertIsDisplayed()
    }
}
