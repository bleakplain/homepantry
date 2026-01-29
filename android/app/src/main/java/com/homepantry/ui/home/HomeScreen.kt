package com.homepantry.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homepantry.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onRecipesClick: () -> Unit = {},
    onIngredientsClick: () -> Unit = {},
    onMealPlanClick: () -> Unit = {},
    onFamilyClick: () -> Unit = {},
    onSearchClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "家常味库",
                        fontWeight = FontWeight.Bold,
                        color = OnPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary
                ),
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "通知",
                            tint = OnPrimary
                        )
                    }
                    IconButton(onClick = onFamilyClick) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "家庭",
                            tint = OnPrimary
                        )
                    }
                }
            )
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
            SearchBar(
                onClick = onSearchClick,
                modifier = Modifier.fillMaxWidth()
            )

            // Navigation Cards
            NavigationCards(
                onRecipesClick = onRecipesClick,
                onIngredientsClick = onIngredientsClick,
                onMealPlanClick = onMealPlanClick,
                onFamilyClick = onFamilyClick
            )

            // Today's Recommendation Section
            SectionTitle(text = "今日推荐")
            RecommendationCard(
                recipeName = "番茄炒蛋",
                cookingTime = "15分钟",
                difficulty = "简单",
                modifier = Modifier.fillMaxWidth()
            )

            // Recently Added Section
            SectionTitle(text = "最近添加")
            RecentlyAddedRecipes()
        }
    }
}

@Composable
fun SearchBar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        modifier = modifier,
        placeholder = {
            Text(
                "搜索菜谱...",
                color = OnSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "搜索",
                tint = OnSurfaceVariant
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            unfocusedBorderColor = SurfaceVariant,
            disabledBorderColor = SurfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
        readOnly = true,
        onClick = { onClick() }
    )
}

@Composable
fun NavigationCards(
    onRecipesClick: () -> Unit,
    onIngredientsClick: () -> Unit,
    onMealPlanClick: () -> Unit,
    onFamilyClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NavCard(
            icon = "📖",
            label = "菜谱",
            modifier = Modifier.weight(1f),
            onClick = onRecipesClick
        )
        NavCard(
            icon = "🥬",
            label = "食材",
            modifier = Modifier.weight(1f),
            onClick = onIngredientsClick
        )
        NavCard(
            icon = "📅",
            label = "计划",
            modifier = Modifier.weight(1f),
            onClick = onMealPlanClick
        )
        NavCard(
            icon = "👨‍👩‍👧",
            label = "家庭",
            modifier = Modifier.weight(1f),
            onClick = onFamilyClick
        )
    }
}

@Composable
fun NavCard(
    icon: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = OnSurface
            )
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = OnBackground
    )
}

@Composable
fun RecommendationCard(
    recipeName: String,
    cookingTime: String,
    difficulty: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
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
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🍅",
                    fontSize = 32.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = recipeName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "⏱ $cookingTime",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = "📊 $difficulty",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun RecentlyAddedRecipes() {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(4) { index ->
            RecipeCardPlaceholder()
        }
    }
}

@Composable
fun RecipeCardPlaceholder() {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📷",
                fontSize = 40.sp,
                color = OnSurfaceVariant.copy(alpha = 0.3f)
            )
        }
    }
}
