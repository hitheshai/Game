package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ZenRepository(
    private val userDao: UserDao,
    private val moodDao: MoodDao,
    private val gameStatDao: GameStatDao,
    private val achievementDao: AchievementDao,
    private val settingsDao: SettingsDao
) {
    val userProfile: Flow<UserEntity?> = userDao.getUserProfile()
    val recentMoods: Flow<List<MoodLogEntity>> = moodDao.getRecentMoods()
    val gameStats: Flow<List<GameStatEntity>> = gameStatDao.getAllGameStats()
    val achievements: Flow<List<AchievementEntity>> = achievementDao.getAllAchievements()
    val settings: Flow<SettingsEntity?> = settingsDao.getSettings()

    suspend fun saveMoodLog(rating: Int, moodName: String, phase: String, note: String = "") {
        moodDao.insertMood(
            MoodLogEntity(
                moodRating = rating,
                moodName = moodName,
                phase = phase,
                note = note
            )
        )
        // Award modest peaceful XP for mindful mood check-in
        awardXpAndCoins(xpGained = 20, coinsGained = 10)
    }

    suspend fun awardXpAndCoins(
        xpGained: Int,
        coinsGained: Int,
        gameModePlayed: String? = null,
        scoreAchieved: Int = 0
    ): Pair<Boolean, Int> { // Returns: (DidLevelUp, NewLevel)
        var didLevelUp = false
        var currentLevel = 1

        val existingUser = userDao.getUserProfile()
        // We'll update the user directly
        userDao.getUserProfile()

        // Fetch or create user
        val user = UserEntity(
            id = 1,
            level = 1,
            xp = 0,
            xpToNextLevel = 100,
            coins = 50
        ) // fallback if null

        // In Dao we can do atomic calculation or retrieve current snapshot
        // We can create a dedicated method in Repository
        // Let's implement full level-up formula:
        return Pair(didLevelUp, currentLevel)
    }

    suspend fun updateUserProfile(user: UserEntity) {
        userDao.insertOrUpdateUser(user)
    }

    suspend fun updateSettings(settings: SettingsEntity) {
        settingsDao.insertOrUpdateSettings(settings)
    }

    suspend fun unlockAchievement(id: String) {
        val rows = achievementDao.unlockAchievement(id, System.currentTimeMillis())
        if (rows > 0) {
            // Reward for achievement
            // Handled in ViewModel with notification
        }
    }

    suspend fun recordGameResult(mode: String, score: Int) {
        val existing = gameStatDao.getStatForMode(mode)
        val newHigh = if (existing != null) maxOf(existing.highScore, score) else score
        val newTimes = (existing?.timesPlayed ?: 0) + 1
        gameStatDao.insertOrUpdateStat(
            GameStatEntity(
                gameMode = mode,
                highScore = newHigh,
                timesPlayed = newTimes,
                bestStreak = existing?.bestStreak ?: 0
            )
        )
    }

    suspend fun recordRelaxTime(minutes: Int) {
        // Increment relax time
    }

    fun getTodayDateKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}
