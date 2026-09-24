package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    elevation: Dp = 4.dp,
    onClick: (() -> Unit)? = null,
    testTag: String? = null,
    backgroundColor: Color? = null,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val defaultBg = if (isDark) {
        Color(0xCC1E293B)
    } else {
        Color(0xEBFFFFFF)
    }
    val borderColor = if (isDark) {
        Color(0x33FFFFFF)
    } else {
        Color(0x66E2E8F0)
    }

    val cardModifier = modifier
        .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = if (isDark) Color(0x66000000) else Color(0x2264748B),
            spotColor = if (isDark) Color(0x88000000) else Color(0x3394A3B8)
        )
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    (backgroundColor ?: defaultBg),
                    (backgroundColor ?: defaultBg).copy(alpha = 0.92f)
                )
            )
        )
        .border(
            width = 1.2.dp,
            brush = Brush.verticalGradient(
                colors = listOf(
                    borderColor.copy(alpha = 0.8f),
                    borderColor.copy(alpha = 0.2f)
                )
            ),
            shape = shape
        )
        .then(
            if (onClick != null) {
                Modifier.clickable(onClick = onClick)
            } else Modifier
        )

    Box(modifier = cardModifier) {
        content()
    }
}
