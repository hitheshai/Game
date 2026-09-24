package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.ZenAmber
import com.example.ui.theme.ZenBlue
import com.example.ui.theme.ZenPink
import com.example.ui.theme.ZenPurple
import com.example.ui.theme.ZenTeal
import com.example.viewmodel.ZenViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.abs

data class RippleWave(
    val id: Long,
    val birthTime: Long,
    val durationMs: Long = 1800,
    val noteIndex: Int
)

@Composable
fun ReactionScreen(
    viewModel: ZenViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var harmonyScore by remember { mutableIntStateOf(0) }
    var currentStreak by remember { mutableIntStateOf(0) }
    var feedbackText by remember { mutableStateOf("Tap when the wave hits the ring") }
    var feedbackColor by remember { mutableStateOf(ZenTeal) }
    val defaultFeedbackColor = MaterialTheme.colorScheme.onSurfaceVariant

    val waves = remember { mutableStateListOf<RippleWave>() }
    var beatCounter by remember { mutableIntStateOf(0) }

    // Wave generation loop
    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        while (isActive && isPlaying) {
            waves.add(
                RippleWave(
                    id = System.nanoTime(),
                    birthTime = System.currentTimeMillis(),
                    durationMs = 1800,
                    noteIndex = beatCounter % 8
                )
            )
            beatCounter++
            delay(1300) // gentle, relaxing tempo (~46 bpm)
        }
    }

    // Animation frame ticker to clear expired waves
    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        while (isActive) {
            delay(16)
            val now = System.currentTimeMillis()
            waves.removeAll { now - it.birthTime > it.durationMs + 200 }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("reaction_screen")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("reaction_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = "Harmonic Ripple",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(ZenPurple.copy(alpha = 0.2f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Score: $harmonyScore",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = ZenPurple
                )
            }
        }

        // Streak & Feedback
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = feedbackText,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = feedbackColor
            )
            if (currentStreak > 1) {
                Text(
                    text = "🌟 Harmony x$currentStreak",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = ZenAmber
                )
            }
        }

        // Tap area & Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0F172A), Color(0xFF1E1B4B))
                    )
                )
                .clickable {
                    if (!isPlaying) {
                        isPlaying = true
                        return@clickable
                    }

                    val now = System.currentTimeMillis()
                    // Find closest wave to target progress (0.75)
                    var bestWave: RippleWave? = null
                    var minDiff = Float.MAX_VALUE

                    for (wave in waves) {
                        val progress = ((now - wave.birthTime).toFloat() / wave.durationMs).coerceIn(0f, 1f)
                        val diff = abs(progress - 0.75f)
                        if (diff < minDiff) {
                            minDiff = diff
                            bestWave = wave
                        }
                    }

                    if (bestWave != null && minDiff <= 0.18f) {
                        // Perfect or Good Hit
                        val isPerfect = minDiff <= 0.08f
                        currentStreak++
                        harmonyScore += if (isPerfect) 20 else 10
                        feedbackText = if (isPerfect) "✨ Perfect Harmony!" else "🎵 Sweet Rhythm!"
                        feedbackColor = if (isPerfect) ZenAmber else ZenTeal

                        viewModel.soundManager.playChime(bestWave.noteIndex)
                        viewModel.hapticsManager.playLightTap()

                        if (harmonyScore >= 100 && harmonyScore - (if (isPerfect) 20 else 10) < 100) {
                            viewModel.unlockAchievement("ach_rhythm_peace")
                            viewModel.addXpAndCoins(xp = 50, coins = 25, celebrate = true)
                        } else if (harmonyScore % 50 == 0) {
                            viewModel.addXpAndCoins(xp = 20, coins = 10)
                        }
                    } else {
                        // Gentle tap
                        feedbackText = "Gently match the outer ring"
                        feedbackColor = defaultFeedbackColor
                        currentStreak = 0
                        viewModel.soundManager.playBubblePop()
                        viewModel.hapticsManager.playLightTap()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val maxRadius = size.width * 0.45f
                val targetRadius = maxRadius * 0.75f
                val now = System.currentTimeMillis()

                // Draw target harmony ring
                drawCircle(
                    color = ZenTeal.copy(alpha = 0.45f),
                    center = center,
                    radius = targetRadius,
                    style = Stroke(width = 3.dp.toPx())
                )
                drawCircle(
                    color = ZenTeal.copy(alpha = 0.15f),
                    center = center,
                    radius = targetRadius,
                    style = Stroke(width = 12.dp.toPx())
                )

                // Draw expanding rhythm ripples
                waves.forEach { wave ->
                    val progress = ((now - wave.birthTime).toFloat() / wave.durationMs).coerceIn(0f, 1f)
                    val r = maxRadius * progress
                    val alpha = (1f - progress * 0.8f).coerceIn(0f, 1f)

                    drawCircle(
                        color = ZenPurple.copy(alpha = alpha * 0.75f),
                        center = center,
                        radius = r,
                        style = Stroke(width = 4.dp.toPx())
                    )
                }

                // Center core orb
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ZenPink, ZenPurple),
                        center = center,
                        radius = 28.dp.toPx()
                    ),
                    center = center,
                    radius = 28.dp.toPx()
                )
            }

            if (!isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { isPlaying = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ZenTeal),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start Rhythm", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}
