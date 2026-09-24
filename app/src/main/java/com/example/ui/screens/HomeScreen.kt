package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.MoodCheckInDialog
import com.example.ui.theme.ZenAmber
import com.example.ui.theme.ZenBlue
import com.example.ui.theme.ZenCoral
import com.example.ui.theme.ZenMint
import com.example.ui.theme.ZenPink
import com.example.ui.theme.ZenPurple
import com.example.ui.theme.ZenTeal
import com.example.viewmodel.ZenViewModel

@Composable
fun HomeScreen(
    viewModel: ZenViewModel,
    onNavigateToRelax: () -> Unit,
    onNavigateToQuickChallenge: () -> Unit,
    onNavigateToPuzzle: () -> Unit,
    onNavigateToCreative: () -> Unit,
    onNavigateToReaction: () -> Unit,
    onNavigateToDaily: () -> Unit,
    onNavigateToMultiplayer: () -> Unit,
    onNavigateToShopProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val recentMoods by viewModel.recentMoods.collectAsState()
    var showMoodCheckIn by remember { mutableStateOf(false) }

    val currentAvatar = viewModel.avatarCatalog.find { it.id == userProfile?.selectedAvatarId }
        ?: viewModel.avatarCatalog.first()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_content"),
        contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
    ) {
        // Top Bar: Avatar, Level, Coins & Settings
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onNavigateToShopProfile() }
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(ZenPurple, ZenPink))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = currentAvatar.emoji, fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = currentAvatar.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Lvl ${userProfile?.level ?: 1}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = ZenPurple
                            )
                            Text(
                                text = " • ${(userProfile?.xp ?: 0)}/${userProfile?.xpToNextLevel ?: 100} XP",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Coin counter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(ZenAmber.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .clickable { onNavigateToShopProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🪙", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${userProfile?.coins ?: 0}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = ZenAmber
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onNavigateToShopProfile,
                        modifier = Modifier.testTag("shop_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "Shop & Avatars",
                            tint = ZenPurple
                        )
                    }

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // XP Progress Bar
        item {
            userProfile?.let { user ->
                val progress = (user.xp.toFloat() / user.xpToNextLevel.toFloat()).coerceIn(0f, 1f)
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = ZenPurple,
                        trackColor = ZenPurple.copy(alpha = 0.15f)
                    )
                }
            }
        }

        // Primary Hero Card: "Play Now" & Quick Relax
        item {
            Spacer(modifier = Modifier.height(12.dp))
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(28.dp),
                elevation = 6.dp,
                testTag = "hero_play_card"
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    ZenPurple.copy(alpha = 0.9f),
                                    ZenBlue.copy(alpha = 0.85f),
                                    ZenTeal.copy(alpha = 0.75f)
                                )
                            )
                        )
                        .padding(22.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Ready to Unwind?",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 24.sp
                                    ),
                                    color = Color.White
                                )
                                Text(
                                    text = "Release tension with gentle fun & peaceful vibes",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            Text(text = "🧘", fontSize = 42.sp)
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = onNavigateToQuickChallenge,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("play_now_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = ZenPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Play Now",
                                    color = ZenPurple,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }

                            Button(
                                onClick = onNavigateToRelax,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White.copy(alpha = 0.25f)
                                ),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("quick_relax_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Spa,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Quick Relax",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Daily Challenge & Mood Check-In Row
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Daily Challenge Card
                GlassCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(22.dp),
                    onClick = onNavigateToDaily,
                    testTag = "daily_challenge_card"
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "⭐ Daily Quest", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = ZenAmber)
                            if (settings?.dailyGoalCompleted == true) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Done", tint = ZenMint, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (settings?.dailyGoalCompleted == true) "Completed today!" else "1-min calm breath & 20 pops",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (settings?.dailyGoalCompleted == true) "Streak: 1 Day 🔥" else "+120 XP • 60 🪙",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (settings?.dailyGoalCompleted == true) ZenMint else ZenPurple
                        )
                    }
                }

                // Mood Tracker Card
                GlassCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(22.dp),
                    onClick = { showMoodCheckIn = true },
                    testTag = "mood_tracker_card"
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "💭 Mood Check-in",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = ZenTeal
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val latestMood = recentMoods.firstOrNull()
                        Text(
                            text = if (latestMood != null) "Latest: ${latestMood.moodName}" else "How is your heart today?",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap to check in ✨",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ZenTeal
                        )
                    }
                }
            }
        }

        // Section Title: Game Modes
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Game Modes",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Playful & Stress-Free",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Grid of Game Modes
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    GameModeCard(
                        title = "Relax Mode",
                        subtitle = "Breathing, bubbles & soundscape",
                        icon = Icons.Default.SelfImprovement,
                        accentColor = ZenTeal,
                        emoji = "🌊",
                        onClick = onNavigateToRelax,
                        testTag = "mode_relax",
                        modifier = Modifier.weight(1f)
                    )
                    GameModeCard(
                        title = "Quick Challenge",
                        subtitle = "30-45s bubble stress pop",
                        icon = Icons.Default.Speed,
                        accentColor = ZenCoral,
                        emoji = "🫧",
                        onClick = onNavigateToQuickChallenge,
                        testTag = "mode_quick_challenge",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    GameModeCard(
                        title = "Puzzle Mode",
                        subtitle = "Calm memory card match",
                        icon = Icons.Default.Extension,
                        accentColor = ZenPurple,
                        emoji = "🧩",
                        onClick = onNavigateToPuzzle,
                        testTag = "mode_puzzle",
                        modifier = Modifier.weight(1f)
                    )
                    GameModeCard(
                        title = "Creative Mode",
                        subtitle = "Neon sand & kaleidoscope",
                        icon = Icons.Default.Brush,
                        accentColor = ZenPink,
                        emoji = "🎨",
                        onClick = onNavigateToCreative,
                        testTag = "mode_creative",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    GameModeCard(
                        title = "Reaction Mode",
                        subtitle = "Harmonic rhythm ripples",
                        icon = Icons.Default.MusicNote,
                        accentColor = ZenBlue,
                        emoji = "🎵",
                        onClick = onNavigateToReaction,
                        testTag = "mode_reaction",
                        modifier = Modifier.weight(1f)
                    )
                    GameModeCard(
                        title = "Co-op / Friends",
                        subtitle = "Harmony tap & warm cheers",
                        icon = Icons.Default.Group,
                        accentColor = ZenMint,
                        emoji = "🤝",
                        onClick = onNavigateToMultiplayer,
                        testTag = "mode_multiplayer",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    if (showMoodCheckIn) {
        MoodCheckInDialog(
            phase = "before",
            onDismiss = { showMoodCheckIn = false },
            onSubmit = { rating, name, note ->
                viewModel.saveMood(rating, name, "before", note)
                showMoodCheckIn = false
            }
        )
    }
}

@Composable
fun GameModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    emoji: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = 3.dp,
        onClick = onClick,
        testTag = testTag
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(text = emoji, fontSize = 20.sp)
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    fontSize = 11.sp
                )
            }
        }
    }
}
