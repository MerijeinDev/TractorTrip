package farmyard.tractortrip.lab

import android.content.Context
import farmyard.tractortrip.lab.game.GameEngine

data class Achievement(
    val id: String,
    val titleRes: Int,
    val descRes: Int,
    val coinReward: Int
)

object AchievementCatalog {

    const val MARATHON_DISTANCE = 5000

    val all: List<Achievement> = listOf(
        Achievement("first_trip", R.string.ach_first_trip_title, R.string.ach_first_trip_desc, 10),
        Achievement("maze_runner", R.string.ach_maze_runner_title, R.string.ach_maze_runner_desc, 50),
        Achievement("fuel_master", R.string.ach_fuel_master_title, R.string.ach_fuel_master_desc, 75),
        Achievement("full_collect", R.string.ach_full_collect_title, R.string.ach_full_collect_desc, 50),
        Achievement("twin_driver", R.string.ach_twin_driver_title, R.string.ach_twin_driver_desc, 100),
        Achievement("no_crash", R.string.ach_no_crash_title, R.string.ach_no_crash_desc, 100),
        Achievement("yard_boss", R.string.ach_yard_boss_title, R.string.ach_yard_boss_desc, 100),
        Achievement("field_champion", R.string.ach_field_champion_title, R.string.ach_field_champion_desc, 300),
        Achievement("big_tires_fan", R.string.ach_big_tires_fan_title, R.string.ach_big_tires_fan_desc, 200),
        Achievement("marathon", R.string.ach_marathon_title, R.string.ach_marathon_desc, 75)
    )

    fun onFirstMaterial(context: Context) {
        GameProgress.tryUnlockAchievement(context, "first_trip")
    }

    fun onDistanceUpdated(context: Context) {
        if (GameProgress.getTotalDistance(context) >= MARATHON_DISTANCE) {
            GameProgress.tryUnlockAchievement(context, "marathon")
        }
    }

    fun onSkinPurchased(context: Context, skinId: Int) {
        if (skinId == 2) {
            GameProgress.tryUnlockAchievement(context, "big_tires_fan")
        }
    }

    fun checkAfterLevelComplete(
        context: Context,
        level: Int,
        livesLeft: Int,
        fuelLeftPct: Int,
        materialsPct: Int,
        hadDualTractors: Boolean
    ) {
        if (level >= 5) GameProgress.tryUnlockAchievement(context, "maze_runner")
        if (fuelLeftPct >= 50) GameProgress.tryUnlockAchievement(context, "fuel_master")
        if (materialsPct >= 100) GameProgress.tryUnlockAchievement(context, "full_collect")
        if (hadDualTractors) GameProgress.tryUnlockAchievement(context, "twin_driver")
        if (livesLeft == GameEngine.MAX_LIVES) GameProgress.tryUnlockAchievement(context, "no_crash")
        if (level >= 20) GameProgress.tryUnlockAchievement(context, "yard_boss")
        if (level >= GameProgress.TOTAL_LEVELS) {
            GameProgress.tryUnlockAchievement(context, "field_champion")
        }
    }
}
