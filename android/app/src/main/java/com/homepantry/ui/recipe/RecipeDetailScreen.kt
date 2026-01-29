package com.homepantry.ui.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homepantry.data.entity.DifficultyLevel
import com.homepantry.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    onEditClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var isFavorite by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("菜谱详情", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { isFavorite = !isFavorite }) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "收藏",
                            tint = if (isFavorite) AccentRed else OnPrimary
                        )
                    }
                    IconButton(onClick = { onEditClick("recipe-id") }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = OnPrimary,
                    navigationIconContentColor = OnPrimary,
                    actionIconContentColor = OnPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Recipe Image
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PrimaryLight.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🍳",
                        fontSize = 64.sp
                    )
                }
            }

            // Recipe Title and Info
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "番茄炒蛋",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground
                    )

                    Text(
                        text = "经典的家常菜，简单易做，营养丰富",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        InfoChip(icon = "⏱", text = "15分钟")
                        InfoChip(icon = "👥", text = "2人份")
                        InfoChip(icon = "📊", text = "简单")
                    }
                }
            }

            // Ingredients Section
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SectionTitle(title = "食材")

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IngredientRow(name = "番茄", quantity = "2个")
                        IngredientRow(name = "鸡蛋", quantity = "3个")
                        IngredientRow(name = "食用油", quantity = "2勺")
                        IngredientRow(name = "盐", quantity = "适量")
                        IngredientRow(name = "葱花", quantity = "少许")
                    }
                }
            }

            // Instructions Section
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SectionTitle(title = "步骤")

                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        InstructionStep(
                            stepNumber = 1,
                            text = "番茄洗净切块，鸡蛋打散备用"
                        )
                        InstructionStep(
                            stepNumber = 2,
                            text = "热锅下油，倒入鸡蛋液炒熟盛起"
                        )
                        InstructionStep(
                            stepNumber = 3,
                            text = "锅中留底油，下番茄块炒出汁水"
                        )
                        InstructionStep(
                            stepNumber = 4,
                            text = "倒入炒蛋，加盐调味，翻炒均匀"
                        )
                        InstructionStep(
                            stepNumber = 5,
                            text = "撒上葱花，出锅装盘"
                        )
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun InfoChip(icon: String, text: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = PrimaryLight.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 14.sp)
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = OnSurface
            )
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = OnBackground
    )
}

@Composable
fun IngredientRow(name: String, quantity: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = OnSurface
        )
        Text(
            text = quantity,
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant
        )
    }
}

@Composable
fun InstructionStep(stepNumber: Int, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber.toString(),
                color = OnPrimary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = OnSurface,
            lineHeight = 22.sp
        )
    }
}
