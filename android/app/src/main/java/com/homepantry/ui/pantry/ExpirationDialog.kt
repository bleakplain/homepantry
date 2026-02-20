package com.homepantry.ui.pantry

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.homepantry.data.entity.ExpirationCheckResult
import com.homepantry.data.entity.ExpirationStatus

/**
 * 过期检查对话框
 */
@Composable
fun ExpirationDialog(
    checkResults: List<ExpirationCheckResult>,
    summary: com.homepantry.data.entity.ExpirationSummary,
    onDismiss: () -> Unit,
    onMarkAsHandled: (String) -> Unit,
    onDelete: (String) -> Unit,
    onClearAllProcessed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(ExpirationTab.ALL) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "食材过期检查",
                    style = MaterialTheme.typography.headlineSmall
                )

                // 过期数量徽章
                if (summary.expiredCount > 0) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error
                    ) {
                        Text(
                            text = "${summary.expiredCount}",
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 统计信息
                ExpirationSummarySection(summary)

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // 标签页
                ExpirationTabsSection(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    summary = summary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 根据标签页显示内容
                when (selectedTab) {
                    ExpirationTab.ALL -> ExpirationAllItemsSection(
                        checkResults = checkResults,
                        onMarkAsHandled = onMarkAsHandled,
                        onDelete = onDelete
                    )
                    ExpirationTab.EXPIRED -> ExpirationExpiredSection(
                        checkResults = checkResults.filter { it.status == ExpirationStatus.EXPIRED || it.status == ExpirationStatus.EXPIRED_TODAY },
                        onMarkAsHandled = onMarkAsHandled,
                        onDelete = onDelete
                    )
                    ExpirationTab.EXPIRING_SOON -> ExpirationExpiringSoonSection(
                        checkResults = checkResults.filter { it.status == ExpirationStatus.EXPIRING_SOON },
                        onMarkAsHandled = onMarkAsHandled,
                        onDelete = onDelete
                    )
                    ExpirationTab.FRESH -> ExpirationFreshSection(
                        checkResults = checkResults.filter { it.status == ExpirationStatus.FRESH }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@Composable
fun ExpirationSummarySection(summary: com.homepantry.data.entity.ExpirationSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "检查结果",
            style = MaterialTheme.typography.titleSmall
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "总计: ${summary.totalCount}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "已过期: ${summary.expiredCount}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "即将过期: ${summary.expiringSoonCount}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.warning
            )
            Text(
                text = "新鲜: ${summary.freshCount}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

enum class ExpirationTab {
    ALL, EXPIRED, EXPIRING_SOON, FRESH
}
