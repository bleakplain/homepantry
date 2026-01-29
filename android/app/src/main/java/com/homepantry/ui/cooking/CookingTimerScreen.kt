package com.homepantry.ui.cooking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homepantry.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingTimerScreen(
    recipeName: String,
    totalMinutes: Int,
    onBackClick: () -> Unit
) {
    var totalTime by remember { mutableStateOf(totalMinutes * 60L) }
    var timeRemaining by remember { mutableStateOf(totalMinutes * 60L) }
    var isRunning by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(1) }

    LaunchedEffect(isRunning) {
        while (isRunning && timeRemaining > 0) {
            delay(1000L)
            timeRemaining--
        }
        if (timeRemaining == 0) {
            isRunning = false
        }
    }

    val progress = if (totalTime > 0) {
        timeRemaining.toFloat() / totalTime.toFloat()
    } else {
        0f
    }

    val minutes = timeRemaining / 60
    val seconds = timeRemaining % 60

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
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Timer Display
            Box(
                modifier = Modifier
                    .size(280.dp),
                    .background(
                        if (timeRemaining == 0) AccentRed.copy(alpha = 0.1f)
                        else PrimaryLight.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (timeRemaining == 0) AccentRed else Primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (timeRemaining == 0) "时间到！" else "剩余时间",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurfaceVariant
                    )

                    // Progress indicator
                    if (totalTime > 0 && timeRemaining > 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .width(200.dp)
                                .height(8.dp),
                            color = Primary,
                            trackColor = SurfaceVariant,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Control Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                FilledIconButton(
                    onClick = {
                        timeRemaining = totalTime
                        isRunning = false
                    },
                    modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = SurfaceVariant
                    )
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "重置",
                        tint = OnSurface
                    )
                }

                // Play/Pause Button
                FilledIconButton(
                    onClick = {
                        if (timeRemaining > 0) {
                            isRunning = !isRunning
                        }
                    },
                    modifier = Modifier.size(80.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Primary
                    )
                ) {
                    Icon(
                        if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "暂停" else "开始",
                        tint = OnPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Add Time Button
                FilledIconButton(
                    onClick = {
                        timeRemaining += 60
                        totalTime += 60
                    },
                    modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = SurfaceVariant
                    )
                ) {
                    Text(
                        text = "+1m",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Quick Add Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = false,
                    onClick = {
                        timeRemaining += 30
                        totalTime += 30
                    },
                    label = { Text("+30秒") },
                    shape = RoundedCornerShape(20.dp)
                )
                FilterChip(
                    selected = false,
                    onClick = {
                        timeRemaining += 300
                        totalTime += 300
                    },
                    label = { Text("+5分钟") },
                    shape = RoundedCornerShape(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Completion Message
            if (timeRemaining == 0) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AccentGreen.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎉",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "菜谱完成！",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AccentGreen
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "享受你的美味佳肴",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
