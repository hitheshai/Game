package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class ConfettiPiece(
    val initialX: Float,
    val initialY: Float,
    val velocityX: Float,
    val velocityY: Float,
    val rotationSpeed: Float,
    val color: Color,
    val size: Float,
    val shapeType: Int // 0: rect, 1: circle, 2: star/cross
)

@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier,
    trigger: Boolean,
    onComplete: () -> Unit = {}
) {
    if (!trigger) return

    val progress = remember { Animatable(0f) }
    val pieces = remember {
        val colors = listOf(
            Color(0xFFFF2A85),
            Color(0xFF00CEC9),
            Color(0xFFFFA502),
            Color(0xFF6C5CE7),
            Color(0xFF00B894),
            Color(0xFFFF7675),
            Color(0xFFFDCB6E)
        )
        List(60) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 700f + 250f
            ConfettiPiece(
                initialX = 0.5f,
                initialY = 0.45f,
                velocityX = cos(angle) * speed,
                velocityY = sin(angle) * speed - 200f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                color = colors[it % colors.size],
                size = Random.nextFloat() * 14f + 8f,
                shapeType = Random.nextInt(3)
            )
        }
    }

    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2400, easing = LinearEasing)
        )
        onComplete()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val t = progress.value
        val alpha = (1f - t * 0.95f).coerceIn(0f, 1f)

        pieces.forEach { piece ->
            val x = piece.initialX * size.width + piece.velocityX * t
            // Gravity effect
            val y = piece.initialY * size.height + piece.velocityY * t + 0.5f * 980f * t * t
            val rotation = piece.rotationSpeed * t

            rotate(degrees = rotation, pivot = Offset(x, y)) {
                when (piece.shapeType) {
                    0 -> {
                        drawRect(
                            color = piece.color.copy(alpha = alpha),
                            topLeft = Offset(x - piece.size / 2, y - piece.size / 2),
                            size = Size(piece.size, piece.size * 0.6f)
                        )
                    }
                    1 -> {
                        drawCircle(
                            color = piece.color.copy(alpha = alpha),
                            radius = piece.size / 2,
                            center = Offset(x, y)
                        )
                    }
                    else -> {
                        // Star / cross shape
                        drawLine(
                            color = piece.color.copy(alpha = alpha),
                            start = Offset(x - piece.size / 2, y),
                            end = Offset(x + piece.size / 2, y),
                            strokeWidth = 3f
                        )
                        drawLine(
                            color = piece.color.copy(alpha = alpha),
                            start = Offset(x, y - piece.size / 2),
                            end = Offset(x, y + piece.size / 2),
                            strokeWidth = 3f
                        )
                    }
                }
            }
        }
    }
}
