package com.homepantry.ui.pantry

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.homepantry.data.entity.ExpirationNotification
import com.homepantry.data.entity.PantryItem
import com.homepantry.data.entity.StorageLocation

/**
 * 过期通知项
 */
@Composable
fun ExpirationNotificationItem(
    pantryItem: PantryItem,
    notification: ExpirationNotification,
    onMarkAsRead: () -> Unit,
    onMarkAsHandled: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        alpha = if (notification.isRead) 0.6f else 1.0f
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            val iconColor = when (notification.notificationType) {
                com.homepantry.data.entity.ExpirationNotification.NotificationType.EXPIRED -> MaterialTheme.colorScheme.error
                com.homepantry.data.entity.ExpirationNotification.NotificationType.EXPIRING_SOON -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.primary
            }

            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(40.dp)
            )

            // 食材信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pantryItem.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (notification.isHandled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${pantryItem.quantity} ${pantryItem.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 存放位置
                    Text(
                        text = when (pantryItem.storageLocation) {
                            StorageLocation.FRIDGE -> "冷藏"
                            StorageLocation.FREEZER -> "冷冻"
                            StorageLocation.PANTRY -> "常温"
                            StorageLocation.OTHER -> "其他"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 过期时间
                val daysSinceExpiration = (System.currentTimeMillis() - pantryItem.expiryDate!!) / (24 * 60 * 60 * 1000)
                Text(
                    text = "过期于 ${daysSinceExpiration.toInt()} 天前",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // 操作按钮
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 已读按钮
                if (!notification.isRead) {
                    IconButton(
                        onClick = onMarkAsRead,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = "标记为已读",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // 已处理按钮
                if (!notification.isHandled) {
                    IconButton(
                        onClick = onMarkAsHandled,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "标记为已处理",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 删除按钮
                IconButton(
                    onClick = { /* TODO: 删除通知 */ },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "删除通知",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
