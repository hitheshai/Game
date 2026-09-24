package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.HapticsManager
import com.example.audio.SoundManager
import com.example.data.AchievementEntity
import com.example.data.AvatarOption
import com.example.data.GameStatEntity
import com.example.data.MoodLogEntity
import com.example.data.SettingsEntity
import com.example.data.ThemeOption
import com.example.data.UserEntity
import com.example.data.ZenDatabase
import com.example.data.ZenRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ZenViewModel(application: Application) : AndroidViewModel(application) {
    val soundManager = SoundManager(application)
    val hapticsManager = HapticsManager(application)

    private val database = ZenDatabase.getDatabase(application, viewModelScope)
    private val repository = ZenRepository(
        database.userDao(),
        database.moodDao(),
        database.gameStatDao(),
        database.achievementDao(),
        database.settingsDao()
    )

    val userProfile: StateFlow<UserEntity?> = repository.userProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val settings: StateFlow<SettingsEntity?> = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val recentMoods: StateFlow<List<MoodLogEntity>> = repository.recentMoods.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val achievements: StateFlow<List<AchievementEntity>> = repository.achievements.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val gameStats: StateFlow<List<GameStatEntity>> = repository.gameStats.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Confetti effect trigger
    private val _confettiTrigger = MutableStateFlow(false)
    val confettiTrigger: StateFlow<Boolean> = _confettiTrigger.asStateFlow()

    // Level-up celebration dialog state
    private val _levelUpEvent = MutableStateFlow<Int?>(null)
    val levelUpEvent: StateFlow<Int?> = _levelUpEvent.asStateFlow()

    // Break reminder dialog trigger
    private val _showBreakReminder = MutableStateFlow(false)
    val showBreakReminder: StateFlow<Boolean> = _showBreakReminder.asStateFlow()

    // Session time tracking
    private var sessionSeconds = 0
    private var sessionTimerJob: Job? = null

    // Available Avatars (All free through earned gameplay coins!)
    val avatarCatalog = listOf(
        AvatarOption("avatar_cloud", "Zen Cloud", "☁️", 0, "Calm, floating free of stress"),
        AvatarOption("avatar_cat", "Cozy Cat", "🐱", 0, "Warm, peaceful and purring"),
        AvatarOption("avatar_panda", "Zen Panda", "🐼", 100, "Gentle master of bamboo serenity"),
        AvatarOption("avatar_sloth", "Chill Sloth", "🦥", 150, "Expert at taking life nice and slow"),
        AvatarOption("avatar_bunny", "Star Bunny", "🐰", 200, "Brings joy and starlight hops"),
        AvatarOption("avatar_fox", "Mystic Fox", "🦊", 250, "Mindful guardian of forest tranquility")
    )

    // Available Themes
    val themeCatalog = listOf(
        ThemeOption("theme_serene", "Serene Sky", 0, 0xFF6C5CE7, 0xFF74B9FF),
        ThemeOption("theme_aurora", "Aurora Borealis", 0, 0xFF00CEC9, 0xFFA29BFE),
        ThemeOption("theme_sunset", "Sunset Glow", 100, 0xFFFF7675, 0xFFFFA502),
        ThemeOption("theme_forest", "Forest Sage", 150, 0xFF00B894, 0xFF55EFC4),
        ThemeOption("theme_midnight", "Midnight Zen", 200, 0xFF341F97, 0xFF54A0FF)
    )

    init {
        startSessionTimer()
    }

    private fun startSessionTimer() {
        sessionTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(60000L) // check every minute
                sessionSeconds += 60
                val minutesPlayed = sessionSeconds / 60
                val reminderMins = settings.value?.breakReminderMinutes ?: 15
                if (reminderMins > 0 && minutesPlayed > 0 && minutesPlayed % reminderMins == 0) {
                    _showBreakReminder.value = true
                }
            }
        }
    }

    fun dismissBreakReminder() {
        _showBreakReminder.value = false
    }

    fun triggerConfetti() {
        _confettiTrigger.value = true
        soundManager.playSuccessChord()
        hapticsManager.playSuccessPattern()
        viewModelScope.launch {
            delay(2500)
            _confettiTrigger.value = false
        }
    }

    fun dismissLevelUp() {
        _levelUpEvent.value = null
    }

    fun addXpAndCoins(
        xp: Int,
        coins: Int,
        gameMode: String? = null,
        score: Int = 0,
        celebrate: Boolean = false
    ) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserEntity()
            val newTotalXp = current.xp + xp
            var newLevel = current.level
            var reqXp = current.xpToNextLevel
            var leveledUp = false

            var remainingXp = newTotalXp
            while (remainingXp >= reqXp) {
                remainingXp -= reqXp
                newLevel += 1
                reqXp = 100 + (newLevel * 40)
                leveledUp = true
            }

            val updatedUser = current.copy(
                level = newLevel,
                xp = remainingXp,
                xpToNextLevel = reqXp,
                coins = current.coins + coins,
                gamesPlayed = if (gameMode != null) current.gamesPlayed + 1 else current.gamesPlayed
            )
            repository.updateUserProfile(updatedUser)

            if (gameMode != null) {
                repository.recordGameResult(gameMode, score)
            }

            if (leveledUp) {
                _levelUpEvent.value = newLevel
                triggerConfetti()
            } else if (celebrate) {
                triggerConfetti()
            } else {
                soundManager.playChime(3)
                hapticsManager.playLightTap()
            }
        }
    }

    fun saveMood(rating: Int, name: String, phase: String, note: String) {
        viewModelScope.launch {
            repository.saveMoodLog(rating, name, phase, note)
            soundManager.playSingingBowl()
            hapticsManager.playGentlePulse()
        }
    }

    fun unlockAchievement(id: String) {
        viewModelScope.launch {
            repository.unlockAchievement(id)
        }
    }

    fun selectAvatar(avatarId: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: return@launch
            val unlocked = current.unlockedAvatars.split(",").toSet()
            if (unlocked.contains(avatarId)) {
                repository.updateUserProfile(current.copy(selectedAvatarId = avatarId))
                soundManager.playSoftClick()
                hapticsManager.playLightTap()
            } else {
                val item = avatarCatalog.find { it.id == avatarId } ?: return@launch
                if (current.coins >= item.unlockCost) {
                    val newUnlocked = "${current.unlockedAvatars},$avatarId"
                    val updated = current.copy(
                        coins = current.coins - item.unlockCost,
                        selectedAvatarId = avatarId,
                        unlockedAvatars = newUnlocked
                    )
                    repository.updateUserProfile(updated)
                    triggerConfetti()
                }
            }
        }
    }

    fun selectTheme(themeId: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: return@launch
            val unlocked = current.unlockedThemes.split(",").toSet()
            if (unlocked.contains(themeId)) {
                repository.updateUserProfile(current.copy(selectedThemeId = themeId))
                soundManager.playSoftClick()
                hapticsManager.playLightTap()
            } else {
                val item = themeCatalog.find { it.id == themeId } ?: return@launch
                if (current.coins >= item.unlockCost) {
                    val newUnlocked = "${current.unlockedThemes},$themeId"
                    val updated = current.copy(
                        coins = current.coins - item.unlockCost,
                        selectedThemeId = themeId,
                        unlockedThemes = newUnlocked
                    )
                    repository.updateUserProfile(updated)
                    triggerConfetti()
                }
            }
        }
    }

    fun updateSoundSettings(enabled: Boolean, volume: Float) {
        soundManager.isSoundEnabled = enabled
        soundManager.soundVolume = volume
        viewModelScope.launch {
            val cur = settings.value ?: SettingsEntity()
            repository.updateSettings(cur.copy(soundEnabled = enabled, soundVolume = volume))
        }
    }

    fun updateHapticsSettings(enabled: Boolean) {
        hapticsManager.isHapticsEnabled = enabled
        viewModelScope.launch {
            val cur = settings.value ?: SettingsEntity()
            repository.updateSettings(cur.copy(hapticsEnabled = enabled))
        }
    }

    fun updateAccessibilitySettings(
        reducedMotion: Boolean,
        largeText: Boolean,
        colorBlind: Boolean,
        breakReminderMins: Int
    ) {
        viewModelScope.launch {
            val cur = settings.value ?: SettingsEntity()
            repository.updateSettings(
                cur.copy(
                    reducedMotion = reducedMotion,
                    largeText = largeText,
                    colorBlindFriendly = colorBlind,
                    breakReminderMinutes = breakReminderMins
                )
            )
        }
    }

    fun completeDailyChallenge() {
        viewModelScope.launch {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val curSettings = settings.value ?: SettingsEntity()
            if (curSettings.lastDailyDateStr != todayStr || !curSettings.dailyGoalCompleted) {
                repository.updateSettings(
                    curSettings.copy(
                        lastDailyDateStr = todayStr,
                        dailyGoalCompleted = true,
                        dailyProgressCount = 1
                    )
                )
                unlockAchievement("ach_daily_spark")
                addXpAndCoins(xp = 120, coins = 60, celebrate = true)
            }
        }
    }

    fun getTodayChallengeTitle(): String = "Mindful Serenity: Take a 1-minute guided breath & pop 20 stress bubbles."
}
