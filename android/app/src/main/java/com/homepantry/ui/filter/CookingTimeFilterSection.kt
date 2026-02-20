package com.homepantry.ui.filter

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 烹饪时间筛选部分
 */
@Composable
fun CookingTimeFilterSection(
    min: Int?,
    max: Int?,
    onMinChange: (Int?) -> Unit,
    onMaxChange: (Int?) -> Unit
) {
    var selectedMin by remember { mutableStateOf(min) }
    var selectedMax by remember { mutableStateOf(max) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "烹饪时间",
            style = MaterialTheme.typography.titleSmall
        )

        // 预设选项
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                text = "15 分钟以内",
                isSelected = selectedMin == null && selectedMax == 14,
                onClick = {
                    selectedMin = null
                    selectedMax = 14
                    onMinChange(null)
                    onMaxChange(14)
                }
            )

            FilterChip(
                text = "15-30 分钟",
                isSelected = selectedMin == 15 && selectedMax == 30,
                onClick = {
                    selectedMin = 15
                    selectedMax = 30
                    onMinChange(15)
                    onMaxChange(30)
                }
            )

            FilterChip(
                text = "30-60 分钟",
                isSelected = selectedMin == 30 && selectedMax == 60,
                onClick = {
                    selectedMin = 30
                    selectedMax = 60
                    onMinChange(30)
                    onMaxChange(60)
                }
            )

            FilterChip(
                text = "60 分钟以上",
                isSelected = selectedMin == 60 && selectedMax == null,
                onClick = {
                    selectedMin = 60
                    selectedMax = null
                    onMinChange(60)
                    onMaxChange(null)
                }
            )
        }

        // 自定义范围
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = selectedMin?.toString() ?: "",
                onValueChange = {
                    val value = it.toIntOrNull()
                    if (value != null && value >= 0) {
                        selectedMin = value
                        onMinChange(value)
                    }
                },
                label = { Text("最短") },
                singleLine = true,
                modifier = Modifier.width(80.dp),
                enabled = selectedMax != null
            )

            Text("至", modifier = Modifier.padding(horizontal = 4.dp))

            OutlinedTextField(
                value = selectedMax?.toString() ?: "",
                onValueChange = {
                    val value = it.toIntOrNull()
                    if (value != null && value >= 0) {
                        selectedMax = value
                        onMaxChange(value)
                    }
                },
                label = { Text("最长") },
                singleLine = true,
                modifier = Modifier.width(80.dp),
                enabled = selectedMin != null
            )
        }
    }
}
