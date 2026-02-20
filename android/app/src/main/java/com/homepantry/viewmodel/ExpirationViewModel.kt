package com.homepantry.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homepantry.data.entity.ExpirationCheckResult
import com.homepantry.data.entity.ReminderConfig
import com.homepantry.data.repository.ExpirationRepository
import com.homepantry.utils.Logger
import com.homepantry.utils.PerformanceMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 过期提醒视图模型
 */
class ExpirationViewModel(
    private val expirationRepository: ExpirationRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ExpirationViewModel"
    }

    private val _uiState = MutableStateFlow(ExpirationUiState())
    val uiState: StateFlow<ExpirationUiState> = _uiState.asStateFlow()

    /**
     * 检查过期食材
     */
    fun checkExpiringItems(reminderDays: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("checkExpiringItems") {
                Logger.enter("ExpirationViewModel.checkExpiringItems", reminderDays)

                expirationRepository.checkExpiringItems(reminderDays)
                    .collect { results ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                checkResults = results
                            )
                        }
                        Logger.d(TAG, "检查过期食材完成：${results.size} 个")
                    }

                Logger.exit("ExpirationViewModel.checkExpiringItems")
            }
        }
    }

    /**
     * 创建过期提醒
     */
    fun createReminder(pantryItemId: String, config: ReminderConfig) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("createReminder") {
                Logger.enter("ExpirationViewModel.createReminder", pantryItemId, config.reminderDays)

                expirationRepository.createReminder(pantryItemId, config)
                    .onSuccess { reminder ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                reminders = it.reminders + reminder,
                                successMessage = "过期提醒创建成功"
                            )
                        }
                        Logger.d(TAG, "过期提醒创建成功：${reminder.id}")
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "创建失败：${e.message}"
                            )
                        }
                        Logger.e(TAG, "过期提醒创建失败", e)
                    }

                Logger.exit("ExpirationViewModel.createReminder")
            }
        }
    }

    /**
     * 获取通知历史
     */
    fun getNotificationHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            PerformanceMonitor.recordMethodPerformance("getNotificationHistory") {
                Logger.enter("ExpirationViewModel.getNotificationHistory")

                expirationRepository.getNotificationHistory()
                    .collect { notifications ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                notifications = notifications
                            )
                        }
                        Logger.d(TAG, "获取通知历史：${notifications.size} 个")
                    }

                Logger.exit("ExpirationViewModel.getNotificationHistory")
            }
        }
    }

    /**
     * 标记通知为已读
     */
    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            PerformanceMonitor.recordMethodPerformance("markNotificationAsRead") {
                Logger.enter("ExpirationViewModel.markNotificationAsRead", notificationId)

                expirationRepository.markNotificationAsRead(notificationId)
                    .onSuccess {
                        Logger.d(TAG, "标记通知为已读：$notificationId")
                    }
                    .onFailure { e ->
                        Logger.e(TAG, "标记通知为已读失败：$notificationId", e)
                    }

                Logger.exit("ExpirationViewModel.markNotificationAsRead")
            }
        }
    }

    /**
     * 标记通知为已处理
     */
    fun markNotificationAsHandled(notificationId: String) {
        viewModelScope.launch {
            PerformanceMonitor.recordMethodPerformance("markNotificationAsHandled") {
                Logger.enter("ExpirationViewModel.markNotificationAsHandled", notificationId)

                expirationRepository.markNotificationAsHandled(notificationId)
                    .onSuccess {
                        Logger.d(TAG, "标记通知为已处理：$notificationId")
                    }
                    .onFailure { e ->
                        Logger.e(TAG, "标记通知为已处理失败：$notificationId", e)
                    }

                Logger.exit("ExpirationViewModel.markNotificationAsHandled")
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
        Logger.d(TAG, "ExpirationViewModel onCleared")
    }
}

/**
 * 过期提醒 UI 状态
 */
data class ExpirationUiState(
    val isLoading: Boolean = false,
    val checkResults: List<ExpirationCheckResult> = emptyList(),
    val reminders: List<com.homepantry.data.entity.ExpirationReminder> = emptyList(),
    val notifications: List<com.homepantry.data.entity.ExpirationNotification> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)
