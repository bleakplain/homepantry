package com.homepantry.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 过期徽章（显示过期食材数量）
 */
@Composable
fun ExpirationBadge(
    expiredCount: Int,
    expiringSoonCount: Int,
    onClick: () -> Unit
) {
    val total = expiredCount + expiringSoonCount

    Box(
        modifier = Modifier
            .size(32.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (total > 0) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = "过期提醒",
                tint = if (expiredCount > 0) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.warning
                },
                modifier = Modifier.size(24.dp)
            )

            // 显示数量
            if (total > 1) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-10).dp, y = (-10).dp),
                    .background(
                        if (expiredCount > 0) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.warning
                        }
                    ),
                    shape = androidx.compose.foundation.shape.CircleShape
                ) {
                    Text(
                        text = if (total > 99) "99+" else "$total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onError,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.offset(x = (-2).dp)
                    )
                }
            }
        } else {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "过期提醒",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
