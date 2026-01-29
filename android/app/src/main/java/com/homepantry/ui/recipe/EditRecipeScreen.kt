package com.homepantry.ui.recipe

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun EditRecipeScreen(
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    // Reuse AddRecipeScreen logic with pre-filled data
    AddRecipeScreen(onSave = onSave, onCancel = onCancel)
}
