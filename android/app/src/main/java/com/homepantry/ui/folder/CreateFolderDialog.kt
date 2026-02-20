package com.homepantry.ui.folder

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 创建收藏夹对话框
 */
@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, icon: String?, color: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf<String?>("star") }
    var selectedColor by remember { mutableStateOf<String?>("#FF6B35") }
    var showError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("创建收藏夹") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 名称输入
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        showError = null
                    },
                    label = { Text("名称") },
                    singleLine = true,
                    isError = showError != null,
                    supportingText = showError?.let { Text(it) },
                    modifier = Modifier.fillMaxWidth()
                )

                // 图标选择
                Text("图标")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconSelector(
                        icon = "star",
                        selected = selectedIcon == "star",
                        onClick = { selectedIcon = "star" }
                    )
                    IconSelector(
                        icon = "restaurant",
                        selected = selectedIcon == "restaurant",
                        onClick = { selectedIcon = "restaurant" }
                    )
                    IconSelector(
                        icon = "eco",
                        selected = selectedIcon == "eco",
                        onClick = { selectedIcon = "eco" }
                    )
                    IconSelector(
                        icon = "schedule",
                        selected = selectedIcon == "schedule",
                        onClick = { selectedIcon = "schedule" }
                    )
                }

                // 颜色选择
                Text("颜色")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ColorSelector(
                        color = "#FF6B35",
                        selected = selectedColor == "#FF6B35",
                        onClick = { selectedColor = "#FF6B35" }
                    )
                    ColorSelector(
                        color = "#27AE60",
                        selected = selectedColor == "#27AE60",
                        onClick = { selectedColor = "#27AE60" }
                    )
                    ColorSelector(
                        color = "#3498DB",
                        selected = selectedColor == "#3498DB",
                        onClick = { selectedColor = "#3498DB" }
                    )
                    ColorSelector(
                        color = "#F39C12",
                        selected = selectedColor == "#F39C12",
                        onClick = { selectedColor = "#F39C12" }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when {
                        name.length < 2 -> showError = "名称不能少于2个字符"
                        name.length > 20 -> showError = "名称不能超过20个字符"
                        else -> onConfirm(name, selectedIcon, selectedColor)
                    }
                }
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun IconSelector(
    icon: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = androidx.compose.material.icons.filled.Check,
            contentDescription = null,
            tint = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
fun ColorSelector(
    color: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .then(
                    if (selected) {
                        Modifier.padding(2.dp)
                    } else {
                        Modifier
                    }
                )
        ) {
            androidx.compose.foundation.shape.CircleShape
        }
        if (selected) {
            androidx.compose.material.icons.Icons.Default.Check
        }
    }
}
