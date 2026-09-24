package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.ZenAmber
import com.example.ui.theme.ZenBlue
import com.example.ui.theme.ZenMint
import com.example.ui.theme.ZenPink
import com.example.ui.theme.ZenPurple
import com.example.ui.theme.ZenTeal
import com.example.viewmodel.ZenViewModel

@Composable
fun MultiplayerScreen(
    viewModel: ZenViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Co-op Harmony Tap", "Kindness Cards")

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("multiplayer_screen")
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
                modifier = Modifier.testTag("multiplayer_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Friends & Harmony",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 20.dp,
            containerColor = Color.Transparent,
            contentColor = ZenMint,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) ZenMint else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedTab) {
            0 -> CoopHarmonyTapSection(viewModel)
            1 -> KindnessCardShareSection(viewModel)
        }
    }
}

@Composable
fun CoopHarmonyTapSection(viewModel: ZenViewModel) {
    var p1Tapped by remember { mutableStateOf(false) }
    var p2Tapped by remember { mutableStateOf(false) }
    var harmonyPoints by remember { mutableIntStateOf(0) }
    val targetPoints = 10

    fun checkCoopSync() {
        if (p1Tapped && p2Tapped) {
            harmonyPoints++
            viewModel.soundManager.playChime(harmonyPoints % 8)
            viewModel.hapticsManager.playSuccessPattern()
            p1Tapped = false
            p2Tapped = false

            if (harmonyPoints >= targetPoints) {
                viewModel.unlockAchievement("ach_friendly_vibe")
                viewModel.addXpAndCoins(xp = 60, coins = 30, celebrate = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Co-op Harmony Resonance 🤝",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Both friends tap together on the screen to create shared chords!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Harmony Meter
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Harmony: $harmonyPoints / $targetPoints", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = ZenMint)
            Spacer(modifier = Modifier.width(12.dp))
            LinearProgressIndicator(
                progress = { (harmonyPoints.toFloat() / targetPoints).coerceIn(0f, 1f) },
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(CircleShape),
                color = ZenMint,
                trackColor = ZenMint.copy(alpha = 0.2f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dual Player Zones
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Player 1 Pad
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .then(
                        if (p1Tapped) Modifier.background(ZenTeal.copy(alpha = 0.5f))
                        else Modifier.background(Brush.verticalGradient(listOf(ZenTeal.copy(alpha = 0.25f), ZenBlue.copy(alpha = 0.15f))))
                    )
                    .clickable {
                        p1Tapped = !p1Tapped
                        viewModel.soundManager.playBubblePop()
                        viewModel.hapticsManager.playLightTap()
                        checkCoopSync()
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = if (p1Tapped) "✨ Ready!" else "🌸 Player 1 (Tap to Sync)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ZenTeal)
                    Text(text = "Tap your pad", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Player 2 Pad
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .then(
                        if (p2Tapped) Modifier.background(ZenPink.copy(alpha = 0.5f))
                        else Modifier.background(Brush.verticalGradient(listOf(ZenPink.copy(alpha = 0.25f), ZenPurple.copy(alpha = 0.15f))))
                    )
                    .clickable {
                        p2Tapped = !p2Tapped
                        viewModel.soundManager.playBubblePop()
                        viewModel.hapticsManager.playLightTap()
                        checkCoopSync()
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = if (p2Tapped) "✨ Ready!" else "🍀 Player 2 (Tap to Sync)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ZenPink)
                    Text(text = "Tap your pad", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun KindnessCardShareSection(viewModel: ZenViewModel) {
    val context = LocalContext.current
    val quotes = listOf(
        "\"Take a gentle breath. You are doing so much better than you realize! 🌸\"",
        "\"Sending you peaceful calm and sunshine today. Never forget how loved you are! ✨\"",
        "\"Slow down and notice the little wonders. May your day be light and soft. 🍃\"",
        "\"You bring warm light and joy to those around you. Thank you for being you! 💜\""
    )
    var selectedQuote by remember { mutableStateOf(quotes[0]) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Share a Mindful Cheer 💌",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Spread kindness to a friend, child, or family member without any pressure.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Card Preview
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            elevation = 4.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(ZenPurple.copy(alpha = 0.2f), ZenPink.copy(alpha = 0.2f))
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🕊️", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = selectedQuote,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selection row
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            quotes.forEach { quote ->
                val isSelected = selectedQuote == quote
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) ZenPurple.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { selectedQuote = quote }
                        .padding(12.dp)
                ) {
                    Text(
                        text = quote,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) ZenPurple else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "$selectedQuote\n\n— Sent with peace via ZenPlay")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Send Mindful Kindness"))
                viewModel.unlockAchievement("ach_friendly_vibe")
                viewModel.addXpAndCoins(xp = 30, coins = 15)
            },
            colors = ButtonDefaults.buttonColors(containerColor = ZenPurple),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("share_kindness_button")
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Send to Friend (+30 XP)", fontWeight = FontWeight.Bold)
        }
    }
}
