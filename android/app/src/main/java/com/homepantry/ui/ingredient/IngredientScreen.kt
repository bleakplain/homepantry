package com.homepantry.ui.ingredient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homepantry.data.entity.IngredientCategory
import com.homepantry.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientScreen(
    onBackClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(IngredientTab.PANTRY) }
    val pantryItems = remember { mutableStateListOf<PantryItemUi>() }
    val allIngredients = remember { mutableStateListOf<IngredientUi>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("食材", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = OnPrimary,
                    navigationIconContentColor = OnPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Add ingredient dialog */ },
                containerColor = Primary,
                contentColor = OnPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加食材")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues)
        ) {
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = Surface
            ) {
                IngredientTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.displayName) }
                    )
                }
            }

            when (selectedTab) {
                IngredientTab.PANTRY -> PantryContent(items = pantryItems)
                IngredientTab.INGREDIENTS -> IngredientsList(ingredients = allIngredients)
                IngredientTab.RECOMMENDATIONS -> RecommendationsContent()
            }
        }
    }
}

enum class IngredientTab(val displayName: String) {
    PANTRY("食材箱"),
    INGREDIENTS("食材库"),
    RECOMMENDATIONS("推荐菜谱")
}

@Composable
fun PantryContent(items: List<PantryItemUi>) {
    if (items.isEmpty()) {
        EmptyPantryState()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AccentRed.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "⚠️", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "2个食材即将过期",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AccentRed
                        )
                    }
                }
            }

            items(items) { item ->
                PantryItemCard(
                    item = item,
                    onDelete = { /* TODO */ }
                )
            }
        }
    }
}

@Composable
fun PantryItemCard(
    item: PantryItemUi,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.icon,
                    style = MaterialTheme.typography.headlineMedium
                )
                Column {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurface
                    )
                    Text(
                        text = "${item.quantity} ${item.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                    if (item.expiryDays != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (item.expiryDays <= 3) AccentRed.copy(alpha = 0.2f)
                                   else AccentGreen.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (item.expiryDays <= 0) "已过期"
                                      else "${item.expiryDays}天后过期",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (item.expiryDays <= 3) AccentRed else AccentGreen
                            )
                        }
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmptyPantryState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🥬",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "食材箱是空的",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = OnSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "添加食材以获取菜谱推荐",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant
        )
    }
}

@Composable
fun IngredientsList(ingredients: List<IngredientUi>) {
    if (ingredients.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "📦",
                fontSize = 64.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "食材库为空",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = OnSurface
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ingredients) { ingredient ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = ingredient.icon, fontSize = 24.sp)
                            Text(
                                text = ingredient.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = OnSurface
                            )
                        }
                        Text(
                            text = ingredient.category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendationsContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "👨‍🍳",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "菜谱推荐",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = OnSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "基于你的食材箱推荐菜谱",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant
        )
    }
}

// UI Models
data class PantryItemUi(
    val id: String,
    val name: String,
    val quantity: Double,
    val unit: String,
    val expiryDays: Int? = null,
    val icon: String = "🥕"
)

data class IngredientUi(
    val id: String,
    val name: String,
    val category: IngredientCategory,
    val icon: String = "🥕"
)


private val IngredientCategory.displayName: String
    get() = when (this) {
        IngredientCategory.VEGETABLE -> "蔬菜"
        IngredientCategory.FRUIT -> "水果"
        IngredientCategory.MEAT -> "肉类"
        IngredientCategory.SEAFOOD -> "海鲜"
        IngredientCategory.DAIRY -> "乳制品"
        IngredientCategory.GRAIN -> "谷物"
        IngredientCategory.SPICE -> "调料"
        IngredientCategory.SAUCE -> "酱料"
        IngredientCategory.OTHER -> "其他"
    }
