package com.homepantry.ui.filter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.windowsize
import androidx.compose.ui.unit.VelocityBased
import com.homepantry.viewmodel.FilterViewModel
import com.homepantry.viewmodel.FilterDialogViewModel

/**
 * 筛选对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    onDismiss: () -> Unit,
    onApply: (com.homepantry.data.entity.RecipeFilterCriteria) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 全局状态管理
    val filterViewModel: FilterViewModel = viewModel()
    val filterDialogViewModel: FilterDialogViewModel = viewModel()

    // 监听状态
    val filterUiState by filterViewModel.uiState.collectAsState()
    val filterDialogUiState by filterDialogViewModel.uiState.collectAsState()

    // 滚动状态
    val scrollState = rememberScrollState()

    val windowSize = androidx.compose.ui.unit.dpWindowSizeSize()
    val bottomSheetPeekHeight = 300.dp

    // BottomSheet 配置
    val bottomSheetState = rememberModalBottomSheetState(
        confirmValueChange = {
            it
        },
        skipHalfExpanded = false
    )

    BottomSheetScaffold(
        sheetState = bottomSheetState,
        scaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetPeekHeight = bottomSheetPeekHeight
        ),
        sheetPeekHeight = bottomSheetPeekHeight,
        sheetContent = {
            FilterDialogContent(
                filterUiState = filterUiState,
                filterDialogUiState = filterDialogUiState,
                onDismiss = onDismiss,
                onApply = onApply,
                onClear = onClear,
                scrollState = scrollState
            )
        },
        sheetDragHandle = {
            BottomSheetDefaults.DragHandle()
        }
    ) {
        // 主内容（菜谱列表）
    }
}

@Composable
fun FilterDialogContent(
    filterUiState: com.homepantry.viewmodel.FilterViewModel.FilterUiState,
    filterDialogUiState: com.homepantry.viewmodel.FilterDialogViewModel.FilterDialogUiState,
    onDismiss: () -> Unit,
    onApply: (com.homepantry.data.entity.RecipeFilterCriteria) -> Unit,
    onClear: () -> Unit,
    scrollState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 标题
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "筛选菜谱",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = onClear) {
                Text("清除")
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // 筛选条件列表（使用滚动）
        androidx.compose.foundation.lazy.LazyColumn(
            state = scrollState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                CookingTimeFilterSection(
                    min = filterUiState.currentCriteria.cookingTimeMin,
                    max = filterUiState.currentCriteria.cookingTimeMax,
                    onMinChange = { /* TODO */ },
                    onMaxChange = { /* TODO */ }
                )
            }

            item {
                DifficultyFilterSection(
                    selectedDifficulties = filterUiState.currentCriteria.getDifficulties(),
                    onToggle = { /* TODO */ }
                )
            }

            item {
                IngredientFilterSection(
                    ingredients = filterDialogUiState.ingredients,
                    includedIngredientIds = filterUiState.currentCriteria.includedIngredients,
                    excludedIngredientIds = filterUiState.currentCriteria.excludedIngredients,
                    searchQuery = filterDialogUiState.searchQuery,
                    onSearchQueryChange = { /* TODO */ },
                    onIncludeIngredient = { /* TODO */ },
                    onExcludeIngredient = { /* TODO */ },
                    onUseAvailableIngredients = { /* TODO */ }
                )
            }

            item {
                CategoryFilterSection(
                    categories = filterDialogUiState.categories,
                    selectedCategoryIds = filterUiState.currentCriteria.categoryIds,
                    onToggle = { /* TODO */ }
                )
            }

            item {
                PresetsSection(
                    onApplyQuickMeal = { /* TODO */ },
                    onApplySimple = { /* TODO */ },
                    onApplyHealthy = { /* TODO */ }
                )
            }
        }

        // 底部按钮
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("取消")
            }

            Button(
                onClick = onApply,
                modifier = Modifier.weight(1f),
                enabled = !filterUiState.currentCriteria.isEmpty()
            ) {
                Text("应用筛选")
            }
        }
    }

    // 错误提示
    filterUiState.error?.let { error ->
        Snackbar(
            modifier = Modifier.fillMaxWidth(),
            action = {
                TextButton(onClick = { filterViewModel.clearMessages() }) {
                    Text("关闭")
                }
            }
        ) {
            Text(text = error)
        }
    }

    // 成功提示
    filterUiState.successMessage?.let { message ->
        Snackbar(
            modifier = Modifier.fillMaxWidth(),
            action = {
                TextButton(onClick = { filterViewModel.clearMessages() }) {
                    Text("关闭")
                }
            }
        ) {
            Text(text = message)
        }
    }
}
