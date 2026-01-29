package com.homepantry.ui.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homepantry.data.entity.DifficultyLevel
import com.homepantry.data.entity.Recipe
import com.homepantry.data.search.RecipeSearchFilters
import com.homepantry.data.search.RecipeSortOption
import com.homepantry.ui.theme.*
import com.homepantry.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    onRecipeClick: (String) -> Unit,
    onAddRecipeClick: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: RecipeViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }

    // Search and filter state
    var filters by remember { mutableStateOf(RecipeSearchFilters()) }
    var sortOption by remember { mutableStateOf(RecipeSortOption.NEWEST_FIRST) }

    val recipes by viewModel.recipes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Apply filters and sorting to recipes
    val filteredAndSortedRecipes = remember(filters, sortOption, recipes) {
        var result = recipes.toList()

        // Apply search query
        if (filters.query.isNotBlank()) {
            result = result.filter {
                it.name.contains(filters.query, ignoreCase = true) ||
                it.description?.contains(filters.query, ignoreCase = true) == true
            }
        }

        // Apply difficulty filter
        if (filters.difficulties.isNotEmpty()) {
            result = result.filter { it.difficulty in filters.difficulties }
        }

        // Apply cooking time filter
        if (filters.maxCookingTime != null) {
            result = result.filter { it.cookingTime <= filters.maxCookingTime!! }
        }

        // Apply servings filter
        if (filters.minServings != null) {
            result = result.filter { it.servings >= filters.minServings!! }
        }
        if (filters.maxServings != null) {
            result = result.filter { it.servings <= filters.maxServings!! }
        }

        // Apply favorites filter
        if (filters.onlyFavorites) {
            result = result.filter { it.isFavorite }
        }

        // Apply sorting
        result = when (sortOption.id) {
            "newest" -> result.sortedByDescending { it.createdAt }
            "oldest" -> result.sortedBy { it.createdAt }
            "quickest" -> result.sortedBy { it.cookingTime }
            "slowest" -> result.sortedByDescending { it.cookingTime }
            "easiest" -> result.sortedBy { it.difficulty }
            "hardest" -> result.sortedByDescending { it.difficulty }
            "name_az" -> result.sortedBy { it.name }
            "name_za" -> result.sortedByDescending { it.name }
            else -> result
        }

        result
    }

    LaunchedEffect(Unit) {
        // Recipes are loaded automatically in ViewModel init
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("菜谱", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "筛选和排序",
                            tint = if (filters.hasActiveFilters) AccentRed else OnPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = OnPrimary,
                    navigationIconContentColor = OnPrimary,
                    actionIconContentColor = OnPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddRecipeClick,
                containerColor = Primary,
                contentColor = OnPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加菜谱")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    filters = filters.copy(query = it)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("搜索菜谱名称或描述...", color = OnSurfaceVariant)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "搜索",
                        tint = OnSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            filters = filters.copy(query = "")
                        }) {
                            Text("✕", color = OnSurfaceVariant)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = SurfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            )

            // Active filters display
            if (filters.hasActiveFilters) {
                FilterChipsRow(
                    filters = filters,
                    onRemoveDifficulty = { difficulty ->
                        filters = filters.copy(difficulties = filters.difficulties - difficulty)
                    },
                    onRemoveMaxTime = {
                        filters = filters.copy(maxCookingTime = null)
                    },
                    onRemoveFavorites = {
                        filters = filters.copy(onlyFavorites = false)
                    },
                    onClearAll = {
                        filters = RecipeSearchFilters()
                        searchQuery = ""
                    }
                )
            }

            // Sort option display
            if (sortOption != RecipeSortOption.NEWEST_FIRST) {
                SortOptionChip(
                    option = sortOption,
                    onChange = { sortOption = RecipeSortOption.NEWEST_FIRST }
                )
            }

            // Quick filter chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = filters.difficulties.isEmpty() &&
                              filters.maxCookingTime == null &&
                              !filters.onlyFavorites,
                    onClick = {
                        filters = RecipeSearchFilters()
                    },
                    label = { Text("全部") },
                    shape = RoundedCornerShape(20.dp)
                )

                FilterChip(
                    selected = filters.onlyFavorites,
                    onClick = {
                        filters = filters.copy(onlyFavorites = !filters.onlyFavorites)
                    },
                    label = { Text("⭐ 收藏") },
                    shape = RoundedCornerShape(20.dp)
                )

                FilterChip(
                    selected = filters.maxCookingTime == 15,
                    onClick = {
                        filters = filters.copy(
                            maxCookingTime = if (filters.maxCookingTime == 15) null else 15
                        )
                    },
                    label = { Text("⚡ 15分钟") },
                    shape = RoundedCornerShape(20.dp)
                )

                FilterChip(
                    selected = filters.difficulties.size == 1 && DifficultyLevel.EASY in filters.difficulties,
                    onClick = {
                        filters = filters.copy(
                            difficulties = if (filters.difficulties.size == 1 && DifficultyLevel.EASY in filters.difficulties)
                                emptySet()
                            else
                                setOf(DifficultyLevel.EASY)
                        )
                    },
                    label = { Text("📊 简单") },
                    shape = RoundedCornerShape(20.dp)
                )
            }

            // Error message
            error?.let {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Results count
            if (filteredAndSortedRecipes.isNotEmpty()) {
                Text(
                    text = "共 ${filteredAndSortedRecipes.size} 道菜谱",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Content
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
                filteredAndSortedRecipes.isEmpty() && recipes.isNotEmpty() -> {
                    EmptyFilterState(onClearFilters = {
                        filters = RecipeSearchFilters()
                        searchQuery = ""
                    })
                }
                recipes.isEmpty() -> {
                    EmptyRecipesState(
                        onAddRecipeClick = onAddRecipeClick
                    )
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredAndSortedRecipes, key = { it.id }) { recipe ->
                            RecipeCard(
                                recipe = recipe,
                                onClick = { onRecipeClick(recipe.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFilterDialog) {
        RecipeFilterDialog(
            currentFilters = filters,
            currentSort = sortOption,
            onDismiss = { showFilterDialog = false },
            onApply = { newFilters, newSort ->
                filters = newFilters
                sortOption = newSort
                showFilterDialog = false
            }
        )
    }
}

@Composable
fun FilterChipsRow(
    filters: RecipeSearchFilters,
    onRemoveDifficulty: (DifficultyLevel) -> Unit,
    onRemoveMaxTime: () -> Unit,
    onRemoveFavorites: () -> Unit,
    onClearAll: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "筛选条件",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            TextButton(onClick = onClearAll) {
                Text("清除全部")
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.difficulties.forEach { difficulty ->
                FilterChip(
                    selected = true,
                    onClick = { onRemoveDifficulty(difficulty) },
                    label = {
                        Text(
                            when (difficulty) {
                                DifficultyLevel.EASY -> "简单"
                                DifficultyLevel.MEDIUM -> "中等"
                                DifficultyLevel.HARD -> "困难"
                            }
                        )
                    },
                    shape = RoundedCornerShape(20.dp)
                )
            }

            if (filters.maxCookingTime != null) {
                FilterChip(
                    selected = true,
                    onClick = onRemoveMaxTime,
                    label = { Text("≤${filters.maxCookingTime}分钟") },
                    shape = RoundedCornerShape(20.dp)
                )
            }

            if (filters.onlyFavorites) {
                FilterChip(
                    selected = true,
                    onClick = onRemoveFavorites,
                    label = { Text("⭐ 收藏") },
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
    }
}

@Composable
fun SortOptionChip(
    option: RecipeSortOption,
    onChange: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = PrimaryLight.copy(alpha = 0.2f),
        onClick = onChange,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp, 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🔀",
                style = MaterialTheme.typography.bodyMedium
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmptyFilterState(onClearFilters: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🔍",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "没有找到符合条件的菜谱",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = OnSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "尝试调整筛选条件",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onClearFilters,
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary
            )
        ) {
            Text("清除筛选")
        }
    }
}

@Composable
fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Recipe Image Placeholder
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PrimaryLight.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🍳",
                fontSize = 36.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Recipe Info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = recipe.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = OnSurface
            )

            recipe.description?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏱",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${recipe.cookingTime}分钟",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👥",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${recipe.servings}人份",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }

                DifficultyBadge(difficulty = recipe.difficulty)
            }
        }
    }
    }
}

