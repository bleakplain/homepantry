// 继续添加到 ExpirationDialog.kt 的部分

@Composable
fun ExpirationTabsSection(
    selectedTab: ExpirationTab,
    onTabSelected: (ExpirationTab) -> Unit,
    summary: com.homepantry.data.entity.ExpirationSummary
) {
    TabRow(selectedTabIndex = selectedTab.ordinal) {
        ExpirationTab.values().forEach { tab ->
            Tab(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                text = when (tab) {
                    ExpirationTab.ALL -> "全部"
                    ExpirationTab.EXPIRED -> "已过期"
                    ExpirationTab.EXPIRING_SOON -> "即将过期"
                    ExpirationTab.FRESH -> "新鲜"
                }
            )
        }
    }
}

@Composable
fun ExpirationAllItemsSection(
    results: List<com.homepantry.data.entity.ExpirationCheckResult>,
    onMarkAsHandled: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(results) { result ->
            ExpirationResultItem(
                result = result,
                onMarkAsHandled = { onMarkAsHandled(result.pantryItem.id) },
                onDelete = { onDelete(result.pantryItem.id) }
            )
        }
    }
}

@Composable
fun ExpirationExpiredSection(
    results: List<com.homepantry.data.entity.ExpirationCheckResult>,
    onMarkAsHandled: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    if (results.isEmpty()) {
        Text("没有已过期的食材", color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(results) { result ->
                ExpirationResultItem(
                    result = result,
                    onMarkAsHandled = { onMarkAsHandled(result.pantryItem.id) },
                    onDelete = { onDelete(result.pantryItem.id) }
                )
            }
        }
    }
}

@Composable
fun ExpirationExpiringSoonSection(
    results: List<com.homepantry.data.entity.ExpirationCheckResult>,
    onMarkAsHandled: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    if (results.isEmpty()) {
        Text("没有即将过期的食材", color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(results) { result ->
                ExpirationResultItem(
                    result = result,
                    onMarkAsHandled = { onMarkAsHandled(result.pantryItem.id) },
                    onDelete = { onDelete(result.pantryItem.id) }
                )
            }
        }
    }
}

@Composable
fun ExpirationFreshSection(
    results: List<com.homepantry.data.entity.ExpirationCheckResult>,
    onDelete: (String) -> Unit
) {
    if (results.isEmpty()) {
        Text("没有新鲜的食材", color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(results) { result ->
                ExpirationResultItem(
                    result = result,
                    onMarkAsHandled = { /* TODO: 只有已过期的可以标记 */ },
                    onDelete = { onDelete(result.pantryItem.id) }
                )
            }
        }
    }
}
