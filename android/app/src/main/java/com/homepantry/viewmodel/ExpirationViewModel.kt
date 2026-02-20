package com.homepantry.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homepantry.data.repository.ExpirationRepository
import com.homepantry.data.entity.ExpirationReminder
import com.homepantry.data.entity.ExpirationNotification
import com.homepantry.data.entity.ExpirationCheckResult
import com.homepantry.data.entity.ExpirationSummary
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 过期检查 ViewModel
 */
class ExpirationViewModel(
    private val expirationRepo: ExpirationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpirationUiState())
    val uiState: StateFlow<ExpirationUiState> = _uiState.asStateFlow()

    init {
        loadReminders()
        loadNotifications()
    }

    /**
     * 加载过期提醒
     */
    private fun loadReminders() {
        viewModelScope.launch {
            expirationRepo.getAllReminders()
                .catch { e ->
                    _uiState.update { state ->
                        state.copy(
                            error = "加载过期提醒失败：${e.message}"
                        )
                    }
                }
                .collect { reminders ->
                    _uiState.update { it.copy(expirationReminders = reminders) }
                }
        }
    }

    /**
     * 加载过期通知
     */
    private fun loadNotifications() {
        viewModelScope.launch {
            expirationRepo.getNotificationHistory()
                .catch { e ->
                    _uiState.update { state ->
                        state.copy(
                            error = "加载过期通知失败：${e.message}"
                        )
                    }
                }
                .collect { notifications ->
                    _uiState.update { it.copy(expirationNotifications = notifications) }
                }
        }
    }

    /**
     * 检查过期食材
     */
    fun checkExpiration(reminderDays: Int = 3) {
        viewModelScope.launch {
            _uiState.update { it.copy(isChecking = true, error = null) }

            val results = expirationRepo.checkExpiringItems(reminderDays)
            val summary = ExpirationSummary.fromResults(results)

            _uiState.update {
                it.copy(
                    expirationCheckResults = results,
                    expirationSummary = summary,
                    isChecking = false
                )
            }
        }
    }

    /**
     * 保存过期提醒配置
     */
    fun saveReminderConfig(
        pantryItemId: String,
        reminderDays: Int = 3,
        reminderTime: String = "08:00",
        reminderFrequency: ExpirationReminder.ReminderFrequency = ExpirationReminder.ReminderFrequency.DAILY,
        notificationEnabled: Boolean = true
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            val config = com.homepantry.data.entity.ReminderConfig(
                reminderDays = reminderDays,
                reminderTime = reminderTime,
                reminderFrequency = reminderFrequency,
                notificationEnabled = notificationEnabled
            )

            expirationRepo.createReminder(pantryItemId, config)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            isSaving = false,
                            successMessage = "过期提醒配置已保存"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { state ->
                        state.copy(
                            isSaving = false,
                            error = "保存失败：${e.message}"
                        )
                    }
                }
        }
    }

    /**
     * 更新过期提醒
     */
    fun updateReminder(reminder: ExpirationReminder) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            expirationRepo.updateReminder(reminder)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            isSaving = false,
                            successMessage = "过期提醒配置已更新"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { state ->
                        state.copy(
                            isSaving = false,
                            error = "更新失败：${e.message}"
                        )
                    }
                }
        }
    }

    /**
     * 删除过期提醒
     */
    fun deleteReminder(reminderId: String) {
        viewModelScope.launch {
            expirationRepo.deleteReminder(reminderId)
                .onSuccess {
                    _uiState.update { it.copy(successMessage = "过期提醒已删除") }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = "删除失败：${e.message}") }
                }
        }
    }

    /**
     * 标记通知为已读
     */
    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            expirationRepo.markNotificationAsRead(notificationId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            expirationNotifications = state.expirationNotifications.map { notif ->
                                if (notif.id == notificationId) {
                                    notif.copy(isRead = true)
                                } else {
                                    notif
                                }
                            }
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = "标记失败：${e.message}") }
                }
        }
    }

    /**
     * 标记通知为已处理
     */
    fun markNotificationAsHandled(notificationId: String) {
        viewModelScope.launch {
            expirationRepo.markNotificationAsHandled(notificationId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            expirationNotifications = state.expirationNotifications.map { notif ->
                                if (notif.id == notificationId) {
                                    notif.copy(isHandled = true)
                                } else {
                                    notif
                                }
                            }
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = "标记失败：${e.message}") }
                }
        }
    }

    /**
     * 清除已处理通知
     */
    fun clearProcessedNotifications() {
        viewModelScope.launch {
            expirationRepo.clearProcessedNotifications()
                .onSuccess { count ->
                    _uiState.update { it.copy(successMessage = "已清除 $count 个已处理通知") }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = "清除失败：${e.message}") }
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
     * Expiration UI State
     */
    data class ExpirationUiState(
        val expirationReminders: List<ExpirationReminder> = emptyList(),
        val expirationNotifications: List<ExpirationNotification> = emptyList(),
        val expirationCheckResults: List<ExpirationCheckResult> = emptyList(),
        val expirationSummary: ExpirationSummary? = null,
        val isChecking: Boolean = false,
        val isSaving: Boolean = false,
        val error: String? = null,
        val successMessage: String? = null
    )
}
