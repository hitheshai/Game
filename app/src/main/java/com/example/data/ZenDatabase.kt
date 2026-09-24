package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        MoodLogEntity::class,
        GameStatEntity::class,
        AchievementEntity::class,
        SettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ZenDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun moodDao(): MoodDao
    abstract fun gameStatDao(): GameStatDao
    abstract fun achievementDao(): AchievementDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: ZenDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): ZenDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ZenDatabase::class.java,
                    "zen_play_database"
                )
                    .addCallback(ZenDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class ZenDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            suspend fun populateInitialData(db: ZenDatabase) {
                // Initial User
                db.userDao().insertOrUpdateUser(
                    UserEntity(
                        id = 1,
                        level = 1,
                        xp = 35,
                        xpToNextLevel = 100,
                        coins = 120,
                        selectedAvatarId = "avatar_cloud",
                        selectedThemeId = "theme_serene",
                        streakDays = 1,
                        totalRelaxMinutes = 3,
                        gamesPlayed = 0,
                        unlockedAvatars = "avatar_cloud,avatar_cat",
                        unlockedThemes = "theme_serene,theme_aurora"
                    )
                )

                // Initial Settings
                db.settingsDao().insertOrUpdateSettings(
                    SettingsEntity(
                        id = 1,
                        soundEnabled = true,
                        soundVolume = 0.8f,
                        hapticsEnabled = true,
                        reducedMotion = false,
                        largeText = false,
                        colorBlindFriendly = false,
                        breakReminderMinutes = 15
                    )
                )

                // Initial Achievements
                val defaultAchievements = listOf(
                    AchievementEntity("ach_first_breath", "Mindful Breath", "Complete your first guided breathing exercise", "🌬️", true, System.currentTimeMillis()),
                    AchievementEntity("ach_bubble_popper", "Bubble Breeze", "Pop 30 floating stress bubbles", "🫧", false, 0),
                    AchievementEntity("ach_memory_master", "Calm Mind", "Solve a puzzle memory match game", "🧩", false, 0),
                    AchievementEntity("ach_creative_spirit", "Color Flow", "Draw a calming glowing creation", "🎨", false, 0),
                    AchievementEntity("ach_rhythm_peace", "Zen Rhythm", "Play harmonious chords in Reaction Mode", "🎵", false, 0),
                    AchievementEntity("ach_daily_spark", "Daily Light", "Complete today's daily calm challenge", "⭐", false, 0),
                    AchievementEntity("ach_zen_streak", "Serenity Streak", "Check in 3 days in a row", "🔥", false, 0),
                    AchievementEntity("ach_friendly_vibe", "Harmony Share", "Share peaceful joy with a friend", "🤝", false, 0)
                )
                db.achievementDao().insertAchievements(defaultAchievements)
            }
        }
    }
}
