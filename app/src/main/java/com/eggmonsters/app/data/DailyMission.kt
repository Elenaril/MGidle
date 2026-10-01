package com.eggmonsters.app.data

import java.util.Calendar

enum class MissionType(
    val title: String,
    val description: String,
    val target: Int,
    val rewardXp: Int,
    val rewardAffection: Float
) {
    FEED("Alimentar", "Alimenta a tu criatura 3 veces", 3, 20, 5f),
    PLAY("Jugar", "Completa 1 minijuego", 1, 25, 8f),
    CLEAN("Limpiar", "Limpia a tu criatura 2 veces", 2, 15, 4f),
    PET("Acariciar", "Acaricia 5 veces", 5, 15, 10f),
    HAPPINESS("Felicidad", "Llega a 90 de felicidad", 90, 30, 6f),
    STEPS("Caminar", "Camina 500 pasos hoy (app abierta)", 500, 35, 5f)
}

data class DailyMission(
    val type: MissionType,
    val progress: Int = 0,
    val completed: Boolean = false,
    val claimed: Boolean = false
) {
    val isDone: Boolean
        get() = progress >= type.target || completed
}

data class DailyMissionsState(
    val dayKey: String = todayKey(), // "2026-09-30"
    val missions: List<DailyMission> = defaultMissions(),
    val stepsToday: Int = 0
) {
    companion object {
        fun todayKey(): String {
            val c = Calendar.getInstance()
            return "${c.get(Calendar.YEAR)}-${c.get(Calendar.MONTH) + 1}-${c.get(Calendar.DAY_OF_MONTH)}"
        }

        fun defaultMissions(): List<DailyMission> {
            // 4 misiones del día (fijas para simplicidad; se pueden rotar)
            return listOf(
                DailyMission(MissionType.FEED),
                DailyMission(MissionType.PLAY),
                DailyMission(MissionType.PET),
                DailyMission(MissionType.HAPPINESS)
            )
        }
    }

    fun ensureToday(): DailyMissionsState {
        val today = todayKey()
        return if (dayKey == today) this
        else DailyMissionsState(dayKey = today) // reset diario
    }
}
