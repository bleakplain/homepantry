package com.homepantry.ui.filter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 预设筛选部分
 */
@Composable
fun PresetsSection(
    onApplyQuickMeal: () -> Unit,
    onApplySimple: () -> Unit,
    onApplyHealthy: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "预设筛选",
            style = MaterialTheme.typography.titleSmall
        )

        // 快手菜预设
        PresetChip(
            icon = Icons.Outlined.Bookmark,
            name = "快手菜",
            onClick = onApplyQuickMeal
        )

        // 简单预设
        PresetChip(
            icon = Icons.Outlined.Bookmark,
            name = "简单",
            onClick = onApplySimple
        )

        // 健康饮食预设
        PresetChip(
            icon = Icons.Outlined.Bookmark,
            name = "健康饮食",
            onClick = onApplyHealthy
        )
    }
}

@Composable
fun PresetChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    name: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = androidx.compose.material.icons.filled.Star,
                contentDescription = "应用",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
