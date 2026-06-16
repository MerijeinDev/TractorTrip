package com.tractorfieldtrip

import android.content.Context

object GameProgress {

    const val TOTAL_LEVELS = 40
    const val STARTING_COINS = 500

    private const val PREFS_NAME = "tractor_field_trip_progress"
    private const val KEY_COINS = "coins"
    private const val KEY_MAX_UNLOCKED_LEVEL = "max_unlocked_level"
    private const val KEY_ONBOARDING_DONE = "onboarding_done"
    private const val KEY_LEVEL_STARS_PREFIX = "level_stars_"
    private const val KEY_SELECTED_SKIN = "selected_skin"
    private const val KEY_OWNED_SKINS = "owned_skins"
    private const val KEY_ACHIEVEMENTS = "achievements"
    private const val KEY_SOUND_ENABLED = "sound_enabled"
    private const val KEY_MUSIC_ENABLED = "music_enabled"
    private const val KEY_TOTAL_DISTANCE = "total_distance"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isOnboardingDone(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ONBOARDING_DONE, false)

    fun setOnboardingDone(context: Context) {
        prefs(context).edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()
    }

    fun getCoins(context: Context): Int =
        prefs(context).getInt(KEY_COINS, STARTING_COINS)

    fun setCoins(context: Context, amount: Int) {
        prefs(context).edit().putInt(KEY_COINS, amount.coerceAtLeast(0)).apply()
    }

    fun addCoins(context: Context, delta: Int) {
        setCoins(context, getCoins(context) + delta)
    }

    fun getMaxUnlockedLevel(context: Context): Int =
        prefs(context).getInt(KEY_MAX_UNLOCKED_LEVEL, 1).coerceIn(1, TOTAL_LEVELS)

    fun isLevelUnlocked(context: Context, level: Int): Boolean =
        level in 1..TOTAL_LEVELS && level <= getMaxUnlockedLevel(context)

    fun isSkinUnlockedInShop(context: Context, skin: TractorSkin): Boolean =
        getMaxUnlockedLevel(context) >= skin.unlockLevel

    fun unlockLevel(context: Context, level: Int) {
        if (level !in 1..TOTAL_LEVELS) return
        val current = getMaxUnlockedLevel(context)
        if (level > current) {
            prefs(context).edit().putInt(KEY_MAX_UNLOCKED_LEVEL, level).apply()
        }
    }

    fun getLevelStars(context: Context, level: Int): Int =
        prefs(context).getInt("$KEY_LEVEL_STARS_PREFIX$level", 0).coerceIn(0, 3)

    fun setLevelStars(context: Context, level: Int, stars: Int) {
        if (level !in 1..TOTAL_LEVELS) return
        val clamped = stars.coerceIn(0, 3)
        val existing = getLevelStars(context, level)
        if (clamped > existing) {
            prefs(context).edit().putInt("$KEY_LEVEL_STARS_PREFIX$level", clamped).apply()
        }
    }

    fun getSelectedSkinId(context: Context): Int =
        prefs(context).getInt(KEY_SELECTED_SKIN, TractorCatalog.BASIC_ID)

    fun setSelectedSkinId(context: Context, skinId: Int) {
        prefs(context).edit().putInt(KEY_SELECTED_SKIN, skinId).apply()
    }

    fun isSkinOwned(context: Context, skinId: Int): Boolean {
        if (skinId == TractorCatalog.BASIC_ID) return true
        return prefs(context).getStringSet(KEY_OWNED_SKINS, emptySet())?.contains(skinId.toString()) == true
    }

    fun unlockSkin(context: Context, skinId: Int) {
        val owned = prefs(context).getStringSet(KEY_OWNED_SKINS, emptySet())?.toMutableSet() ?: mutableSetOf()
        owned += skinId.toString()
        prefs(context).edit().putStringSet(KEY_OWNED_SKINS, owned).apply()
        AchievementCatalog.onSkinPurchased(context, skinId)
    }

    fun purchaseSkin(context: Context, skin: TractorSkin): Boolean {
        if (!isSkinUnlockedInShop(context, skin)) return false
        if (isSkinOwned(context, skin.id)) return true
        val coins = getCoins(context)
        if (coins < skin.price) return false
        setCoins(context, coins - skin.price)
        unlockSkin(context, skin.id)
        return true
    }

    fun isAchievementUnlocked(context: Context, achievementId: String): Boolean =
        prefs(context).getStringSet(KEY_ACHIEVEMENTS, emptySet())?.contains(achievementId) == true

    /** Unlocks achievement and awards coin reward once. Returns true if newly unlocked. */
    fun tryUnlockAchievement(context: Context, achievementId: String): Boolean {
        if (isAchievementUnlocked(context, achievementId)) return false
        val set = prefs(context).getStringSet(KEY_ACHIEVEMENTS, emptySet())?.toMutableSet() ?: mutableSetOf()
        if (!set.add(achievementId)) return false
        prefs(context).edit().putStringSet(KEY_ACHIEVEMENTS, set).apply()
        AchievementCatalog.all.firstOrNull { it.id == achievementId }?.let { achievement ->
            addCoins(context, achievement.coinReward)
        }
        SoundManager.play(context, SoundManager.Effect.NOTIFY)
        return true
    }

    fun getTotalDistance(context: Context): Int =
        prefs(context).getInt(KEY_TOTAL_DISTANCE, 0)

    fun addDistance(context: Context, cells: Int) {
        if (cells <= 0) return
        val total = getTotalDistance(context) + cells
        prefs(context).edit().putInt(KEY_TOTAL_DISTANCE, total).apply()
    }

    fun isSoundEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SOUND_ENABLED, true)

    fun setSoundEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    fun isMusicEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_MUSIC_ENABLED, true)

    fun setMusicEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_MUSIC_ENABLED, enabled).apply()
    }

    fun formatCoins(value: Int): String =
        value.toString().reversed().chunked(3).joinToString(" ").reversed()

    fun getLevelsCompleted(context: Context): Int =
        (getMaxUnlockedLevel(context) - 1).coerceAtLeast(0)

    fun getTotalStars(context: Context): Int =
        (1..TOTAL_LEVELS).sumOf { getLevelStars(context, it) }

    fun getAchievementsUnlockedCount(context: Context): Int =
        AchievementCatalog.all.count { isAchievementUnlocked(context, it.id) }
}
