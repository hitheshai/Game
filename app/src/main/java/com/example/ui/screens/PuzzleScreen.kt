package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.GlassCard
import com.example.ui.theme.ZenAmber
import com.example.ui.theme.ZenBlue
import com.example.ui.theme.ZenMint
import com.example.ui.theme.ZenPink
import com.example.ui.theme.ZenPurple
import com.example.ui.theme.ZenTeal
import com.example.viewmodel.ZenViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class MemoryCard(
    val id: Int,
    val iconEmoji: String,
    var isFaceUp: Boolean = false,
    var isMatched: Boolean = false
)

@Composable
fun PuzzleScreen(
    viewModel: ZenViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconsPool = listOf("🌸", "🌙", "🍵", "☁️", "🎋", "🕯️", "🐟", "⭐", "🦋", "🍃")
    var numPairs by remember { mutableIntStateOf(6) } // 12 cards
    val cards = remember { mutableStateListOf<MemoryCard>() }
    var moves by remember { mutableIntStateOf(0) }
    var matchedPairsCount by remember { mutableIntStateOf(0) }
    var selectedFirstIndex by remember { mutableStateOf<Int?>(null) }
    var isProcessingMatch by remember { mutableStateOf(false) }
    var showVictoryDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun setupBoard(pairs: Int) {
        numPairs = pairs
        moves = 0
        matchedPairsCount = 0
        selectedFirstIndex = null
        isProcessingMatch = false
        showVictoryDialog = false

        val chosenIcons = iconsPool.shuffled().take(pairs)
        val deck = (chosenIcons + chosenIcons).shuffled().mapIndexed { index, icon ->
            MemoryCard(id = index, iconEmoji = icon)
        }
        cards.clear()
        cards.addAll(deck)
    }

    LaunchedEffect(Unit) {
        setupBoard(6)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("puzzle_screen")
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
                modifier = Modifier.testTag("puzzle_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = "Calm Memory Pairs",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            IconButton(
                onClick = {
                    setupBoard(numPairs)
                    viewModel.soundManager.playSoftClick()
                },
                modifier = Modifier.testTag("puzzle_restart_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Restart",
                    tint = ZenPurple
                )
            }
        }

        // Stats & Difficulty Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (numPairs == 6) ZenPurple.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { setupBoard(6) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        "6 Pairs",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (numPairs == 6) ZenPurple else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (numPairs == 8) ZenPurple.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { setupBoard(8) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        "8 Pairs",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (numPairs == 8) ZenPurple else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Moves: $moves",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Pairs: $matchedPairsCount/$numPairs",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = ZenTeal
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Cards Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (numPairs == 6) 3 else 4),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(cards) { index, card ->
                FlipCardItem(
                    card = card,
                    onClick = {
                        if (isProcessingMatch || card.isFaceUp || card.isMatched) return@FlipCardItem

                        viewModel.soundManager.playBubblePop()
                        viewModel.hapticsManager.playLightTap()

                        cards[index] = card.copy(isFaceUp = true)

                        if (selectedFirstIndex == null) {
                            selectedFirstIndex = index
                        } else {
                            moves++
                            val firstIdx = selectedFirstIndex!!
                            val firstCard = cards[firstIdx]

                            if (firstCard.iconEmoji == card.iconEmoji) {
                                // Match!
                                cards[firstIdx] = firstCard.copy(isMatched = true)
                                cards[index] = card.copy(isMatched = true)
                                matchedPairsCount++
                                selectedFirstIndex = null
                                viewModel.soundManager.playChime(matchedPairsCount % 8)
                                viewModel.hapticsManager.playGentlePulse()

                                if (matchedPairsCount == numPairs) {
                                    showVictoryDialog = true
                                    viewModel.unlockAchievement("ach_memory_master")
                                    viewModel.addXpAndCoins(
                                        xp = 70,
                                        coins = 35,
                                        gameMode = "puzzle_mode",
                                        score = maxOf(10, 100 - moves * 3),
                                        celebrate = true
                                    )
                                }
                            } else {
                                // Mismatch
                                isProcessingMatch = true
                                viewModel.soundManager.playSoftClick()
                                // Flip back after small delay
                                coroutineScope.launch {
                                    delay(850)
                                    cards[firstIdx] = cards[firstIdx].copy(isFaceUp = false)
                                    cards[index] = cards[index].copy(isFaceUp = false)
                                    selectedFirstIndex = null
                                    isProcessingMatch = false
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    // Victory Dialog
    if (showVictoryDialog) {
        Dialog(onDismissRequest = { showVictoryDialog = false }) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "✨ Mindful Harmony!", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You solved the calm puzzle in $moves moves.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { setupBoard(numPairs) },
                            colors = ButtonDefaults.buttonColors(containerColor = ZenPurple),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
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

@Composable
fun FlipCardItem(
    card: MemoryCard,
    onClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (card.isFaceUp || card.isMatched) 180f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "card_flip"
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = !card.isMatched && !card.isFaceUp, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (rotation <= 90f) {
            // Card Back (Zen pattern)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(listOf(ZenPurple, ZenTeal))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "✨", fontSize = 24.sp)
            }
        } else {
            // Card Front
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
                    .background(
                        if (card.isMatched) ZenMint.copy(alpha = 0.25f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = card.iconEmoji,
                    fontSize = 32.sp
                )
            }
        }
    }
}
