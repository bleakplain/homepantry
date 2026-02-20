package com.homepantry.ui.filter

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.homepantry.data.entity.DifficultyLevel

/**
 * 难度筛选部分
 */
@Composable
fun DifficultyFilterSection(
    selectedDifficulties: Set<DifficultyLevel>,
    onToggle: (DifficultyLevel) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "难度",
            style = MaterialTheme.typography.titleSmall
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DifficultyChip(
                level = DifficultyLevel.EASY,
                label = "简单",
                isSelected = selectedDifficulties.contains(DifficultyLevel.EASY),
                onClick = { onToggle(DifficultyLevel.EASY) }
            )

            DifficultyChip(
                level = DifficultyLevel.MEDIUM,
                label = "中等",
                isSelected = selectedDifficulties.contains(DifficultyLevel.MEDIUM),
                onClick = { onToggle(DifficultyLevel.MEDIUM) }
            )

            DifficultyChip(
                level = DifficultyLevel.HARD,
                label = "困难",
                isSelected = selectedDifficulties.contains(DifficultyLevel.HARD),
                onClick = { onToggle(DifficultyLevel.HARD) }
            )
        }
    }
}

@Composable
fun DifficultyChip(
    level: DifficultyLevel,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        icon = getDifficultyIcon(level),
        label = label,
        isSelected = isSelected,
        onClick = onClick
    )
}

fun getDifficultyIcon(level: DifficultyLevel): String {
    return when (level) {
        DifficultyLevel.EASY -> "🟢"
        DifficultyLevel.MEDIUM -> "🟡"
        DifficultyLevel.HARD -> "🔴"
    }
}
