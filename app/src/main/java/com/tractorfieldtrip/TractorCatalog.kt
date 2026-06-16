package com.tractorfieldtrip

import com.tractorfieldtrip.game.LevelCatalog

data class TractorSkin(
    val id: Int,
    val nameRes: Int,
    val perkRes: Int,
    val spriteRes: Int,
    val price: Int,
    val unlockLevel: Int,
    val bigTires: Boolean = false,
    val fuelSaver: Boolean = false,
    val heavy: Boolean = false,
    val twinEngine: Boolean = false
)

object TractorCatalog {

    const val BASIC_ID = 1

    val all: List<TractorSkin> = listOf(
        TractorSkin(
            id = 1,
            nameRes = R.string.tractor_basic_name,
            perkRes = R.string.tractor_basic_perk,
            spriteRes = R.drawable.shop_tractor_1,
            price = 0,
            unlockLevel = 1
        ),
        TractorSkin(
            id = 2,
            nameRes = R.string.tractor_big_tires_name,
            perkRes = R.string.tractor_big_tires_perk,
            spriteRes = R.drawable.shop_tractor_2,
            price = 100,
            unlockLevel = 1,
            bigTires = true
        ),
        TractorSkin(
            id = 3,
            nameRes = R.string.tractor_fuel_name,
            perkRes = R.string.tractor_fuel_perk,
            spriteRes = R.drawable.shop_tractor_3,
            price = 200,
            unlockLevel = 1,
            fuelSaver = true
        ),
        TractorSkin(
            id = 4,
            nameRes = R.string.tractor_heavy_name,
            perkRes = R.string.tractor_heavy_perk,
            spriteRes = R.drawable.shop_tractor_4,
            price = 300,
            unlockLevel = 1,
            heavy = true
        ),
        TractorSkin(
            id = 5,
            nameRes = R.string.tractor_twin_name,
            perkRes = R.string.tractor_twin_perk,
            spriteRes = R.drawable.shop_tractor_5,
            price = 500,
            unlockLevel = 1,
            twinEngine = true
        )
    )

    fun byId(id: Int): TractorSkin =
        all.firstOrNull { it.id == id } ?: all.first()

    fun perksFor(id: Int): TractorPerks {
        val skin = byId(id)
        return TractorPerks(
            bigTires = skin.bigTires,
            fuelSaver = skin.fuelSaver,
            heavy = skin.heavy,
            twinEngine = skin.twinEngine
        )
    }
}

data class TractorPerks(
    val bigTires: Boolean = false,
    val fuelSaver: Boolean = false,
    val heavy: Boolean = false,
    val twinEngine: Boolean = false
) {
    fun dualTractorFromLevel(): Int = if (twinEngine) LevelCatalog.TWIN_DUAL_LEVEL else LevelCatalog.BASE_DUAL_LEVEL
}
