package com.tractorfieldtrip.game

enum class Direction(val dr: Int, val dc: Int) {
    UP(-1, 0),
    RIGHT(0, 1),
    DOWN(1, 0),
    LEFT(0, -1);

    fun opposite(): Direction = when (this) {
        UP -> DOWN
        DOWN -> UP
        LEFT -> RIGHT
        RIGHT -> LEFT
    }

    fun left(): Direction = when (this) {
        UP -> LEFT
        LEFT -> DOWN
        DOWN -> RIGHT
        RIGHT -> UP
    }

    companion object {
        val CARDINAL = entries
    }
}

enum class EntityType {
    CRATE,
    SACK,
    BRICK,
    FUEL,
    CRANE,
    PIT,
    BARREL
}

enum class GamePhase {
    RUNNING,
    BLOCKED_BY_CRANE,
    PAUSED,
    CLEARED,
    LOST_FUEL,
    LOST_LIVES
}

data class CollectibleTotals(
    val crates: Int = 0,
    val sacks: Int = 0,
    val bricks: Int = 0
) {
    val total: Int = crates + sacks + bricks
}

data class LevelDefinition(
    val levelNumber: Int,
    val width: Int,
    val height: Int,
    val walls: Set<Cell>,
    val entities: Map<Cell, EntityType>,
    val startCell: Cell,
    val startDirection: Direction,
    val secondStartCell: Cell? = null,
    val secondStartDirection: Direction = Direction.RIGHT,
    val totals: CollectibleTotals
) {
    fun isWalkable(cell: Cell): Boolean =
        cell.row in 0 until height &&
            cell.col in 0 until width &&
            cell !in walls

    fun entityAt(cell: Cell): EntityType? = entities[cell]
}

data class Cell(val row: Int, val col: Int)

data class CollectedCounts(
    val crates: Int = 0,
    val sacks: Int = 0,
    val bricks: Int = 0
) {
    fun isComplete(totals: CollectibleTotals): Boolean =
        crates >= totals.crates && sacks >= totals.sacks && bricks >= totals.bricks
}

data class HudSnapshot(
    val level: Int,
    val fuelPct: Float,
    val lives: Int,
    val cratesCollected: Int,
    val crateTotal: Int,
    val sacksCollected: Int,
    val sackTotal: Int,
    val bricksCollected: Int,
    val brickTotal: Int,
    val score: Int,
    val activeTractorIndex: Int = 0,
    val tractorCount: Int = 1
)

enum class GameOutcome {
    CLEARED,
    LOST_LIVES,
    LOST_FUEL
}

data class GameResult(
    val cleared: Boolean,
    val failPhase: GamePhase?,
    val stars: Int,
    val coinsEarned: Int,
    val materialsPct: Int,
    val fuelLeftPct: Int,
    val livesLeft: Int = 0,
    val hadDualTractors: Boolean = false
) {
    fun outcome(): GameOutcome = when {
        cleared -> GameOutcome.CLEARED
        failPhase == GamePhase.LOST_FUEL -> GameOutcome.LOST_FUEL
        else -> GameOutcome.LOST_LIVES
    }
}
