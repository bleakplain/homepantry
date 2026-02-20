package com.homepantry.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homepantry.data.entity.ExpirationReminder
import com.homepantry.data.entity.ReminderConfig
import com.homepantry.data.repository.ExpirationRepository
import com.homepantry.data.validation.ExpirationValidator
import com.homepantry.data.validation.ValidationResult
import com.homepantry.data.constants.Constants
import com.homepantry.utils.Logger
import com.homepantry.utils.PerformanceMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 过期设置视图模型
 */
class ExpirationSettingsViewModel(
    private val expirationRepository: ExpirationRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ExpirationSettingsViewModel"
    }

    private val _uiState = MutableStateFlow(ExpirationSettingsUiState())
    val uiState: StateFlow<ExpirationSettingsUiState> = _uiState.asStateFlow()

    init {
        Logger.d(TAG, "ExpirationSettingsViewModel init")
        loadLatestReminder()
    }

    /**
     * 加载最新提醒
     */
    private fun loadLatestReminder() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("loadLatestReminder") {
                Logger.enter("ExpirationSettingsViewModel.loadLatestReminder")

                val reminder = expirationRepository.getLatestReminder()
                if (reminder != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            reminderDays = reminder.reminderDays,
                            reminderTime = reminder.reminderTime,
                            reminderFrequency = reminder.reminderFrequency,
                            notificationEnabled = reminder.isEnabled
                        )
                    }
                    Logger.d(TAG, "加载最新提醒：${reminder.id}")
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                    Logger.d(TAG, "没有最新提醒，使用默认值")
                }

                Logger.exit("ExpirationSettingsViewModel.loadLatestReminder")
            }
        }
    }

    /**
     * 更新提醒配置
     */
    fun updateReminderConfig(
        reminderDays: Int,
        reminderTime: String,
        reminderFrequency: ExpirationReminder.ReminderFrequency,
        notificationEnabled: Boolean
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            PerformanceMonitor.recordMethodPerformance("updateReminderConfig") {
                Logger.enter(
                    "ExpirationSettingsViewModel.updateReminderConfig",
                    reminderDays,
                    reminderTime,
                    reminderFrequency,
                    notificationEnabled
                )

                val config = ReminderConfig(
                    reminderDays = reminderDays,
                    reminderTime = reminderTime,
                    reminderFrequency = reminderFrequency,
                    notificationEnabled = notificationEnabled
                )

                // 验证提醒配置
                ExpirationValidator.validateConfig(config)
                    .onSuccess {
                        // TODO: 更新所有启用的提醒
                        // expirationRepository.updateAllReminders(config)

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                reminderDays = reminderDays,
                                reminderTime = reminderTime,
                                reminderFrequency = reminderFrequency,
                                notificationEnabled = notificationEnabled,
                                successMessage = "提醒配置更新成功"
                            )
                        }
                        Logger.d(TAG, "更新提醒配置成功")
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "配置验证失败：${e.message}"
                            )
                        }
                        Logger.e(TAG, "提醒配置验证失败", e)
                    }

                Logger.exit("ExpirationSettingsViewModel.updateReminderConfig")
            }
        }
    }

    /**
     * 重置为默认配置
     */
    fun resetToDefault() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            PerformanceMonitor.recordMethodPerformance("resetToDefault") {
                Logger.enter("ExpirationSettingsViewModel.resetToDefault")

                val config = ReminderConfig(
                    reminderDays = Constants.Days.DEFAULT_REMINDER_DAYS,
                    reminderTime = Constants.Times.DEFAULT_REMINDER_TIME,
                    reminderFrequency = ExpirationReminder.ReminderFrequency.DAILY,
                    notificationEnabled = true
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        reminderDays = config.reminderDays,
                        reminderTime = config.reminderTime,
                        reminderFrequency = config.reminderFrequency,
                        notificationEnabled = config.notificationEnabled,
                        successMessage = "已重置为默认配置"
                    )
                }
                Logger.d(TAG, "重置为默认配置成功")
                Logger.exit("ExpirationSettingsViewModel.resetToDefault")
            }
        }
    }

    /**
     * 清除错误消息
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * 清除成功消息
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "ExpirationSettingsViewModel onCleared")
    }
}

/**
 * 过期设置 UI 状态
 */
data class ExpirationSettingsUiState(
    val isLoading: Boolean = false,
    val reminderDays: Int = Constants.Days.DEFAULT_REMINDER_DAYS,
    val reminderTime: String = Constants.Times.DEFAULT_REMINDER_TIME,
    val reminderFrequency: ExpirationReminder.ReminderFrequency = ExpirationReminder.ReminderFrequency.DAILY,
    val notificationEnabled: Boolean = true,
    val error: String? = null,
    val successMessage: String? = null
)
