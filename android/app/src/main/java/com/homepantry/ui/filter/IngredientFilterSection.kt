package com.homepantry.ui.filter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 食材筛选部分
 */
@Composable
fun IngredientFilterSection(
    ingredients: List<com.homepantry.data.entity.Ingredient>,
    includedIngredientIds: Set<String>,
    excludedIngredientIds: Set<String>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onIncludeIngredient: (String) -> Unit,
    onExcludeIngredient: (String) -> Unit,
    onUseAvailableIngredients: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "食材",
            style = MaterialTheme.typography.titleSmall
        )

        // 搜索框
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    text = "搜索食材...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "搜索",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // "使用现有食材"按钮
        FilledTonalButton(
            onClick = onUseAvailableIngredients,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "使用现有食材",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("使用现有食材")
        }
    }

    Divider(modifier = Modifier.padding(vertical = 8.dp))

    // 已选食材列表
    if (includedIngredientIds.isNotEmpty() || excludedIngredientIds.isNotEmpty()) {
        Text(
            text = "已选择：${includedIngredientIds.size + excludedIngredientIds.size} 个",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 显示已选的包含食材
        items(
            items = ingredients.filter { includedIngredientIds.contains(it.id) }
        ) { ingredient ->
            IngredientItem(
                ingredient = ingredient,
                type = IngredientType.INCLUDED,
                onClick = { onExcludeIngredient(ingredient.id) }
            )
        }

        // 显示已选的排除食材
        items(
            items = ingredients.filter { excludedIngredientIds.contains(it.id) }
        ) { ingredient ->
            IngredientItem(
                ingredient = ingredient,
                type = IngredientType.EXCLUDED,
                onClick = { onIncludeIngredient(ingredient.id) }
            )
        }

        // 显示未选的食材
        val selectedIds = includedIngredientIds + excludedIngredientIds
        items(
            items = ingredients.filter { !selectedIds.contains(it.id) }
                .take(10) // 只显示前 10 个未选的食材
        ) { ingredient ->
            IngredientItem(
                ingredient = ingredient,
                type = IngredientType.AVAILABLE,
                onClick = { onIncludeIngredient(ingredient.id) }
            )
        }

        if (ingredients.size > 20) {
            Text(
                text = "显示 10/${ingredients.size} 个未选食材...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
fun IngredientItem(
    ingredient: com.homepantry.data.entity.Ingredient,
    type: IngredientType,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 类型图标
        Icon(
            imageVector = when (type) {
                IngredientType.INCLUDED -> androidx.compose.material.icons.filled.Check
                IngredientType.EXCLUDED -> androidx.compose.material.icons.filled.Close
                IngredientType.AVAILABLE -> androidx.compose.material.icons.filled.Add
            },
            contentDescription = null,
            tint = when (type) {
                IngredientType.INCLUDED -> MaterialTheme.colorScheme.primary
                IngredientType.EXCLUDED -> MaterialTheme.colorScheme.error
                IngredientType.AVAILABLE -> MaterialTheme.colorScheme.secondary
            },
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 食材名称
        Text(
            text = ingredient.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

enum class IngredientType {
    INCLUDED, EXCLUDED, AVAILABLE
}
