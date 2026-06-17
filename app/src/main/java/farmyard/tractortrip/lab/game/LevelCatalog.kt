package farmyard.tractortrip.lab.game

object LevelCatalog {

    /** Dual tractors from level 21; Twin Engine perk lowers to 19. */
    const val BASE_DUAL_LEVEL = 21
    const val TWIN_DUAL_LEVEL = 19

    fun dualTractorFromLevel(twinEngine: Boolean): Int =
        if (twinEngine) TWIN_DUAL_LEVEL else BASE_DUAL_LEVEL

    fun getLevel(number: Int): LevelDefinition {
        val level = number.coerceIn(1, 40)
        val templateIndex = (level - 1) / 5
        val variation = (level - 1) % 5
        val base = parseTemplate(BASE_MAPS[templateIndex], templateIndex)
        return applyVariation(base, level, variation, templateIndex)
    }

    /** 8 unique layouts — each used for 5 level variations (40 total). */
    private val BASE_MAPS: List<String> = listOf(
        // 0 · Tier 1 (Lv 1–5): pit on the main lane teaches damage early
        """
            #######
            #>..P.K#
            #.###.#
            #..F..#
            #..B..#
            #######
        """.trimIndent(),
        // 1 · Tier 1 (Lv 6–10): pit + crane
        """
            #########
            #>..P.K..#
            #.#####.#
            #...X...#
            #.C..B..#
            #########
        """.trimIndent(),
        // 2 · Tier 2 (Lv 11–15): 4×4 lanes, pits
        """
            ###########
            #>....K...#
            #.#######.#
            #...P.....#
            #.###.###.#
            #...C.F.B.#
            ###########
        """.trimIndent(),
        // 3 · Tier 2 (Lv 16–20): 4×4, pits + barrels
        """
            ###########
            #>....C...#
            #.#######.#
            #...R.P...#
            #.###.###.#
            #...K.F.B.#
            #.....P...#
            ###########
        """.trimIndent(),
        // 4 · Tier 3 (Lv 21–25): 5×5, dual, fuel
        """
            #############
            #>....K.....#
            #.#####.###.#
            #...F...P...#
            #.###.###.#.#
            #...C...2...#
            #.....B...F.#
            #############
        """.trimIndent(),
        // 5 · Tier 3 (Lv 26–30): 5×5 dual, fuel spread
        """
            ###############
            #>......K......#
            #.#####.#####.#
            #...F.....P...#
            #.###.###.###.#
            #...C...2...B.#
            #.....F...R...#
            #.......K.....#
            ###############
        """.trimIndent(),
        // 6 · Tier 4 (Lv 31–35): dead-ends, dual
        """
            #################
            #>.......K.......#
            #.#####.#####.##.#
            #...F.........P..#
            #.###.###.###.#.#
            #...C...2...B....#
            #.###.....###.###.#
            #...R...F...X.....#
            #################
        """.trimIndent(),
        // 7 · Tier 4 (Lv 36–40): hard dual, few fuel
        """
            ###################
            #>........K........#
            #.#####.#####.#####.#
            #...P.........R.....#
            #.###.###.###.###.#.#
            #...C...2...B...P...#
            #.###.#####.###.###.#
            #...K.......X.......#
            #........F..........#
            ###################
        """.trimIndent()
    )

    private val COLLECTIBLE_TYPES = listOf(
        EntityType.SACK,
        EntityType.CRATE,
        EntityType.BRICK
    )

    private fun applyVariation(
        base: LevelDefinition,
        levelNumber: Int,
        variation: Int,
        templateIndex: Int
    ): LevelDefinition {
        val entities = base.entities.toMutableMap()

        val collectEntries = entities.filter { (_, type) ->
            type == EntityType.SACK || type == EntityType.CRATE || type == EntityType.BRICK
        }.toList()
        if (collectEntries.isNotEmpty()) {
            val rotated = collectEntries.map { it.second }
                .let { types ->
                    val shift = variation % types.size
                    types.drop(shift) + types.take(shift)
                }
            collectEntries.forEachIndexed { index, (cell, _) ->
                entities[cell] = rotated[index]
            }
        }

        when (templateIndex) {
            in 0..1 -> {
                if (variation >= 2) {
                    addHazardIfFree(entities, base, EntityType.CRANE, variation)
                }
            }
            in 2..3 -> {
                if (variation >= 1) addHazardIfFree(entities, base, EntityType.PIT, variation)
                if (variation >= 3) addHazardIfFree(entities, base, EntityType.BARREL, variation + 1)
            }
            in 4..5 -> {
                if (variation % 2 == 1) {
                    swapFuelPositions(entities, variation)
                }
            }
            in 6..7 -> {
                if (variation >= 2) {
                    val fuelCells = entities.filter { it.value == EntityType.FUEL }.keys.toList()
                    if (fuelCells.size > 1 && variation >= 3) {
                        entities.remove(fuelCells.last())
                    }
                }
                if (variation == 4) addHazardIfFree(entities, base, EntityType.CRANE, variation)
            }
        }

        return base.copy(
            levelNumber = levelNumber,
            entities = entities,
            totals = totalsFromEntities(entities)
        )
    }

