package com.homepantry.ui.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homepantry.data.entity.DifficultyLevel
import com.homepantry.data.entity.Recipe
import com.homepantry.data.entity.RecipeIngredient
import com.homepantry.data.entity.RecipeInstruction
import com.homepantry.ui.theme.*
import com.homepantry.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecipeScreen(
    recipeId: String,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    viewModel: RecipeViewModel = viewModel()
) {
    var recipe by remember { mutableStateOf<Recipe?>(null) }
    var ingredients by remember { mutableStateOf<List<RecipeIngredient>>(emptyList()) }
    var instructions by remember { mutable.stateListOf<RecipeInstruction>() }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Form state
    var recipeName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var cookingTime by remember { mutableStateOf("") }
    var servings by remember { mutableStateOf("2") }
    var difficulty by remember { mutableStateOf(DifficultyLevel.EASY) }

    LaunchedEffect(recipeId) {
        viewModel.selectRecipe(recipeId)
        viewModel.selectedRecipe.collect { selectedRecipe ->
            if (selectedRecipe != null) {
                recipe = selectedRecipe
                recipeName = selectedRecipe.name
                description = selectedRecipe.description ?: ""
                cookingTime = selectedRecipe.cookingTime.toString()
                servings = selectedRecipe.servings.toString()
                difficulty = selectedRecipe.difficulty
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑菜谱", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = OnPrimary,
                    navigationIconContentColor = OnPrimary
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
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Background)
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Basic Info Section
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "基本信息",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                OutlinedTextField(
                                    value = recipeName,
                                    onValueChange = { recipeName = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("菜谱名称") },
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    isError = recipeName.isBlank()
                                )

                                OutlinedTextField(
                                    value = description,
                                    onValueChange = { description = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("描述（可选）") },
                                    shape = RoundedCornerShape(12.dp),
                                    minLines = 2,
                                    maxLines = 4
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = cookingTime,
                                        onValueChange = { cookingTime = it },
                                        modifier = Modifier.weight(1f),
                                        label = { Text("烹饪时间(分钟)") },
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        isError = cookingTime.toIntOrNull() == null || cookingTime.toIntOrNull()!! <= 0
                                    )
                                    OutlinedTextField(
                                        value = servings,
                                        onValueChange = { servings = it },
                                        modifier = Modifier.weight(1f),
                                        label = { Text("份数") },
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        isError = servings.toIntOrNull() == null || servings.toIntOrNull()!! <= 0
                                    )
                                }

                                DifficultySelector(
                                    selected = difficulty,
                                    onDifficultySelected = { difficulty = it }
                                )
                            }
                        }
                    }

                    // Ingredients Section
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "食材",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    TextButton(onClick = {
                                        ingredients = ingredients + RecipeIngredient(
                                            id = System.currentTimeMillis().toString(),
                                            recipeId = recipeId,
                                            ingredientId = "",
                                            quantity = 0.0
                                        )
                                    }) {
                                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("添加")
                                    }
                                }

                                if (ingredients.isEmpty()) {
                                    Text(
                                        text = "点击上方按钮添加食材",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                } else {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        ingredients.forEachIndexed { index, ingredient ->
                                            IngredientInputRow(
                                                ingredient = ingredient,
                                                onIngredientChange = { updated ->
                                                    ingredients = ingredients.toMutableList().apply { set(index, updated) }
                                                },
                                                onDelete = {
                                                    ingredients = ingredients.toMutableList().apply { removeAt(index) }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Instructions Section
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "步骤",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    TextButton(onClick = {
                                        instructions.add(
                                            RecipeInstruction(
                                                id = System.currentTimeMillis().toString(),
                                                recipeId = recipeId,
                                                stepNumber = instructions.size + 1,
                                                instructionText = ""
                                            )
                                        )
                                    }) {
                                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("添加")
                                    }
                                }

                                if (instructions.isEmpty()) {
                                    Text(
                                        text = "点击上方按钮添加步骤",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                } else {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        instructions.forEachIndexed { index, step ->
                                            StepEditRow(
                                                step = step,
                                                onStepChange = { updated ->
                                                    instructions[index] = updated
                                                },
                                                onDelete = {
                                                    instructions.removeAt(index)
                                                    // Renumber remaining steps
                                                    instructions.forEachIndexed { i, s ->
                                                        instructions[i] = s.copy(stepNumber = i + 1)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Error message
                    if (error != null) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = error!!,
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    // Save Button
                    item {
                        Button(
                            onClick = {
                                if (recipeName.isBlank()) {
                                    error = "请输入菜谱名称"
                                    return@Button
                                }
                                val time = cookingTime.toIntOrNull()
                                if (time == null || time <= 0) {
                                    error = "请输入有效的烹饪时间"
                                    return@Button
                                }
                                val servingCount = servings.toIntOrNull()
                                if (servingCount == null || servingCount <= 0) {
                                    error = "请输入有效的份数"
                                    return@Button
                                }

                                recipe?.let { currentRecipe ->
                                    val updatedRecipe = currentRecipe.copy(
                                        name = recipeName,
                                        description = description.ifBlank { null },
                                        cookingTime = time,
                                        servings = servingCount,
                                        difficulty = difficulty
                                    )
                                    viewModel.updateRecipe(updatedRecipe, ingredients, instructions.toList())
                                    onSave()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isSaving
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = OnPrimary
                                )
                            } else {
                                Text("保存菜谱", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StepEditRow(
    step: RecipeInstruction,
    onStepChange: (RecipeInstruction) -> Unit,
    onDelete: () -> Unit
) {
    var text by remember { mutableStateOf(step.instructionText) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Primary, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step.stepNumber.toString(),
                color = OnPrimary,
                style = MaterialTheme.typography.labelSmall
            )
        }
        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
                onStepChange(step.copy(instructionText = it))
            },
            modifier = Modifier.weight(1f),
            placeholder = { Text("输入步骤说明") },
            shape = RoundedCornerShape(12.dp),
            minLines = 2,
            maxLines = 4
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "删除")
        }
    }
}
