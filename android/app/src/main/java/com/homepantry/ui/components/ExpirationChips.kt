package com.homepantry.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.homepantry.data.entity.ExpirationCheckResult
import com.homepantry.data.entity.ExpirationStatus

/**
 * 过期芯片（批量操作）
 */
@Composable
fun ExpirationChipsRow(
    expiredItems: List<ExpirationCheckResult>,
    expiringSoonItems: List<ExpirationCheckResult>,
    onMarkAllAsHandled: () -> Unit,
    onClearAllProcessed: () -> Unit,
    onDeleteAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 已过期芯片
        if (expiredItems.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExpirationChip(
                    text = "已过期：${expiredItems.size}",
                    color = MaterialTheme.colorScheme.error,
                    count = expiredItems.size,
                    onClick = {}
                )

                Spacer(modifier = Modifier.weight(1f))

                // 批量标记为已处理
                IconButton(
                    onClick = onMarkAllAsHandled,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "全部标记为已处理",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 批量删除
                IconButton(
                    onClick = onDeleteAll,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "全部删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // 即将过期芯片
        if (expiringSoonItems.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExpirationChip(
                    text = "即将过期：${expiringSoonItems.size}",
                    color = MaterialTheme.colorScheme.tertiary,
                    count = expiringSoonItems.size,
                    onClick = {}
                )

                Spacer(modifier = Modifier.weight(1f))

                // 批量标记为已处理
                IconButton(
                    onClick = { /* TODO: 标记为已处理 */ },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "全部标记为已处理",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ExpirationChip(
    text: String,
    color: Color,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.2f)
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )

            // 显示数量
            if (count > 1) {
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = color
                ) {
                    Text(
                        text = if (count > 99) "99+" else "$count",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onError,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(horizontal = 2.dp)
                    )
                }
            }
        }
    }
}
