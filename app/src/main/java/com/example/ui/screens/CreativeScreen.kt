package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonColors
import com.example.ui.theme.ZenPurple
import com.example.viewmodel.ZenViewModel

data class StrokePoint(val offset: Offset)
data class DrawnPath(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float,
    val symmetryMode: Int // 0: single, 1: 4-way, 2: 8-way mandala
)

@Composable
fun CreativeScreen(
    viewModel: ZenViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentColor by remember { mutableStateOf(NeonColors[0]) }
    var strokeWidth by remember { mutableFloatStateOf(8f) }
    var symmetryMode by remember { mutableIntStateOf(1) } // Default 4-way kaleidoscope
    val drawnPaths = remember { mutableStateListOf<DrawnPath>() }
    val currentPoints = remember { mutableStateListOf<Offset>() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("creative_screen")
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
                modifier = Modifier.testTag("creative_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = "Glow Mandala Studio",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Row {
                IconButton(
                    onClick = {
                        if (drawnPaths.isNotEmpty()) {
                            drawnPaths.removeAt(drawnPaths.lastIndex)
                            viewModel.soundManager.playSoftClick()
                        }
                    },
                    modifier = Modifier.testTag("undo_button")
                ) {
                    Icon(Icons.Default.Undo, contentDescription = "Undo")
                }
                IconButton(
                    onClick = {
                        drawnPaths.clear()
                        viewModel.soundManager.playSoftClick()
                    },
                    modifier = Modifier.testTag("clear_button")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear")
                }
            }
        }

        // Symmetry Selector & Palette Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Free", "4-Way", "8-Way").forEachIndexed { index, modeLabel ->
                    val selected = symmetryMode == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) ZenPurple.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                symmetryMode = index
                                viewModel.soundManager.playSoftClick()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = modeLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (selected) ZenPurple else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.unlockAchievement("ach_creative_spirit")
                    viewModel.addXpAndCoins(xp = 40, coins = 20, celebrate = true)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ZenPurple),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Glow (+40 XP)", fontSize = 12.sp)
            }
        }

        // Color Swatches
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NeonColors.forEach { color ->
                val isSelected = currentColor == color
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable {
                            currentColor = color
                            viewModel.soundManager.playSoftClick()
                        }
                )
            }
        }

        // Canvas Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(14.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0F172A))
                .pointerInput(symmetryMode, currentColor, strokeWidth) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPoints.clear()
                            currentPoints.add(offset)
                            viewModel.hapticsManager.playLightTap()
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentPoints.add(change.position)
                        },
                        onDragEnd = {
                            if (currentPoints.isNotEmpty()) {
                                drawnPaths.add(
                                    DrawnPath(
                                        points = currentPoints.toList(),
                                        color = currentColor,
                                        strokeWidth = strokeWidth,
                                        symmetryMode = symmetryMode
                                    )
                                )
                                currentPoints.clear()
                                viewModel.soundManager.playChime(drawnPaths.size % 8)
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)

                // Draw existing paths
                drawnPaths.forEach { path ->
                    renderSymmetricPath(path.points, path.color, path.strokeWidth, path.symmetryMode, center)
                }

                // Draw current path in progress
                if (currentPoints.isNotEmpty()) {
                    renderSymmetricPath(currentPoints, currentColor, strokeWidth, symmetryMode, center)
                }
            }
        }
    }
}

fun DrawScope.renderSymmetricPath(
    points: List<Offset>,
    color: Color,
    width: Float,
    symmetryMode: Int,
    center: Offset
) {
    if (points.size < 2) return

    val numSymmetries = when (symmetryMode) {
        1 -> 4
        2 -> 8
        else -> 1
    }

    for (sym in 0 until numSymmetries) {
        val angle = sym * (360f / numSymmetries)
        val rad = Math.toRadians(angle.toDouble())
        val cosA = Math.cos(rad).toFloat()
        val sinA = Math.sin(rad).toFloat()

        for (i in 0 until points.size - 1) {
            val p1 = points[i] - center
            val p2 = points[i + 1] - center

            // Rotated points
            val r1 = Offset(p1.x * cosA - p1.y * sinA, p1.x * sinA + p1.y * cosA) + center
            val r2 = Offset(p2.x * cosA - p2.y * sinA, p2.x * sinA + p2.y * cosA) + center

            // Outer soft glow
            drawLine(
                color = color.copy(alpha = 0.35f),
                start = r1,
                end = r2,
                strokeWidth = width * 2.2f,
                cap = StrokeCap.Round
            )
            // Inner crisp line
            drawLine(
                color = color,
                start = r1,
                end = r2,
                strokeWidth = width,
                cap = StrokeCap.Round
            )
        }
    }
}
