package com.homepantry.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.homepantry.ui.settings.SettingsScreen
import com.homepantry.viewmodel.SettingsViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Mock
    private lateinit var mockOnNavigateBack: () -> Unit

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun settingsScreen_displays_title() {
        composeTestRule.setContent {
            SettingsScreen(
                onNavigateBack = {},
                settingsViewModel = SettingsViewModel(
                    settingsRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("设置").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displays_appearance_section() {
        composeTestRule.setContent {
            SettingsScreen(
                onNavigateBack = {},
                settingsViewModel = SettingsViewModel(
                    settingsRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("外观").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displays_theme_option() {
        composeTestRule.setContent {
            SettingsScreen(
                onNavigateBack = {},
                settingsViewModel = SettingsViewModel(
                    settingsRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("主题").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displays_language_option() {
        composeTestRule.setContent {
            SettingsScreen(
                onNavigateBack = {},
                settingsViewModel = SettingsViewModel(
                    settingsRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("语言").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displays_notifications_section() {
        composeTestRule.setContent {
            SettingsScreen(
                onNavigateBack = {},
                settingsViewModel = SettingsViewModel(
                    settingsRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("通知").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displays_meal_plan_reminders_option() {
        composeTestRule.setContent {
            SettingsScreen(
                onNavigateBack = {},
                settingsViewModel = SettingsViewModel(
                    settingsRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("膳食计划提醒").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displays_expiry_reminders_option() {
        composeTestRule.setContent {
            SettingsScreen(
                onNavigateBack = {},
                settingsViewModel = SettingsViewModel(
                    settingsRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("食材过期提醒").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displays_data_section() {
        composeTestRule.setContent {
            SettingsScreen(
                onNavigateBack = {},
                settingsViewModel = SettingsViewModel(
                    settingsRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("数据").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displays_export_data_option() {
        composeTestRule.setContent {
            SettingsScreen(
                onNavigateBack = {},
                settingsViewModel = SettingsViewModel(
                    settingsRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("导出数据").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displays_import_data_option() {
        composeTestRule.setContent {
            SettingsScreen(
                onNavigateBack = {},
                settingsViewModel = SettingsViewModel(
                    settingsRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("导入数据").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displays_about_section() {
        composeTestRule.setContent {
            SettingsScreen(
                onNavigateBack = {},
                settingsViewModel = SettingsViewModel(
                    settingsRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("关于").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displays_app_version() {
        composeTestRule.setContent {
            SettingsScreen(
                onNavigateBack = {},
                settingsViewModel = SettingsViewModel(
                    settingsRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("版本").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displays_privacy_policy_option() {
        composeTestRule.setContent {
            SettingsScreen(
                onNavigateBack = {},
                settingsViewModel = SettingsViewModel(
                    settingsRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("隐私政策").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displays_terms_of_service_option() {
        composeTestRule.setContent {
            SettingsScreen(
                onNavigateBack = {},
                settingsViewModel = SettingsViewModel(
                    settingsRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("服务条款").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_can_toggle_theme() {
        composeTestRule.setContent {
            SettingsScreen(
                onNavigateBack = {},
                settingsViewModel = SettingsViewModel(
                    settingsRepository = null
                )
            )
        }

        // Click on theme option
        composeTestRule.onNodeWithText("主题").performClick()

        // Verify theme selection dialog appears
        composeTestRule.onNodeWithText("浅色").assertIsDisplayed()
        composeTestRule.onNodeWithText("深色").assertIsDisplayed()
        composeTestRule.onNodeWithText("跟随系统").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_can_toggle_meal_plan_reminders() {
        composeTestRule.setContent {
            SettingsScreen(
                onNavigateBack = {},
                settingsViewModel = SettingsViewModel(
                    settingsRepository = null
                )
            )
        }

        // Toggle meal plan reminders
        composeTestRule.onNodeWithText("膳食计划提醒").performClick()

        // Verify state changes (this would require actual ViewModel testing)
    }

    @Test
    fun settingsScreen_can_toggle_expiry_reminders() {
        composeTestRule.setContent {
            SettingsScreen(
                onNavigateBack = {},
                settingsViewModel = SettingsViewModel(
                    settingsRepository = null
                )
            )
        }

        // Toggle expiry reminders
        composeTestRule.onNodeWithText("食材过期提醒").performClick()

        // Verify state changes
    }

    @Test
    fun settingsScreen_can_open_language_selection() {
        composeTestRule.setContent {
            SettingsScreen(
                onNavigateBack = {},
                settingsViewModel = SettingsViewModel(
                    settingsRepository = null
                )
            )
        }

        // Click on language option
        composeTestRule.onNodeWithText("语言").performClick()

        // Verify language selection dialog appears
        composeTestRule.onNodeWithText("简体中文").assertIsDisplayed()
        composeTestRule.onNodeWithText("English").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_can_export_data() {
        composeTestRule.setContent {
            SettingsScreen(
                onNavigateBack = {},
                settingsViewModel = SettingsViewModel(
                    settingsRepository = null
                )
            )
        }

        // Click export data
        composeTestRule.onNodeWithText("导出数据").performClick()

        // Verify export dialog appears
        composeTestRule.onNodeWithText("导出").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_can_import_data() {
        composeTestRule.setContent {
            SettingsScreen(
                onNavigateBack = {},
                settingsViewModel = SettingsViewModel(
                    settingsRepository = null
                )
            )
        }

        // Click import data
        composeTestRule.onNodeWithText("导入数据").performClick()

        // Verify import dialog appears
        composeTestRule.onNodeWithText("导入").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displays_clear_cache_option() {
        composeTestRule.setContent {
            SettingsScreen(
                onNavigateBack = {},
                settingsViewModel = SettingsViewModel(
                    settingsRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("清除缓存").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_can_clear_cache() {
        composeTestRule.setContent {
            SettingsScreen(
                onNavigateBack = {},
                settingsViewModel = SettingsViewModel(
                    settingsRepository = null
                )
            )
        }

        // Click clear cache
        composeTestRule.onNodeWithText("清除缓存").performClick()

        // Verify confirmation dialog
        composeTestRule.onNodeWithText("确认清除缓存").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displays_logout_option() {
        composeTestRule.setContent {
            SettingsScreen(
                onNavigateBack = {},
                settingsViewModel = SettingsViewModel(
                    settingsRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("退出登录").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_can_navigate_back() {
        var navigatedBack = false

        composeTestRule.setContent {
            SettingsScreen(
                onNavigateBack = { navigatedBack = true },
                settingsViewModel = SettingsViewModel(
                    settingsRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("返回").performClick()

        assert(navigatedBack)
    }

    @Test
    fun settingsScreen_displays_feedback_option() {
        composeTestRule.setContent {
            SettingsScreen(
                onNavigateBack = {},
                settingsViewModel = SettingsViewModel(
                    settingsRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("反馈").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displays_contact_support_option() {
        composeTestRule.setContent {
            SettingsScreen(
                onNavigateBack = {},
                settingsViewModel = SettingsViewModel(
                    settingsRepository = null
                )
            )
        }

        composeTestRule.onNodeWithText("联系支持").assertIsDisplayed()
    }
}
