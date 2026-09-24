package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.window.Dialog
import com.example.ui.components.GlassCard
import com.example.ui.theme.ZenAmber
import com.example.ui.theme.ZenBlue
import com.example.ui.theme.ZenCoral
import com.example.ui.theme.ZenMint
import com.example.ui.theme.ZenPink
import com.example.ui.theme.ZenPurple
import com.example.ui.theme.ZenTeal
import com.example.viewmodel.ZenViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.hypot
import kotlin.random.Random

data class ChallengeBubble(
    val id: Long,
    var x: Float,
    var y: Float,
    val radius: Float,
    val speedY: Float,
    val speedX: Float,
    val colorIndex: Int,
    val label: String
)

@Composable
fun QuickChallengeScreen(
    viewModel: ZenViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bubbleTypes = listOf(
        Pair(ZenPink, "🌸 Joy"),
        Pair(ZenTeal, "🌊 Peace"),
        Pair(ZenAmber, "✨ Hope"),
        Pair(ZenPurple, "💜 Calm"),
        Pair(ZenMint, "🍀 Bloom")
    )

    var gameState by remember { mutableStateOf("ready") } // "ready", "playing", "finished"
    var secondsLeft by remember { mutableIntStateOf(45) }
    var score by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(0) }
    var maxCombo by remember { mutableIntStateOf(0) }
    var targetColorIndex by remember { mutableIntStateOf(0) }
    val bubbles = remember { mutableStateListOf<ChallengeBubble>() }

    // Change target color every 12 seconds
    LaunchedEffect(gameState) {
        if (gameState == "playing") {
            while (isActive && secondsLeft > 0) {
                delay(1000)
                secondsLeft--
                if (secondsLeft % 10 == 0) {
                    targetColorIndex = (targetColorIndex + 1) % bubbleTypes.size
                    viewModel.soundManager.playChime(4)
                }
            }
            if (secondsLeft <= 0) {
                gameState = "finished"
                val earnedXp = 50 + score * 2
                val earnedCoins = 20 + (score / 3)
                viewModel.addXpAndCoins(
                    xp = earnedXp,
                    coins = earnedCoins,
                    gameMode = "quick_challenge",
                    score = score,
                    celebrate = true
                )
            }
        }
    }

    // Spawn and physics ticker
    LaunchedEffect(gameState) {
        if (gameState == "playing") {
            while (isActive) {
                delay(30)
                if (bubbles.size < 8) {
                    val cIdx = Random.nextInt(bubbleTypes.size)
                    bubbles.add(
                        ChallengeBubble(
                            id = System.nanoTime(),
                            x = Random.nextFloat() * 0.85f + 0.07f,
                            y = 1.05f,
                            radius = Random.nextFloat() * 26f + 32f,
                            speedY = Random.nextFloat() * 0.005f + 0.0035f,
                            speedX = (Random.nextFloat() - 0.5f) * 0.002f,
                            colorIndex = cIdx,
                            label = bubbleTypes[cIdx].second
                        )
                    )
                }

                val it = bubbles.iterator()
                while (it.hasNext()) {
                    val b = it.next()
                    b.y -= b.speedY
                    b.x += b.speedX
                    if (b.y < -0.1f) {
                        it.remove()
                    }
                }
            }
        }
    }

    fun startGame() {
        secondsLeft = 45
        score = 0
        combo = 0
        maxCombo = 0
        targetColorIndex = Random.nextInt(bubbleTypes.size)
        bubbles.clear()
        gameState = "playing"
        viewModel.soundManager.playSingingBowl()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("quick_challenge_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("challenge_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Glow Pop Challenge",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            // Time Left
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(ZenCoral.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "⏱️ ${secondsLeft}s",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = ZenCoral
                )
            }
        }

        // Target Indicator & Score
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val target = bubbleTypes[targetColorIndex]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(target.first.copy(alpha = 0.25f))
                    .border(1.5.dp, target.first, RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Target: ${target.second}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = target.first
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Score: $score",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ZenPurple
                )
                if (combo > 1) {
                    Text(
                        text = "🔥 ${combo}x Combo!",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ZenAmber
                    )
                }
            }
        }

        // Play Area Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0x221E1B4B), Color(0x350F172A))
                    )
                )
                .pointerInput(gameState) {
                    if (gameState == "playing") {
                        detectTapGestures { tapOffset ->
                            val w = size.width.toFloat()
                            val h = size.height.toFloat()

                            var hit: ChallengeBubble? = null
                            for (b in bubbles) {
                                val bx = b.x * w
                                val by = b.y * h
                                val d = hypot((tapOffset.x - bx).toDouble(), (tapOffset.y - by).toDouble()).toFloat()
                                if (d <= b.radius * 1.4f) {
                                    hit = b
                                    break
                                }
                            }

                            hit?.let { b ->
                                bubbles.remove(b)
                                viewModel.soundManager.playBubblePop()
                                viewModel.hapticsManager.playLightTap()

                                if (b.colorIndex == targetColorIndex) {
                                    // Match!
                                    combo++
                                    if (combo > maxCombo) maxCombo = combo
                                    score += 10 + combo * 2
                                    viewModel.soundManager.playChime(combo % 8)
                                } else {
                                    // Friendly pop: still adds basic points without penalty
                                    combo = 1
                                    score += 5
                                }
                            }
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                bubbles.forEach { b ->
                    val cx = b.x * w
                    val cy = b.y * h
                    val col = bubbleTypes[b.colorIndex].first
                    // Bubble glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.8f), col),
                            center = Offset(cx - b.radius * 0.3f, cy - b.radius * 0.3f),
                            radius = b.radius
                        ),
                        center = Offset(cx, cy),
                        radius = b.radius
                    )
                    // Inner reflection
                    drawCircle(
                        color = Color.White.copy(alpha = 0.85f),
                        center = Offset(cx - b.radius * 0.3f, cy - b.radius * 0.3f),
                        radius = b.radius * 0.25f
                    )
                }
            }

            // Start Overlay if ready
            if (gameState == "ready") {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "45-Second Quick Relax 🫧",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Pop the target emotion bubbles to release tension and build happy combos!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { startGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = ZenPink),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("start_quick_challenge_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Start Challenge", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }

    // Results Dialog
    if (gameState == "finished") {
        Dialog(onDismissRequest = { gameState = "ready" }) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎉 Beautiful Work!",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You popped away your stress with mindful ease.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Score", style = MaterialTheme.typography.labelSmall)
                            Text("$score", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = ZenPurple)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Best Combo", style = MaterialTheme.typography.labelSmall)
                            Text("${maxCombo}x", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = ZenAmber)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { startGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = ZenPurple),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("replay_challenge_button"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Play Again")
                        }
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Done", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
