package com.homepantry.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homepantry.data.repository.ExpirationRepository
import com.homepantry.data.entity.ReminderConfig
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 过期设置 ViewModel
 */
class ExpirationSettingsViewModel(
    private val expirationRepo: ExpirationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpirationSettingsUiState())
    val uiState: StateFlow<ExpirationSettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    /**
     * 加载设置
     */
    private fun loadSettings() {
        viewModelScope.launch {
            // 加载全局设置（如果有）
            // 目前使用默认值
            _uiState.update { state ->
                state.copy(
                    currentConfig = ReminderConfig()
                )
            }
        }
    }

    /**
     * 更新提前天数
     */
    fun updateReminderDays(days: Int) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    currentConfig = state.currentConfig.copy(
                        reminderDays = days
                    )
                )
            }
        }
    }

    /**
     * 更新提醒时间
     */
    fun updateReminderTime(time: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    currentConfig = state.currentConfig.copy(
                        reminderTime = time
                    )
                )
            }
        }
    }

    /**
     * 更新提醒频率
     */
    fun updateReminderFrequency(frequency: com.homepantry.data.entity.ExpirationReminder.ReminderFrequency) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    currentConfig = state.currentConfig.copy(
                        reminderFrequency = frequency
                    )
                )
            }
        }
    }

    /**
     * 启用/禁用通知
     */
    fun enableNotifications(enable: Boolean) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    currentConfig = state.currentConfig.copy(
                        notificationEnabled = enable
                    )
                )
            }
        }
    }

    /**
     * 保存设置到默认配置
     */
    fun saveAsDefault() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            val config = _uiState.value.currentConfig

            // 这里应该保存到 SharedPreferences 或其他存储
            // 暂时只是模拟
            _uiState.update { state ->
                state.copy(
                    isSaving = false,
                    successMessage = "默认配置已保存"
                )
            }
        }
    }

    /**
     * 清除消息
     */
    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    /**
     * Expiration Settings UI State
     */
    data class ExpirationSettingsUiState(
        val currentConfig: ReminderConfig = ReminderConfig(),
        val isSaving: Boolean = false,
        val error: String? = null,
        val successMessage: String? = null
    )
}