    private fun swapFuelPositions(entities: MutableMap<Cell, EntityType>, variation: Int) {
        val fuels = entities.filter { it.value == EntityType.FUEL }.keys.toList()
        if (fuels.size >= 2 && variation % 2 == 1) {
            val a = fuels[0]
            val b = fuels[1]
            entities[a] = EntityType.FUEL
            entities[b] = EntityType.FUEL
        }
    }

    private fun addHazardIfFree(
        entities: MutableMap<Cell, EntityType>,
        base: LevelDefinition,
        hazard: EntityType,
        seed: Int
    ) {
        val walkable = mutableListOf<Cell>()
        for (row in 0 until base.height) {
            for (col in 0 until base.width) {
                val cell = Cell(row, col)
                if (base.isWalkable(cell) && cell !in entities && cell != base.startCell &&
                    cell != base.secondStartCell
                ) {
                    walkable += cell
                }
            }
        }
        if (walkable.isNotEmpty()) {
            entities[walkable[seed % walkable.size]] = hazard
        }
    }

    private fun parseTemplate(map: String, templateIndex: Int): LevelDefinition {
        val lines = map.lines().filter { it.isNotBlank() }
        val height = lines.size
        val width = lines.maxOf { it.length }
        val walls = mutableSetOf<Cell>()
        val entities = mutableMapOf<Cell, EntityType>()
        var startCell = Cell(0, 0)
        var startDirection = Direction.RIGHT
        var secondStartCell: Cell? = null
        var secondStartDirection = Direction.LEFT

        lines.forEachIndexed { row, line ->
            line.padEnd(width, '#').forEachIndexed { col, char ->
                val cell = Cell(row, col)
                when (char) {
                    '#' -> walls += cell
                    '.', 'S' -> Unit
                    '>' -> {
                        startCell = cell
                        startDirection = Direction.RIGHT
                    }
                    '<' -> {
                        startCell = cell
                        startDirection = Direction.LEFT
                    }
                    '^' -> {
                        startCell = cell
                        startDirection = Direction.UP
                    }
                    'v' -> {
                        startCell = cell
                        startDirection = Direction.DOWN
                    }
                    '2' -> {
                        secondStartCell = cell
                        secondStartDirection = if (templateIndex >= 4) Direction.LEFT else Direction.RIGHT
                    }
                    'C' -> entities[cell] = EntityType.CRATE
                    'K' -> entities[cell] = EntityType.SACK
                    'B' -> entities[cell] = EntityType.BRICK
                    'F' -> entities[cell] = EntityType.FUEL
                    'X' -> entities[cell] = EntityType.CRANE
                    'P' -> entities[cell] = EntityType.PIT
                    'R' -> entities[cell] = EntityType.BARREL
                    else -> walls += cell
                }
            }
        }

        return LevelDefinition(
            levelNumber = templateIndex + 1,
            width = width,
            height = height,
            walls = walls,
            entities = entities,
            startCell = startCell,
            startDirection = startDirection,
            secondStartCell = if (templateIndex >= 4) secondStartCell else null,
            secondStartDirection = secondStartDirection,
            totals = totalsFromEntities(entities)
        )
    }

    private fun totalsFromEntities(entities: Map<Cell, EntityType>): CollectibleTotals {
        var crates = 0
        var sacks = 0
        var bricks = 0
        entities.values.forEach { type ->
            when (type) {
                EntityType.CRATE -> crates++
                EntityType.SACK -> sacks++
                EntityType.BRICK -> bricks++
                else -> Unit
            }
        }
        return CollectibleTotals(crates, sacks, bricks)
    }
}
