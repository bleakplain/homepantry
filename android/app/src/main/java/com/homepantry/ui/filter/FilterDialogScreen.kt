package com.homepantry.ui.filter

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 筛选对话框（临时占位符）
 * TODO: 实现完整的筛选对话框
 */
@Composable
fun FilterDialogScreen(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("筛选菜谱") },
        text = { Text("筛选功能正在开发中...") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}
