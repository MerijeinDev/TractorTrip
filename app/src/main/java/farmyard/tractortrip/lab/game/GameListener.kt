package farmyard.tractortrip.lab.game

interface GameListener {
    fun onHudUpdate(snapshot: HudSnapshot)
    fun onGameFinished(result: GameResult)
    fun onMaterialCollected()
    fun onDistanceTraveled(cellsMoved: Int)
    fun onFuelCollected()
    fun onCraneBlocked()
    fun onDamageTaken()
    fun onTurnInput()
    fun onTractorSwitched()
}
