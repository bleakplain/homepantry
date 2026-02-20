package com.homepantry.ui.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Create
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homepantry.data.entity.DifficultyLevel
import com.homepantry.data.entity.Recipe
import com.homepantry.data.entity.RecipeIngredient
import com.homepantry.data.entity.RecipeInstruction
import com.homepantry.data.entity.Folder
import com.homepantry.ui.folder.FolderChip
import com.homepantry.ui.theme.*
import com.homepantry.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: String,
    folderDao: com.homepantry.data.dao.FolderDao,
    recipeFolderDao: com.homepantry.data.dao.RecipeFolderDao,
    onEditClick: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: RecipeViewModel = viewModel()
) {
    var recipe by remember { mutableStateOf<Recipe?>(null) }
    var ingredients by remember { mutableStateOf<List<RecipeIngredient>>(emptyList()) }
    var instructions by remember { mutableStateOf<List<RecipeInstruction>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // 收藏夹相关状态
    var showFolderSelector by remember { mutableStateOf(false) }
    var folderIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var folders by remember { mutableStateOf<List<com.homepantry.data.dao.FolderDao.FolderWithCount>>(emptyList()) }

    // 加载菜谱所属的收藏夹
    LaunchedEffect(recipeId) {
        viewModel.selectRecipe(recipeId)
        
        // 加载菜谱所属的收藏夹
        folderIds = folderDao.getFolderIdsByRecipeId(recipeId).toSet()
        
        // 加载所有收藏夹
        folderDao.getAllFoldersOnce().let { folderList ->
            folders = folderList.map { folder ->
                com.homepantry.data.dao.FolderDao.FolderWithCount(folder, 0)
            }
        }
        
        viewModel.selectedRecipe.collect { selectedRecipe ->
            if (selectedRecipe != null) {
                recipe = selectedRecipe
                isLoading = false
            }
        }
    }

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
                    recipe?.let { currentRecipe ->
                        IconButton(onClick = {
                            // TODO: Toggle favorite
                        }) {
                            Icon(
                                if (currentRecipe.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "收藏",
                                tint = if (currentRecipe.isFavorite) AccentRed else OnPrimary
                            )
                        }
                        IconButton(onClick = { onEditClick(recipeId) }) {
                            Icon(Icons.Default.Edit, contentDescription = "编辑")
                        }
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
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Background),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error ?: "未知错误",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            recipe == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Background),
                    contentAlignment = Alignment.Center
                ) {
                    Text("菜谱不存在")
                }
            }
            else -> {
                RecipeDetailContent(
                    recipe = recipe!!,
                    ingredients = ingredients,
                    instructions = instructions,
                    folderIds = folderIds,
                    folders = folders,
                    onAddToFolder = { showFolderSelector = true },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Background)
                        .padding(paddingValues)
                )
            }
        }
    }

    // Folder Selector Dialog
    if (showFolderSelector) {
        FolderSelectorDialog(
            folders = folders,
            selectedFolderIds = folderIds,
            onDismiss = { showFolderSelector = false },
            onConfirm = { selectedIds ->
                // TODO: 添加到收藏夹
                folderIds = selectedIds
                showFolderSelector = false
            }
        )
    }
}

@Composable
fun RecipeDetailContent(
    recipe: Recipe,
    ingredients: List<RecipeIngredient>,
    instructions: List<RecipeInstruction>,
    folderIds: Set<String>,
    folders: List<com.homepantry.data.dao.FolderDao.FolderWithCount>,
    onAddToFolder: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
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
                    text = recipe.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )

                recipe.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InfoChip(icon = "⏱", text = "${recipe.cookingTime}分钟")
                    InfoChip(icon = "👥", text = "${recipe.servings}人份")
                    InfoChip(
                        icon = "📊",
                        text = when (recipe.difficulty) {
                            DifficultyLevel.EASY -> "简单"
                            DifficultyLevel.MEDIUM -> "中等"
                            DifficultyLevel.HARD -> "困难"
                        }
                    )
                }
            }
        }

        // 收藏夹信息
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SectionTitle(title = "收藏夹")
                
                // 收藏到的收藏夹列表
                if (folderIds.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        folders.filter { folderIds.contains(it.folder.id) }
                            .forEach { folderWithCount ->
                                FolderChip(
                                    folder = folderWithCount.folder,
                                    recipeCount = folderWithCount.recipe_count
                                )
                            }
                    }
                } else {
                    Text(
                        text = "还未收藏到任何收藏夹",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
                
                // 添加到收藏夹按钮
                FilledTonalButton(
                    onClick = onAddToFolder,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Create,
                        contentDescription = "添加到收藏夹",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("收藏到收藏夹")
                }
            }
        }

        // Ingredients Section
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionTitle(title = "食材")

                if (ingredients.isEmpty()) {
                    Text(
                        text = "暂无食材信息",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ingredients.forEach { ingredient ->
                            IngredientRow(
                                name = ingredient.ingredientId,
                                quantity = "${ingredient.quantity} ${ingredient.notes ?: ""}"
                            )
                        }
                    }
                }
            }
        }

        // Instructions Section
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionTitle(title = "步骤")

                if (instructions.isEmpty()) {
                    Text(
                        text = "暂无步骤信息",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        instructions.sortedBy { it.stepNumber }.forEach { instruction ->
                            InstructionStep(
                                stepNumber = instruction.stepNumber,
                                text = instruction.instructionText
                            )
                        }
                    }
                }
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(16.dp))
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
