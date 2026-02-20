package com.homepantry.ui.pantry

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homepantry.data.entity.ReminderConfig
import com.homepantry.data.entity.ExpirationReminder
import com.homepantry.data.entity.PantryItem
import com.homepantry.ui.theme.*
import com.homepantry.viewmodel.ExpirationSettingsViewModel

/**
 * 过期设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpirationSettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ExpirationSettingsViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("过期提醒设置") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.saveAsDefault() }) {
                        Text("保存为默认")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                ExpirationSettingsContent(
                    uiState = uiState,
                    viewModel = viewModel,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun ExpirationSettingsContent(
    uiState: ExpirationSettingsViewModel.ExpirationSettingsUiState,
    viewModel: ExpirationSettingsViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 提前天数设置
        ReminderDaysSection(
            currentDays = uiState.currentConfig.reminderDays,
            onDaysChange = { viewModel.updateReminderDays(it) }
        )

        // 提醒时间设置
        ReminderTimeSection(
            currentTime = uiState.currentConfig.reminderTime,
            onTimeChange = { viewModel.updateReminderTime(it) }
        )

        // 提醒频率设置
        ReminderFrequencySection(
            currentFrequency = uiState.currentConfig.reminderFrequency,
            onFrequencyChange = { viewModel.updateReminderFrequency(it) }
        )

        // 通知开关
        NotificationSwitchSection(
            isEnabled = uiState.currentConfig.notificationEnabled,
            onEnabledChange = { viewModel.enableNotifications(it) }
        )

        Spacer(modifier = Modifier.weight(1f))

        // 保存按钮
        FilledButton(
            onClick = { viewModel.saveAsDefault() },
            enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("保存设置")
            }
        }
    }
}

@Composable
fun ReminderDaysSection(
    currentDays: Int,
    onDaysChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "提前提醒天数",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(1, 3, 5, 7).forEach { days ->
                FilterChip(
                    text = "${days}天",
                    selected = currentDays == days,
                    onClick = { onDaysChange(days) }
                )
            }

            // 自定义输入
            OutlinedTextField(
                value = if (!listOf(1, 3, 5, 7).contains(currentDays)) currentDays.toString() else "",
                onValueChange = {
                    val value = it.toIntOrNull()
                    if (value != null && value in 1..30) {
                        onDaysChange(value)
                    }
                },
                label = { Text("自定义") },
                placeholder = { Text("1-30") },
                singleLine = true,
                modifier = Modifier.width(80.dp),
                isError = currentDays < 1 || currentDays > 30
            )
        }
    }
}

@Composable
fun ReminderTimeSection(
    currentTime: String,
    onTimeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "提醒时间",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = currentTime,
                onValueChange = onTimeChange,
                label = { Text("HH:mm") },
                placeholder = { Text("08:00") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ReminderFrequencySection(
    currentFrequency: com.homepantry.data.entity.ExpirationReminder.ReminderFrequency,
    onFrequencyChange: (com.homepantry.data.entity.ExpirationReminder.ReminderFrequency) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "提醒频率",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                text = "每天",
                selected = currentFrequency == com.homepantry.data.entity.ExpirationReminder.ReminderFrequency.DAILY,
                onClick = { onFrequencyChange(com.homepantry.data.entity.ExpirationReminder.ReminderFrequency.DAILY) }
            )

            FilterChip(
                text = "每周",
                selected = currentFrequency == com.homepantry.data.entity.ExpirationReminder.ReminderFrequency.WEEKLY,
                onClick = { onFrequencyChange(com.homepantry.data.entity.ExpirationReminder.ReminderFrequency.WEEKLY) }
            )

            FilterChip(
                text = "每月",
                selected = currentFrequency == com.homepantry.data.entity.ExpirationReminder.ReminderFrequency.MONTHLY,
                onClick = { onFrequencyChange(com.homepantry.data.entity.ExpirationReminder.ReminderFrequency.MONTHLY) }
            )
        }
    }
}

@Composable
fun NotificationSwitchSection(
    isEnabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "启用过期提醒",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "在食材过期前收到提醒通知",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = isEnabled,
            onCheckedChange = onEnabledChange
        )
    }
}