@Composable
fun DifficultyBadge(difficulty: DifficultyLevel) {
    val (text, color) = when (difficulty) {
        DifficultyLevel.EASY -> "简单" to AccentGreen
        DifficultyLevel.MEDIUM -> "中等" to AccentYellow
        DifficultyLevel.HARD -> "困难" to AccentRed
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
fun EmptyRecipesState(
    onAddRecipeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📖",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "还没有菜谱",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = OnSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "添加你的第一个菜谱开始吧",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAddRecipeClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary
            )
        ) {
            Text("添加菜谱")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeFilterDialog(
    currentFilters: RecipeSearchFilters,
    currentSort: RecipeSortOption,
    onDismiss: () -> Unit,
    onApply: (RecipeSearchFilters, RecipeSortOption) -> Unit
) {
    var filters by remember { mutableStateOf(currentFilters) }
    var selectedSort by remember { mutableStateOf(currentSort) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "筛选和排序",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Difficulty Filter
                Text(
                    text = "难度",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DifficultyLevel.values().forEach { level ->
                        val isSelected = level in filters.difficulties
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                filters = filters.copy(
                                    difficulties = if (isSelected)
                                        filters.difficulties - level
                                    else
                                        filters.difficulties + level
                                )
                            },
                            label = {
                                Text(
                                    when (level) {
                                        DifficultyLevel.EASY -> "简单"
                                        DifficultyLevel.MEDIUM -> "中等"
                                        DifficultyLevel.HARD -> "困难"
                                    }
                                )
                            },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                // Cooking Time Filter
                Text(
                    text = "烹饪时间",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15, 30, 60, null).forEach { time ->
                        FilterChip(
                            selected = filters.maxCookingTime == time,
                            onClick = {
                                filters = filters.copy(maxCookingTime = time)
                            },
                            label = {
                                Text(
                                    if (time == null) "不限"
                                    else "≤${time}分钟"
                                )
                            },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                // Favorites Filter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "仅显示收藏",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = filters.onlyFavorites,
                        onCheckedChange = {
                            filters = filters.copy(onlyFavorites = it)
                        }
                    )
                }

                // Sort Option
                Text(
                    text = "排序方式",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RecipeSortOption.ALL.forEach { option ->
                        FilterChip(
                            selected = selectedSort == option,
                            onClick = { selectedSort = option },
                            label = { Text(option.displayName) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApply(filters, selectedSort) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary
                )
            ) {
                Text("应用")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
