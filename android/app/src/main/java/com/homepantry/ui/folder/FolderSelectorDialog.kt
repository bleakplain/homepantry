package com.homepantry.ui.folder

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 收藏夹选择对话框
 */
@Composable
fun FolderSelectorDialog(
    folders: List<com.homepantry.data.dao.FolderDao.FolderWithCount>,
    selectedFolderIds: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit,
    onCreateFolder: () -> Unit
) {
    var selectedIds by remember { mutableStateOf(selectedFolderIds) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择收藏夹") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
            ) {
                LazyColumn {
                    items(folders) { folderWithCount ->
                        val isSelected = selectedIds.contains(folderWithCount.folder.id)
                        
                        FolderSelectorItem(
                            folder = folderWithCount.folder,
                            recipeCount = folderWithCount.recipe_count,
                            isSelected = isSelected,
                            onClick = {
                                selectedIds = if (isSelected) {
                                    selectedIds - folderWithCount.folder.id
                                } else {
                                    selectedIds + folderWithCount.folder.id
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(selectedIds)
                }
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        iconButton = {
            IconButton(onClick = onCreateFolder) {
                Icon(Icons.Outlined.Create, contentDescription = "创建收藏夹")
            }
        }
    )
}

@Composable
fun FolderSelectorItem(
    folder: com.homepantry.data.entity.Folder,
    recipeCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            val iconColor = folder.color?.let { Color(android.graphics.Color.parseColor(it)) }
                ?: MaterialTheme.colorScheme.primary

            Icon(
                imageVector = androidx.compose.material.icons.filled.Check,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 名称和数量
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$recipeCount 个菜谱",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 选中图标
            if (isSelected) {
                Icon(
                    imageVector = androidx.compose.material.icons.filled.Check,
                    contentDescription = "已选择",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
