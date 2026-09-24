package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val level: Int = 1,
    val xp: Int = 0,
    val xpToNextLevel: Int = 100,
    val coins: Int = 50,
    val selectedAvatarId: String = "avatar_cloud",
    val selectedThemeId: String = "theme_serene",
    val streakDays: Int = 1,
    val lastActiveDate: Long = System.currentTimeMillis(),
    val totalRelaxMinutes: Int = 5,
    val gamesPlayed: Int = 0,
    val unlockedAvatars: String = "avatar_cloud,avatar_cat",
    val unlockedThemes: String = "theme_serene,theme_aurora"
)

@Entity(tableName = "mood_logs")
data class MoodLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val moodRating: Int, // 1 to 5
    val moodName: String, // Overwhelmed, Tense, Okay, Relaxed, Radiant
    val phase: String, // "before" or "after"
    val note: String = ""
)

@Entity(tableName = "game_stats")
data class GameStatEntity(
    @PrimaryKey val gameMode: String,
    val highScore: Int = 0,
    val timesPlayed: Int = 0,
    val bestStreak: Int = 0
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val isUnlocked: Boolean = false,
    val unlockedTimestamp: Long = 0
)

@Entity(tableName = "app_settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val soundEnabled: Boolean = true,
    val soundVolume: Float = 0.8f,
    val hapticsEnabled: Boolean = true,
    val reducedMotion: Boolean = false,
    val largeText: Boolean = false,
    val colorBlindFriendly: Boolean = false,
    val breakReminderMinutes: Int = 15,
    val screenTimeLimitMinutes: Int = 0, // 0 means no limit
    val parentPin: String = "",
    val lastDailyDateStr: String = "",
    val dailyProgressCount: Int = 0,
    val dailyGoalCompleted: Boolean = false
)

data class AvatarOption(
    val id: String,
    val name: String,
    val emoji: String,
    val unlockCost: Int,
    val description: String
)

data class ThemeOption(
    val id: String,
    val name: String,
    val unlockCost: Int,
    val primaryHex: Long,
    val secondaryHex: Long
)

enum class MoodType(val rating: Int, val label: String, val emoji: String) {
    OVERWHELMED(1, "Overwhelmed", "🌧️"),
    TENSE(2, "Tense", "☁️"),
    OKAY(3, "Okay", "🌤️"),
    RELAXED(4, "Relaxed", "🌸"),
    RADIANT(5, "Radiant", "✨")
}
