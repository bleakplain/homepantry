package com.homepantry.ui.filter

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 筛选徽章
 */
@Composable
fun FilterBadge(
    activeFilterCount: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(32.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (activeFilterCount > 0) {
            Icon(
                imageVector = Icons.Filled.FilterList,
                contentDescription = "筛选",
                tint = MaterialTheme.colorScheme.primary
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.FilterList,
                contentDescription = "筛选",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 显示数量
        if (activeFilterCount > 0) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-12).dp, y = (-12).dp),
                .background(MaterialTheme.colorScheme.error),
                shape = androidx.compose.foundation.shape.CircleShape
            ) {
                Text(
                    text = if (activeFilterCount > 99) "99+" else "$activeFilterCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onError,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.offset(x = (-2).dp)
                )
            }
        }
    }
}
