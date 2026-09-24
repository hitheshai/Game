package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.ZenBlue
import com.example.ui.theme.ZenMint
import com.example.ui.theme.ZenPink
import com.example.ui.theme.ZenPurple
import com.example.ui.theme.ZenTeal
import com.example.viewmodel.ZenViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.hypot
import kotlin.random.Random

@Composable
fun RelaxScreen(
    viewModel: ZenViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Guided Breath", "Zen Pond", "Soundscape")

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("relax_screen")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("relax_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Relax Sanctuary",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 20.dp,
            containerColor = Color.Transparent,
            contentColor = ZenTeal,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        selectedTab = index
                        viewModel.soundManager.playSoftClick()
                    },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) ZenTeal else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.testTag("relax_tab_$index")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedTab) {
            0 -> GuidedBreathingSection(viewModel)
            1 -> ZenBubblePondSection(viewModel)
            2 -> SoundscapeSection(viewModel)
        }
    }
}

// -------------------------------------------------------------
// 1. Guided Breathing Section
// -------------------------------------------------------------
enum class BreathingPattern(val label: String, val inhale: Int, val hold1: Int, val exhale: Int, val hold2: Int) {
    BOX("Box (4-4-4-4)", 4, 4, 4, 4),
    CALM_478("Calm 4-7-8", 4, 7, 8, 0),
    ZEN_BALANCE("Zen (5-5)", 5, 0, 5, 0)
}

