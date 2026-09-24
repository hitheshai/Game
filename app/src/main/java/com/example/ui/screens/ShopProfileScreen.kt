package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
fun ShopProfileScreen(
    viewModel: ZenViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Avatars", "Themes", "Badges & Stats")

    val unlockedAvatars = userProfile?.unlockedAvatars?.split(",")?.toSet() ?: setOf("avatar_cloud")
    val unlockedThemes = userProfile?.unlockedThemes?.split(",")?.toSet() ?: setOf("theme_serene")

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("shop_profile_screen")
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
                modifier = Modifier.testTag("shop_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = "Wardrobe & Growth",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            // Coins Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(ZenAmber.copy(alpha = 0.2f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
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
        }

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 20.dp,
            containerColor = Color.Transparent,
            contentColor = ZenPurple,
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
                            color = if (selectedTab == index) ZenPurple else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedTab) {
            0 -> {
                // Avatars Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(viewModel.avatarCatalog) { avatar ->
                        val isUnlocked = unlockedAvatars.contains(avatar.id)
                        val isSelected = userProfile?.selectedAvatarId == avatar.id

                        GlassCard(
                            modifier = Modifier.height(180.dp),
                            shape = RoundedCornerShape(22.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) ZenPurple.copy(alpha = 0.25f)
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = if (isSelected) ZenPurple else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = avatar.emoji, fontSize = 32.sp)
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = avatar.name,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = avatar.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        fontSize = 10.sp
                                    )
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(ZenMint.copy(alpha = 0.2f))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Equipped ✓", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = ZenMint)
                                    }
                                } else if (isUnlocked) {
                                    Button(
                                        onClick = { viewModel.selectAvatar(avatar.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = ZenPurple),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Equip", fontSize = 12.sp)
                                    }
                                } else {
                                    val canAfford = (userProfile?.coins ?: 0) >= avatar.unlockCost
                                    Button(
                                        onClick = { viewModel.selectAvatar(avatar.id) },
                                        enabled = canAfford,
                                        colors = ButtonDefaults.buttonColors(containerColor = ZenAmber),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Unlock (${avatar.unlockCost} 🪙)", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Themes List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(viewModel.themeCatalog) { theme ->
                        val isUnlocked = unlockedThemes.contains(theme.id)
                        val isSelected = userProfile?.selectedThemeId == theme.id

                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(theme.primaryHex), Color(theme.secondaryHex))
                                                )
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = theme.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isUnlocked) "Available" else "${theme.unlockCost} Coins",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(ZenMint.copy(alpha = 0.2f))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Applied ✓", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = ZenMint)
                                    }
                                } else if (isUnlocked) {
                                    Button(
                                        onClick = { viewModel.selectTheme(theme.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = ZenPurple),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text("Apply", fontSize = 12.sp)
                                    }
                                } else {
                                    val canAfford = (userProfile?.coins ?: 0) >= theme.unlockCost
                                    Button(
                                        onClick = { viewModel.selectTheme(theme.id) },
                                        enabled = canAfford,
                                        colors = ButtonDefaults.buttonColors(containerColor = ZenAmber),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text("Unlock (${theme.unlockCost} 🪙)", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Badges & Stats
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Overall Stats Card
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "Your Serenity Journey 🧘",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Level", style = MaterialTheme.typography.labelSmall)
                                        Text("${userProfile?.level ?: 1}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = ZenPurple)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Games", style = MaterialTheme.typography.labelSmall)
                                        Text("${userProfile?.gamesPlayed ?: 0}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = ZenTeal)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Streak", style = MaterialTheme.typography.labelSmall)
                                        Text("${userProfile?.streakDays ?: 1} Days", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = ZenAmber)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Milestone Badges",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    items(achievements) { ach ->
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (ach.isUnlocked) ZenAmber.copy(alpha = 0.25f)
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = if (ach.isUnlocked) ach.iconEmoji else "🔒", fontSize = 22.sp)
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ach.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (ach.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = ach.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (ach.isUnlocked) {
                                    Text("✓ Unlocked", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = ZenMint)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
