package com.homepantry.ui.folder

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 编辑收藏夹对话框
 */
@Composable
fun EditFolderDialog(
    folder: com.homepantry.data.entity.Folder,
    onDismiss: () -> Unit,
    onUpdate: (com.homepantry.data.entity.Folder) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(folder.name) }
    var selectedIcon by remember { mutableStateOf<String?>(folder.icon) }
    var selectedColor by remember { mutableStateOf<String?>(folder.color) }
    var showError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑收藏夹") },
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
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !folder.isSystem
                )

                // 图标选择
                Text("图标")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconSelector(
                        icon = "star",
                        selected = selectedIcon == "star",
                        onClick = { selectedIcon = "star" },
                        enabled = !folder.isSystem
                    )
                    IconSelector(
                        icon = "restaurant",
                        selected = selectedIcon == "restaurant",
                        onClick = { selectedIcon = "restaurant" },
                        enabled = !folder.isSystem
                    )
                    IconSelector(
                        icon = "eco",
                        selected = selectedIcon == "eco",
                        onClick = { selectedIcon = "eco" },
                        enabled = !folder.isSystem
                    )
                    IconSelector(
                        icon = "schedule",
                        selected = selectedIcon == "schedule",
                        onClick = { selectedIcon = "schedule" },
                        enabled = !folder.isSystem
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
                        onClick = { selectedColor = "#FF6B35" },
                        enabled = !folder.isSystem
                    )
                    ColorSelector(
                        color = "#27AE60",
                        selected = selectedColor == "#27AE60",
                        onClick = { selectedColor = "#27AE60" },
                        enabled = !folder.isSystem
                    )
                    ColorSelector(
                        color = "#3498DB",
                        selected = selectedColor == "#3498DB",
                        onClick = { selectedColor = "#3498DB" },
                        enabled = !folder.isSystem
                    )
                    ColorSelector(
                        color = "#F39C12",
                        selected = selectedColor == "#F39C12",
                        onClick = { selectedColor = "#F39C12" },
                        enabled = !folder.isSystem
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
                        else -> onUpdate(folder.copy(
                            name = name,
                            icon = selectedIcon,
                            color = selectedColor
                        ))
                    }
                },
                enabled = !folder.isSystem
            ) {
                Text("更新")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        // 如果不是系统默认收藏夹，显示删除按钮
        dismissButton = {
            if (!folder.isSystem) {
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            }
        }
    )
}
