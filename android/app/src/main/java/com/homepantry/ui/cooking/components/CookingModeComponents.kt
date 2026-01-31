package com.homepantry.ui.cooking.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homepantry.data.entity.RecipeInstruction
import com.homepantry.ui.cooking.CookingTimerStatus
import com.homepantry.ui.theme.*

/**
 * Step progress indicator showing current step and total steps with dots
 */
@Composable
fun StepProgressIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "步骤 $currentStep/$totalSteps",
            style = MaterialTheme.typography.titleMedium,
            color = OnSurfaceVariant
        )

        // Progress dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(totalSteps) { index ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (index < currentStep) Primary
                            else SurfaceVariant,
                            CircleShape
                        )
                )
            }
        }
    }
}

/**
 * Voice speaking indicator shown when TTS is active
 */
@Composable
fun VoiceSpeakingIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "🔊 播报中...",
            style = MaterialTheme.typography.bodyMedium,
            color = Primary
        )
    }
}

/**
 * Control buttons for navigation and playback
 */
@Composable
fun ControlButtons(
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPlayPause: () -> Unit,
    isPlaying: Boolean,
    onStop: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous step button
        FilledIconButton(
            onClick = onPrevious,
            modifier = Modifier.size(64.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = SurfaceVariant
            )
        ) {
            Icon(
                Icons.Default.KeyboardArrowLeft,
                contentDescription = "上一步",
                tint = OnSurface,
                modifier = Modifier.size(32.dp)
            )
        }

        // Play/Pause button
        FilledIconButton(
            onClick = onPlayPause,
            modifier = Modifier.size(72.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Primary
            )
        ) {
            Icon(
                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "暂停" else "播放",
                tint = OnPrimary,
                modifier = Modifier.size(36.dp)
            )
        }

        // Next step button
        FilledIconButton(
            onClick = onNext,
            modifier = Modifier.size(64.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = SurfaceVariant
            )
        ) {
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "下一步",
                tint = OnSurface,
                modifier = Modifier.size(32.dp)
            )
        }

        // Complete button
        FilledIconButton(
            onClick = onStop,
            modifier = Modifier.size(64.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = AccentGreen
            )
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = "完成",
                tint = OnPrimary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * Gesture hint at the bottom of the screen
 */
@Composable
fun GestureHint() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "← 滑动切换步骤",
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariant
        )
    }
}

/**
 * Cooking completed content with celebration and action buttons
 */
@Composable
fun CookingCompletedContent(
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎉",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "烹饪完成！",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = AccentGreen
        )
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary
                )
            ) {
                Text("再来一次")
            }
            Button(
                onClick = onExit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceVariant
                )
            ) {
                Text("完成")
            }
        }
    }
}

/**
 * Error content with message and back button
 */
@Composable
fun ErrorContent(
    message: String?,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚠️",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message ?: "发生错误",
            style = MaterialTheme.typography.titleLarge,
            color = AccentRed
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onBack) {
            Text("返回")
        }
    }
}

/**
 * Main step content with instruction text and details
 */
@Composable
fun StepContent(
    step: RecipeInstruction?,
    isSpeaking: Boolean,
    activeTimers: List<CookingTimerStatus>,
    onTimerClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Step number
        Text(
            text = "步骤 ${step?.stepNumber ?: 0}",
            style = MaterialTheme.typography.headlineMedium,
            color = OnSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Step description (large font)
        Text(
            text = step?.instruction ?: "",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = OnSurface,
            textAlign = TextAlign.Center,
            fontSize = 32.sp
        )

        // Key step highlight
        if (step?.isKeyStep == true) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = AccentYellow.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "⚠️ 关键步骤",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = AccentYellow
                )
            }
        }

        // Reminder info
        step?.reminder?.let { reminder ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = AccentRed.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💡 ",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = reminder,
                        style = MaterialTheme.typography.titleMedium,
                        color = AccentRed
                    )
                }
            }
        }

        // Timer display
        activeTimers.forEach { timer ->
            if (timer.stepNumber == step?.stepNumber) {
                Spacer(modifier = Modifier.height(24.dp))
                StepTimerDisplay(
                    timer = timer,
                    onClick = { onTimerClick(timer.stepNumber) }
                )
            }
        }
    }
}

/**
 * Step timer display with countdown and progress bar
 */
@Composable
fun StepTimerDisplay(
    timer: CookingTimerStatus,
    onClick: () -> Unit
) {
    val minutes = timer.remainingSeconds / 60
    val seconds = timer.remainingSeconds % 60

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                timer.remainingSeconds <= 30 -> AccentRed.copy(alpha = 0.1f)
                timer.remainingSeconds <= 60 -> AccentYellow.copy(alpha = 0.1f)
                else -> PrimaryLight.copy(alpha = 0.1f)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⏱ 计时中",
                style = MaterialTheme.typography.titleMedium,
                color = OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = when {
                    timer.remainingSeconds <= 30 -> AccentRed
                    timer.remainingSeconds <= 60 -> AccentYellow
                    else -> Primary
                }
            )

            // Progress bar
            LinearProgressIndicator(
                progress = { timer.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .height(12.dp),
                color = when {
                    timer.remainingSeconds <= 30 -> AccentRed
                    timer.remainingSeconds <= 60 -> AccentYellow
                    else -> Primary
                },
                trackColor = SurfaceVariant
            )
        }
    }
}

/**
 * Main cooking mode content layout
 */
@Composable
fun CookingModeContent(
    currentStep: RecipeInstruction?,
    totalSteps: Int,
    progress: Int,
    isPlaying: Boolean,
    isSpeaking: Boolean,
    isVoiceEnabled: Boolean,
    activeTimers: List<CookingTimerStatus>,
    dragOffset: Float,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onTimerClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress indicator
        StepProgressIndicator(
            currentStep = progress,
            totalSteps = totalSteps,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        // Main content area (supports gesture swipe)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .offset(x = dragOffset.dp)
        ) {
            StepContent(
                step = currentStep,
                isSpeaking = isSpeaking,
                activeTimers = activeTimers,
                onTimerClick = onTimerClick
            )
        }

        // Voice status indicator
        if (isVoiceEnabled && isSpeaking) {
            VoiceSpeakingIndicator()
        }

        // Control buttons
        ControlButtons(
            onPrevious = onPrevious,
            onNext = onNext,
            onPlayPause = onPlayPause,
            isPlaying = isPlaying,
            onStop = onStop
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Gesture hint
        GestureHint()
    }
}
