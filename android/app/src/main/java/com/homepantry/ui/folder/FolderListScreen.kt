package com.homepantry.ui.folder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homepantry.data.dao.FolderDao
import com.homepantry.viewmodel.FolderViewModel

/**
 * 收藏夹列表页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderListScreen(
    folderDao: FolderDao,
    onFolderClick: (String) -> Unit,
    onCreateFolder: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FolderViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的收藏") },
                actions = {
                    IconButton(onClick = onCreateFolder) {
                        Icon(Icons.Default.Add, contentDescription = "创建收藏夹")
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.folders.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("还没有收藏夹，点击右上角创建")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(uiState.folders) { folderWithCount ->
                        FolderItem(
                            folder = folderWithCount.folder,
                            recipeCount = folderWithCount.recipe_count,
                            onClick = { onFolderClick(folderWithCount.folder.id) }
                        )
                    }
                }
            }
        }

        // 显示错误消息
        uiState.error?.let { error ->
            LaunchedEffect(error) {
                // 可以显示 Snackbar
            }
        }

        // 显示成功消息
        uiState.successMessage?.let { message ->
            LaunchedEffect(message) {
                viewModel.clearMessages()
            }
        }
    }
}

@Composable
fun FolderItem(
    folder: com.homepantry.data.entity.Folder,
    recipeCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                imageVector = when (folder.icon) {
                    "star" -> Icons.Default.Star
                    else -> Icons.Default.Star
                },
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

            if (folder.isSystem) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "系统默认",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
