package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AnimatedBackground
import com.example.ui.components.BreakReminderDialog
import com.example.ui.components.ConfettiEffect
import com.example.ui.screens.CreativeScreen
import com.example.ui.screens.DailyChallengeScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MultiplayerScreen
import com.example.ui.screens.PuzzleScreen
import com.example.ui.screens.QuickChallengeScreen
import com.example.ui.screens.ReactionScreen
import com.example.ui.screens.RelaxScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ShopProfileScreen
import com.example.ui.theme.ZenAmber
import com.example.ui.theme.ZenPlayTheme
import com.example.ui.theme.ZenPurple
import com.example.viewmodel.ZenViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: ZenViewModel = viewModel()
            val settings by viewModel.settings.collectAsState()
            val reducedMotion = settings?.reducedMotion ?: false

            ZenPlayTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("app_root_scaffold")
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedBackground(reducedMotion = reducedMotion) {
                            ZenAppNavHost(viewModel = viewModel)
                        }

                        // Global Confetti Celebration Overlay
                        val confettiActive by viewModel.confettiTrigger.collectAsState()
                        ConfettiEffect(
                            trigger = confettiActive,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Break Reminder Modal
                        val showBreakReminder by viewModel.showBreakReminder.collectAsState()
                        if (showBreakReminder) {
                            BreakReminderDialog(
                                minutesPlayed = settings?.breakReminderMinutes ?: 15,
                                onTakeBreak = { viewModel.dismissBreakReminder() },
                                onContinue = { viewModel.dismissBreakReminder() }
                            )
                        }

                        // Level-Up Celebration Modal
                        val levelUpLevel by viewModel.levelUpEvent.collectAsState()
                        levelUpLevel?.let { newLvl ->
                            Dialog(onDismissRequest = { viewModel.dismissLevelUp() }) {
                                Card(
                                    shape = RoundedCornerShape(28.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("🌟 LEVEL UP!", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = ZenAmber)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "You reached Level $newLvl in your mindful journey!",
                                            style = MaterialTheme.typography.bodyLarge,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(18.dp))
                                        Button(
                                            onClick = { viewModel.dismissLevelUp() },
                                            colors = ButtonDefaults.buttonColors(containerColor = ZenPurple),
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier.fillMaxWidth(0.6f)
                                        ) {
                                            Text("Celebrate ✨")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ZenAppNavHost(
    viewModel: ZenViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf("home") }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_nav",
        modifier = modifier.fillMaxSize()
    ) { screen ->
        when (screen) {
            "home" -> HomeScreen(
                viewModel = viewModel,
                onNavigateToRelax = { currentScreen = "relax" },
                onNavigateToQuickChallenge = { currentScreen = "quick_challenge" },
                onNavigateToPuzzle = { currentScreen = "puzzle" },
                onNavigateToCreative = { currentScreen = "creative" },
                onNavigateToReaction = { currentScreen = "reaction" },
                onNavigateToDaily = { currentScreen = "daily" },
                onNavigateToMultiplayer = { currentScreen = "multiplayer" },
                onNavigateToShopProfile = { currentScreen = "shop_profile" },
                onNavigateToSettings = { currentScreen = "settings" }
            )
            "relax" -> RelaxScreen(
                viewModel = viewModel,
                onBack = { currentScreen = "home" }
            )
            "quick_challenge" -> QuickChallengeScreen(
                viewModel = viewModel,
                onBack = { currentScreen = "home" }
            )
            "puzzle" -> PuzzleScreen(
                viewModel = viewModel,
                onBack = { currentScreen = "home" }
            )
            "creative" -> CreativeScreen(
                viewModel = viewModel,
                onBack = { currentScreen = "home" }
            )
            "reaction" -> ReactionScreen(
                viewModel = viewModel,
                onBack = { currentScreen = "home" }
            )
            "daily" -> DailyChallengeScreen(
                viewModel = viewModel,
                onBack = { currentScreen = "home" }
            )
            "multiplayer" -> MultiplayerScreen(
                viewModel = viewModel,
                onBack = { currentScreen = "home" }
            )
            "shop_profile" -> ShopProfileScreen(
                viewModel = viewModel,
                onBack = { currentScreen = "home" }
            )
            "settings" -> SettingsScreen(
                viewModel = viewModel,
                onBack = { currentScreen = "home" }
            )
            else -> HomeScreen(
                viewModel = viewModel,
                onNavigateToRelax = { currentScreen = "relax" },
                onNavigateToQuickChallenge = { currentScreen = "quick_challenge" },
                onNavigateToPuzzle = { currentScreen = "puzzle" },
                onNavigateToCreative = { currentScreen = "creative" },
                onNavigateToReaction = { currentScreen = "reaction" },
                onNavigateToDaily = { currentScreen = "daily" },
                onNavigateToMultiplayer = { currentScreen = "multiplayer" },
                onNavigateToShopProfile = { currentScreen = "shop_profile" },
                onNavigateToSettings = { currentScreen = "settings" }
            )
        }
    }
}
