package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class AmbientParticle(
    val initialX: Float,
    val initialY: Float,
    val radius: Float,
    val speed: Float,
    val angleOffset: Float,
    val color: Color
)

@Composable
fun AnimatedBackground(
    modifier: Modifier = Modifier,
    reducedMotion: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val baseBg = MaterialTheme.colorScheme.background

    val particles = remember {
        val colors = if (isDark) {
            listOf(
                Color(0x336C5CE7),
                Color(0x2800CEC9),
                Color(0x28FD79A8),
                Color(0x2074B9FF)
            )
        } else {
            listOf(
                Color(0x22A29BFE),
                Color(0x2081ECEC),
                Color(0x20FFB8D2),
                Color(0x1CFFA502)
            )
        }

        List(12) {
            AmbientParticle(
                initialX = Random.nextFloat(),
                initialY = Random.nextFloat(),
                radius = Random.nextFloat() * 100f + 60f,
                speed = Random.nextFloat() * 0.4f + 0.3f,
                angleOffset = Random.nextFloat() * 6.28f,
                color = colors[it % colors.size]
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bg_progress"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Base gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = if (isDark) {
                        listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF0F172A))
                    } else {
                        listOf(Color(0xFFF8FAFC), Color(0xFFEEF2FF), Color(0xFFFAF5FF))
                    }
                )
            )

            // Draw floating ambient glow orbs
            particles.forEach { p ->
                val progress = if (reducedMotion) 0f else animProgress * p.speed + p.angleOffset
                val x = (p.initialX + 0.12f * sin(progress)) * width
                val y = (p.initialY + 0.10f * cos(progress)) * height

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(p.color, p.color.copy(alpha = 0f)),
                        center = Offset(x, y),
                        radius = p.radius * density
                    ),
                    center = Offset(x, y),
                    radius = p.radius * density
                )
            }
        }

        content()
    }
}
