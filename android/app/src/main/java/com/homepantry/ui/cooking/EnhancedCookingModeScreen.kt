package com.homepantry.ui.cooking

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homepantry.data.entity.RecipeInstruction
import com.homepantry.ui.cooking.components.*
import com.homepantry.ui.theme.*

/**
 * Enhanced cooking mode screen with voice playback, gesture controls, and step navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedCookingModeScreen(
    recipeId: String,
    recipeName: String,
    instructions: List<RecipeInstruction>,
    onBackClick: () -> Unit,
    onComplete: () -> Unit,
    viewModel: CookingModeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isVoiceEnabled by viewModel.isVoiceEnabled.collectAsState()

    // Initialize cooking mode
    LaunchedEffect(Unit) {
        viewModel.initialize(instructions)
    }

    // Handle gesture swipe
    var dragOffset by remember { mutableStateOf(0f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "烹饪模式",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = recipeName,
                            style = MaterialTheme.typography.bodySmall,
                            color = OnPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.stop()
                        onBackClick()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleVoice() }) {
                        Icon(
                            if (isVoiceEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = if (isVoiceEnabled) "关闭语音" else "开启语音",
                            tint = if (isVoiceEnabled) Primary else OnSurfaceVariant
                        )
                    }
                    IconButton(onClick = { viewModel.toggleSettings() }) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            dragOffset = dragAmount + dragAmount
                        },
                        onDragEnd = { offset ->
                            when {
                                offset < -100 -> viewModel.nextStep() // Swipe left: next step
                                offset > 100 -> viewModel.previousStep() // Swipe right: previous step
                            }
                            dragOffset = 0f
                        }
                    )
                }
        ) {
            when (val state = uiState) {
                is CookingModeUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is CookingModeUiState.Active -> {
                    CookingModeContent(
                        currentStep = state.currentStep,
                        totalSteps = state.totalSteps,
                        progress = state.progress,
                        isPlaying = state.isPlaying,
                        isSpeaking = state.isSpeaking,
                        isVoiceEnabled = isVoiceEnabled,
                        activeTimers = state.activeTimers,
                        dragOffset = dragOffset,
                        onNext = { viewModel.nextStep() },
                        onPrevious = { viewModel.previousStep() },
                        onPlayPause = { viewModel.togglePlayPause() },
                        onStop = {
                            viewModel.stop()
                            onComplete()
                        },
                        onTimerClick = { stepNumber ->
                            viewModel.toggleTimer(stepNumber)
                        }
                    )
                }
                is CookingModeUiState.Completed -> {
                    CookingCompletedContent(
                        onRestart = { viewModel.restart() },
                        onExit = onBackClick
                    )
                }
                is CookingModeUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onBack = onBackClick
                    )
                }
            }
        }
    }
}