@Composable
fun GuidedBreathingSection(viewModel: ZenViewModel) {
    var pattern by remember { mutableStateOf(BreathingPattern.BOX) }
    var isBreathingActive by remember { mutableStateOf(false) }
    var phaseText by remember { mutableStateOf("Ready to begin") }
    var phaseSecondsRemaining by remember { mutableIntStateOf(0) }
    var completedCycles by remember { mutableIntStateOf(0) }
    val circleScale = remember { Animatable(0.45f) }

    LaunchedEffect(isBreathingActive, pattern) {
        if (!isBreathingActive) {
            circleScale.snapTo(0.45f)
            phaseText = "Tap start to breathe"
            return@LaunchedEffect
        }

        while (isActive && isBreathingActive) {
            // Inhale
            phaseText = "Inhale gently..."
            viewModel.hapticsManager.playBreathePulse()
            viewModel.soundManager.playChime(1)
            for (sec in pattern.inhale downTo 1) {
                phaseSecondsRemaining = sec
                circleScale.animateTo(
                    targetValue = 0.95f,
                    animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
                )
            }

            // Hold 1
            if (pattern.hold1 > 0) {
                phaseText = "Hold softly..."
                for (sec in pattern.hold1 downTo 1) {
                    phaseSecondsRemaining = sec
                    delay(1000)
                }
            }

            // Exhale
            phaseText = "Exhale slowly..."
            viewModel.hapticsManager.playBreathePulse()
            viewModel.soundManager.playChime(4)
            for (sec in pattern.exhale downTo 1) {
                phaseSecondsRemaining = sec
                circleScale.animateTo(
                    targetValue = 0.45f,
                    animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
                )
            }

            // Hold 2
            if (pattern.hold2 > 0) {
                phaseText = "Rest calmly..."
                for (sec in pattern.hold2 downTo 1) {
                    phaseSecondsRemaining = sec
                    delay(1000)
                }
            }

            completedCycles++
            if (completedCycles % 2 == 0) {
                viewModel.addXpAndCoins(xp = 25, coins = 10)
                viewModel.unlockAchievement("ach_first_breath")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Pattern Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BreathingPattern.values().forEach { p ->
                val selected = pattern == p
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selected) ZenTeal.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            if (!isBreathingActive) pattern = p
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = p.label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (selected) ZenTeal else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Breathing Circle Animation Canvas
        Box(
            modifier = Modifier
                .size(280.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val baseRadius = (size.width / 2) * circleScale.value

                // Outer aura rings
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ZenTeal.copy(alpha = 0.35f), Color.Transparent),
                        center = center,
                        radius = baseRadius * 1.35f
                    ),
                    center = center,
                    radius = baseRadius * 1.35f
                )

                // Middle breathing petal
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ZenMint.copy(alpha = 0.6f), ZenTeal.copy(alpha = 0.2f)),
                        center = center,
                        radius = baseRadius
                    ),
                    center = center,
                    radius = baseRadius
                )

                // Core inner circle
                drawCircle(
                    color = Color.White.copy(alpha = 0.85f),
                    center = center,
                    radius = baseRadius * 0.45f
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = phaseText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ZenTeal,
                    textAlign = TextAlign.Center
                )
                if (isBreathingActive) {
                    Text(
                        text = "$phaseSecondsRemaining",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Cycles: $completedCycles",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Bottom Controls
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    isBreathingActive = !isBreathingActive
                    viewModel.hapticsManager.playLightTap()
                    if (isBreathingActive) {
                        viewModel.soundManager.playSingingBowl()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBreathingActive) ZenPink else ZenTeal
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(54.dp)
                    .testTag("breathing_toggle_button")
            ) {
                Icon(
                    imageVector = if (isBreathingActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBreathingActive) "Pause Meditation" else "Begin Gentle Breath",
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "💡 Follow the circle as it expands and shrinks with your natural lungs",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// -------------------------------------------------------------
// 2. Zen Pond Bubble Pop Section
// -------------------------------------------------------------
data class PondBubble(
    val id: Long,
    var x: Float,
    var y: Float,
    val radius: Float,
    val speed: Float,
    val color: Color
)

data class BubbleSparkle(
    val x: Float,
    val y: Float,
    val color: Color,
    val lifetime: Long = System.currentTimeMillis()
)

@Composable
fun ZenBubblePondSection(viewModel: ZenViewModel) {
    var poppedCount by remember { mutableIntStateOf(0) }
    val bubbles = remember { mutableStateListOf<PondBubble>() }
    val sparkles = remember { mutableStateListOf<BubbleSparkle>() }

    val colors = listOf(
        Color(0xCC74B9FF),
        Color(0xCCA29BFE),
        Color(0xCC81ECEC),
        Color(0xCCFFB8D2),
        Color(0xCC55EFC4)
    )

    // Animation ticker to spawn and move bubbles
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(32)
            // Spawn new bubbles if count is low
            if (bubbles.size < 10) {
                bubbles.add(
                    PondBubble(
                        id = System.nanoTime(),
                        x = Random.nextFloat(),
                        y = 1.1f, // start below bottom
                        radius = Random.nextFloat() * 32f + 24f,
                        speed = Random.nextFloat() * 0.003f + 0.002f,
                        color = colors[Random.nextInt(colors.size)]
                    )
                )
            }

            // Move bubbles upward
            val iterator = bubbles.iterator()
            while (iterator.hasNext()) {
                val b = iterator.next()
                b.y -= b.speed
                if (b.y < -0.1f) {
                    iterator.remove()
                }
            }

            // Expire sparkles
            val now = System.currentTimeMillis()
            sparkles.removeAll { now - it.lifetime > 600 }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🫧 Floating Zen Pond",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Popped: $poppedCount bubbles",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = ZenTeal
            )
        }
        Text(
            text = "Gently tap the floating bubbles to release gentle chimes & sparkles",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x2074B9FF),
                            Color(0x3500CEC9),
                            Color(0x206C5CE7)
                        )
                    )
                )
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()

                        var hitBubble: PondBubble? = null
                        for (bubble in bubbles) {
                            val bx = bubble.x * w
                            val by = bubble.y * h
                            val dist = hypot((tapOffset.x - bx).toDouble(), (tapOffset.y - by).toDouble()).toFloat()
                            if (dist <= bubble.radius * 1.5f) {
                                hitBubble = bubble
                                break
                            }
                        }

                        hitBubble?.let { b ->
                            bubbles.remove(b)
                            poppedCount++
                            // Sparkles
                            repeat(8) {
                                sparkles.add(BubbleSparkle(b.x * w, b.y * h, b.color))
                            }
                            viewModel.soundManager.playBubblePop()
                            viewModel.soundManager.playChime(poppedCount % 8)
                            viewModel.hapticsManager.playLightTap()

                            if (poppedCount == 30) {
                                viewModel.unlockAchievement("ach_bubble_popper")
                                viewModel.addXpAndCoins(xp = 40, coins = 20, celebrate = true)
                            } else if (poppedCount % 10 == 0) {
                                viewModel.addXpAndCoins(xp = 15, coins = 5)
                            }
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw bubbles
                bubbles.forEach { b ->
                    val cx = b.x * w
                    val cy = b.y * h
                    // Body
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.7f), b.color),
                            center = Offset(cx - b.radius * 0.3f, cy - b.radius * 0.3f),
                            radius = b.radius
                        ),
                        center = Offset(cx, cy),
                        radius = b.radius
                    )
                    // Highlight gloss
                    drawCircle(
                        color = Color.White.copy(alpha = 0.85f),
                        center = Offset(cx - b.radius * 0.35f, cy - b.radius * 0.35f),
                        radius = b.radius * 0.22f
                    )
                }

                // Draw sparkle remnants
                sparkles.forEach { sp ->
                    drawCircle(
                        color = sp.color,
                        center = Offset(sp.x, sp.y),
                        radius = 4.dp.toPx()
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. Sound Sanctuary Section
// -------------------------------------------------------------
@Composable
fun SoundscapeSection(viewModel: ZenViewModel) {
    val soundManager = viewModel.soundManager
    var activeAmbient by remember { mutableStateOf(soundManager.getActiveAmbient()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Nature & Calming Frequencies",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Procedurally generated real-time soundscapes to quiet the mind.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SoundscapeCard(
            title = "Gentle Summer Rain",
            description = "Soft patter of soothing rain drops to wash away tension",
            iconEmoji = "🌧️",
            isPlaying = activeAmbient == "rain",
            onToggle = {
                soundManager.toggleAmbient("rain")
                activeAmbient = soundManager.getActiveAmbient()
            }
        )

        SoundscapeCard(
            title = "Ocean Wave Swell",
            description = "Slow rhythmic tide of the peaceful sea breeze",
            iconEmoji = "🌊",
            isPlaying = activeAmbient == "ocean",
            onToggle = {
                soundManager.toggleAmbient("ocean")
                activeAmbient = soundManager.getActiveAmbient()
            }
        )

        SoundscapeCard(
            title = "Zen Singing Bowl (Chime)",
            description = "Harmonic resonance bell to center your breath",
            iconEmoji = "🔔",
            isPlaying = false,
            onToggle = {
                soundManager.playSingingBowl()
                viewModel.hapticsManager.playGentlePulse()
            }
        )
    }
}

@Composable
fun SoundscapeCard(
    title: String,
    description: String,
    iconEmoji: String,
    isPlaying: Boolean,
    onToggle: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Text(text = iconEmoji, fontSize = 32.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) ZenTeal else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = if (isPlaying) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
