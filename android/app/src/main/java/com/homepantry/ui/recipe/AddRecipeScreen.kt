package com.homepantry.ui.recipe

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
import com.homepantry.data.entity.DifficultyLevel
import com.homepantry.data.entity.RecipeIngredient
import com.homepantry.data.entity.RecipeInstruction
import com.homepantry.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    var recipeName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var cookingTime by remember { mutableStateOf("") }
    var servings by remember { mutableStateOf("2") }
    var difficulty by remember { mutableStateOf(DifficultyLevel.EASY) }

    val ingredients = remember { mutableStateListOf<RecipeIngredient>() }
    val instructions = remember { mutableStateListOf<String>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加菜谱", fontWeight = FontWeight.Bold) },
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
                            singleLine = true
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
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = servings,
                                onValueChange = { servings = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("份数") },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
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
                                ingredients.add(
                                    RecipeIngredient(
                                        recipeId = "",
                                        ingredientId = "",
                                        quantity = 0.0
                                    )
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
                                            ingredients[index] = updated
                                        },
                                        onDelete = {
                                            ingredients.removeAt(index)
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
                                instructions.add("")
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
                                    StepInputRow(
                                        stepNumber = index + 1,
                                        step = step,
                                        onStepChange = { updated ->
                                            instructions[index] = updated
                                        },
                                        onDelete = {
                                            instructions.removeAt(index)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Save Button
            item {
                Button(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("保存菜谱", style = MaterialTheme.typography.titleMedium)
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun DifficultySelector(
    selected: DifficultyLevel,
    onDifficultySelected: (DifficultyLevel) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "难度",
            style = MaterialTheme.typography.labelMedium,
            color = OnSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DifficultyLevel.values().forEach { level ->
                FilterChip(
                    selected = selected == level,
                    onClick = { onDifficultySelected(level) },
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
    }
}

@Composable
fun IngredientInputRow(
    ingredient: RecipeIngredient,
    onIngredientChange: (RecipeIngredient) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                onIngredientChange(ingredient.copy(ingredientId = it))
            },
            modifier = Modifier.weight(2f),
            label = { Text("食材名称") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        OutlinedTextField(
            value = quantity,
            onValueChange = {
                quantity = it
                onIngredientChange(ingredient.copy(quantity = it.toDoubleOrNull() ?: 0.0))
            },
            modifier = Modifier.weight(1f),
            label = { Text("数量") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "删除")
        }
    }
}

@Composable
fun StepInputRow(
    stepNumber: Int,
    step: String,
    onStepChange: (String) -> Unit,
    onDelete: () -> Unit
) {
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
                text = stepNumber.toString(),
                color = OnPrimary,
                style = MaterialTheme.typography.labelSmall
            )
        }
        OutlinedTextField(
            value = step,
            onValueChange = onStepChange,
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
