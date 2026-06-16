package com.tractorfieldtrip.game

import com.tractorfieldtrip.TractorPerks

data class TractorRuntime(
    var row: Int,
    var col: Int,
    var direction: Direction,
    var incomingDirection: Direction? = null,
    var pendingTurn: Direction? = null,
    var blockedOnCrane: Boolean = false,
    val isSecondary: Boolean = false
)

class GameEngine(
    private val level: LevelDefinition,
    private val perks: TractorPerks,
    useDualTractors: Boolean
) {
    private val tractors = buildTractors(useDualTractors)
    var activeIndex: Int = 0
        private set

    var phase: GamePhase = GamePhase.RUNNING
        private set
    var fuel: Float = FULL_FUEL
        private set
    var lives: Int = MAX_LIVES
        private set
    var score: Int = 0
        private set
    var collected: CollectedCounts = CollectedCounts()
        private set

    private val remainingEntities = level.entities.toMutableMap()

    var onMaterialCollected: (() -> Unit)? = null
    var onCellsMoved: ((Int) -> Unit)? = null
    var onFuelCollected: (() -> Unit)? = null
    var onCraneBlocked: (() -> Unit)? = null
    var onDamageTaken: (() -> Unit)? = null
    var onPhaseChanged: ((GamePhase) -> Unit)? = null

    val totals: CollectibleTotals get() = level.totals
    val tractorCount: Int get() = tractors.size

    fun tractorStates(): List<TractorRuntime> = tractors

    fun hudSnapshot(): HudSnapshot = HudSnapshot(
        level = level.levelNumber,
        fuelPct = fuel / FULL_FUEL,
        lives = lives,
        cratesCollected = collected.crates,
        crateTotal = totals.crates,
        sacksCollected = collected.sacks,
        sackTotal = totals.sacks,
        bricksCollected = collected.bricks,
        brickTotal = totals.bricks,
        score = score,
        activeTractorIndex = activeIndex,
        tractorCount = tractors.size
    )

    fun activeTractorIndex(): Int = activeIndex

    fun switchActiveTractor() {
        if (tractors.size < 2) return
        activeIndex = (activeIndex + 1) % tractors.size
    }

    fun queueTurn(direction: Direction) {
        if (phase == GamePhase.PAUSED || phase == GamePhase.CLEARED ||
            phase == GamePhase.LOST_FUEL || phase == GamePhase.LOST_LIVES
        ) {
            return
        }
        val active = tractors[activeIndex]
        if (active.blockedOnCrane) {
            val incoming = active.incomingDirection ?: active.direction.opposite()
            val options = forwardExits(active.row, active.col, incoming)
            if (direction in options) {
                active.direction = direction
                active.incomingDirection = direction.opposite()
                active.blockedOnCrane = false
                active.pendingTurn = null
            }
            return
        }
        active.pendingTurn = direction
    }

    fun tickFuel() {
        if (phase != GamePhase.RUNNING) return
        val drain = FUEL_DRAIN_PER_SEC * if (perks.fuelSaver) 0.7f else 1f
        fuel = (fuel - drain).coerceAtLeast(0f)
        if (fuel <= 0f) setPhase(GamePhase.LOST_FUEL)
    }

    fun step(): Boolean {
        if (phase != GamePhase.RUNNING) return false
        var movedCount = 0
        tractors.forEachIndexed { index, tractor ->
            if (tractor.blockedOnCrane) return@forEachIndexed
            val pending = if (index == activeIndex) tractor.pendingTurn else null
            val incoming = tractor.incomingDirection ?: tractor.direction
            val nextDir = resolveExit(tractor.row, tractor.col, incoming, pending)
            if (index == activeIndex) tractor.pendingTurn = null
            tractor.direction = nextDir

            val nextCell = Cell(tractor.row + nextDir.dr, tractor.col + nextDir.dc)
            if (!level.isWalkable(nextCell)) return@forEachIndexed

            tractor.incomingDirection = nextDir
            tractor.row = nextCell.row
            tractor.col = nextCell.col
            handleEntity(nextCell, tractor)
            movedCount++
            if (phase != GamePhase.RUNNING) return movedCount > 0
        }
        if (movedCount > 0) onCellsMoved?.invoke(movedCount)
        return movedCount > 0
    }

    private fun handleEntity(cell: Cell, tractor: TractorRuntime) {
        when (remainingEntities[cell]) {
            EntityType.CRATE -> {
                remainingEntities.remove(cell)
                collected = collected.copy(crates = collected.crates + 1)
                score += POINTS_PER_MATERIAL
                onMaterialCollected?.invoke()
            }
            EntityType.SACK -> {
                remainingEntities.remove(cell)
                collected = collected.copy(sacks = collected.sacks + 1)
                score += POINTS_PER_MATERIAL
                onMaterialCollected?.invoke()
            }
            EntityType.BRICK -> {
                remainingEntities.remove(cell)
                collected = collected.copy(bricks = collected.bricks + 1)
                score += POINTS_PER_MATERIAL
                onMaterialCollected?.invoke()
            }
            EntityType.FUEL -> {
                remainingEntities.remove(cell)
                fuel = (fuel + FUEL_CAN_BONUS).coerceAtMost(FULL_FUEL)
                onFuelCollected?.invoke()
            }
            EntityType.CRANE -> {
                tractor.blockedOnCrane = true
                onCraneBlocked?.invoke()
            }
            EntityType.PIT -> {
                if (!perks.bigTires) applyDamage()
            }
            EntityType.BARREL -> {
                if (!perks.heavy) applyDamage()
            }
            null -> return
        }
        checkCleared()
    }

    private fun applyDamage() {
        if (phase != GamePhase.RUNNING) return
        lives -= 1
        onDamageTaken?.invoke()
        if (lives <= 0) setPhase(GamePhase.LOST_LIVES)
    }

    private fun checkCleared() {
        if (phase == GamePhase.RUNNING && collected.isComplete(totals)) {
            setPhase(GamePhase.CLEARED)
        }
    }

    private fun setPhase(newPhase: GamePhase) {
        if (phase == newPhase) return
        phase = newPhase
        onPhaseChanged?.invoke(newPhase)
    }

    fun entityAt(cell: Cell): EntityType? = remainingEntities[cell]

    fun pause() {
        if (phase == GamePhase.RUNNING) setPhase(GamePhase.PAUSED)
    }

    fun resume() {
        if (phase == GamePhase.PAUSED) setPhase(GamePhase.RUNNING)
    }

    fun buildResult(): GameResult {
        val materialsPct = if (totals.total == 0) {
            100
        } else {
            val got = collected.crates + collected.sacks + collected.bricks
            (got * 100) / totals.total
        }
        val fuelLeftPct = fuel.toInt()
        val stars = when {
            phase != GamePhase.CLEARED -> 0
            lives == MAX_LIVES && fuelLeftPct >= 50 -> 3
            lives >= 2 || fuelLeftPct >= 25 -> 2
            else -> 1
        }
        val coinsEarned = if (phase == GamePhase.CLEARED) {
            10 + level.levelNumber * 2 + stars * 5
        } else {
            0
        }
        return GameResult(
            cleared = phase == GamePhase.CLEARED,
            failPhase = if (phase == GamePhase.CLEARED) null else phase,
            stars = stars,
            coinsEarned = coinsEarned,
            materialsPct = materialsPct,
            fuelLeftPct = fuelLeftPct,
            livesLeft = lives,
            hadDualTractors = tractors.size >= 2
        )
    }

    private fun buildTractors(useDual: Boolean): MutableList<TractorRuntime> {
        val list = mutableListOf(
            TractorRuntime(
                row = level.startCell.row,
                col = level.startCell.col,
                direction = level.startDirection,
                isSecondary = false
            )
        )
        if (useDual && level.secondStartCell != null) {
            list += TractorRuntime(
                row = level.secondStartCell.row,
                col = level.secondStartCell.col,
                direction = level.secondStartDirection,
                isSecondary = true
            )
        }
        return list
    }

    private fun resolveExit(
        row: Int,
        col: Int,
        incoming: Direction,
        pending: Direction?
    ): Direction {
        val options = forwardExits(row, col, incoming)
        if (options.size == 1) return options[0]
        if (options.isEmpty()) return incoming

        pending?.let { choice ->
            if (choice in options) return choice
        }

        if (isTJunction(row, col) && incoming in options) {
            val left = incoming.left()
            if (left in options) return left
        }

        if (incoming in options) return incoming
        return options.first()
    }

    private fun forwardExits(row: Int, col: Int, incoming: Direction): List<Direction> =
        Direction.CARDINAL.filter { dir ->
            dir != incoming.opposite() && level.isWalkable(Cell(row + dir.dr, col + dir.dc))
        }

    private fun isTJunction(row: Int, col: Int): Boolean =
        Direction.CARDINAL.count { level.isWalkable(Cell(row + it.dr, col + it.dc)) } == 3

    companion object {
        const val MAX_LIVES = 3
        const val FULL_FUEL = 100f
        const val FUEL_DRAIN_PER_SEC = 1f
        const val FUEL_CAN_BONUS = 30f
        const val POINTS_PER_MATERIAL = 5
        const val MOVE_INTERVAL_MS = 280L
    }
}
