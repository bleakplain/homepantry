package com.homepantry.ui.recipe

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun EditRecipeScreen(
    recipeId: String? = null,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    // TODO: Load recipe data and pre-fill the form
    // For now, reuse AddRecipeScreen
    AddRecipeScreen(onSave = onSave, onCancel = onCancel)
}
