package com.eggmonsters.app.data

/**
 * Estado global del juego. Persistido con DataStore.
 */
data class GameState(
    val hasChosenEgg: Boolean = false,
    val selectedEgg: EggType? = null,
    val clicksProgress: Int = 0,
    val stepsProgress: Int = 0,
    val stepsBaseline: Int = -1,
    val creature: Creature? = null,
    val isIncubating: Boolean = false,
    val isHatched: Boolean = false,
    val lastSaveTime: Long = System.currentTimeMillis(),
    val dailyMissions: DailyMissionsState = DailyMissionsState(),
    val unlockedSkins: Set<String> = emptySet(),
    // UI temporal (no se guarda)
    val showMiniGame: Boolean = false,
    val miniGameType: Int = 0, // 0 = reacción, 1 = memoria
    val showMissions: Boolean = false,
    val showSkinPicker: Boolean = false,
    val isLoading: Boolean = true
) {
    val isReadyToHatch: Boolean
        get() = clicksProgress >= 1000 && stepsProgress >= 1000

    val incubationPercent: Float
        get() {
            val clickPct = (clicksProgress.coerceAtMost(1000) / 1000f) * 50f
            val stepPct = (stepsProgress.coerceAtMost(1000) / 1000f) * 50f
            return clickPct + stepPct
        }

    val isCreatureDead: Boolean
        get() = creature != null && !creature.isAlive
}
