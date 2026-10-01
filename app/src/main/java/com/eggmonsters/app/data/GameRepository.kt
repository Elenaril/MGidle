package com.eggmonsters.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "eggmonsters_save")

class GameRepository(private val context: Context) {

    private object Keys {
        val HAS_CHOSEN = booleanPreferencesKey("has_chosen")
        val EGG_TYPE = stringPreferencesKey("egg_type")
        val CLICKS = intPreferencesKey("clicks")
        val STEPS = intPreferencesKey("steps")
        val STEPS_BASELINE = intPreferencesKey("steps_baseline")
        val IS_INCUBATING = booleanPreferencesKey("is_incubating")
        val IS_HATCHED = booleanPreferencesKey("is_hatched")
        val CREATURE_NAME = stringPreferencesKey("creature_name")
        val STAGE = stringPreferencesKey("stage")
        val HUNGER = floatPreferencesKey("hunger")
        val HAPPINESS = floatPreferencesKey("happiness")
        val ENERGY = floatPreferencesKey("energy")
        val CLEANLINESS = floatPreferencesKey("cleanliness")
        val AFFECTION = floatPreferencesKey("affection")
        val EXPERIENCE = intPreferencesKey("experience")
        val BIRTH_TIME = longPreferencesKey("birth_time")
        val LAST_UPDATE = longPreferencesKey("last_update")
        val TOTAL_CLICKS = intPreferencesKey("total_clicks")
        val TOTAL_STEPS = intPreferencesKey("total_steps")
        val LAST_SAVE = longPreferencesKey("last_save")
        val IS_ALIVE = booleanPreferencesKey("is_alive")
        val DEATH_TIME = longPreferencesKey("death_time")
        val TIMES_REVIVED = intPreferencesKey("times_revived")
        val EQUIPPED_SKIN = stringPreferencesKey("equipped_skin")
        val UNLOCKED_SKINS = stringPreferencesKey("unlocked_skins")
        val MISSION_DAY = stringPreferencesKey("mission_day")
        val MISSION_DATA = stringPreferencesKey("mission_data")
        val STEPS_TODAY = intPreferencesKey("steps_today")
    }

    val gameStateFlow: Flow<GameState> = context.dataStore.data.map { prefs ->
        prefsToState(prefs)
    }

    suspend fun load(): GameState = prefsToState(context.dataStore.data.first())

    suspend fun save(state: GameState) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HAS_CHOSEN] = state.hasChosenEgg
            prefs[Keys.EGG_TYPE] = state.selectedEgg?.name ?: ""
            prefs[Keys.CLICKS] = state.clicksProgress
            prefs[Keys.STEPS] = state.stepsProgress
            prefs[Keys.STEPS_BASELINE] = state.stepsBaseline
            prefs[Keys.IS_INCUBATING] = state.isIncubating
            prefs[Keys.IS_HATCHED] = state.isHatched
            prefs[Keys.LAST_SAVE] = System.currentTimeMillis()
            prefs[Keys.UNLOCKED_SKINS] = state.unlockedSkins.joinToString(",")

            val m = state.dailyMissions
            prefs[Keys.MISSION_DAY] = m.dayKey
            prefs[Keys.STEPS_TODAY] = m.stepsToday
            // Formato: TYPE:progress:completed:claimed|...
            prefs[Keys.MISSION_DATA] = m.missions.joinToString("|") {
                "${it.type.name}:${it.progress}:${it.completed}:${it.claimed}"
            }

            val c = state.creature
            if (c != null) {
                prefs[Keys.CREATURE_NAME] = c.name
                prefs[Keys.STAGE] = c.stage.name
                prefs[Keys.HUNGER] = c.stats.hunger
                prefs[Keys.HAPPINESS] = c.stats.happiness
                prefs[Keys.ENERGY] = c.stats.energy
                prefs[Keys.CLEANLINESS] = c.stats.cleanliness
                prefs[Keys.AFFECTION] = c.stats.affection
                prefs[Keys.EXPERIENCE] = c.experience
                prefs[Keys.BIRTH_TIME] = c.birthTime
                prefs[Keys.LAST_UPDATE] = c.lastUpdateTime
                prefs[Keys.TOTAL_CLICKS] = c.totalClicks
                prefs[Keys.TOTAL_STEPS] = c.totalSteps
                prefs[Keys.IS_ALIVE] = c.isAlive
                prefs[Keys.DEATH_TIME] = c.deathTime
                prefs[Keys.TIMES_REVIVED] = c.timesRevived
                prefs[Keys.EQUIPPED_SKIN] = c.equippedSkin
            }
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    private fun prefsToState(prefs: Preferences): GameState {
        val hasChosen = prefs[Keys.HAS_CHOSEN] ?: false
        val eggName = prefs[Keys.EGG_TYPE] ?: ""
        val egg = if (eggName.isNotEmpty()) EggType.fromName(eggName) else null

        val isHatched = prefs[Keys.IS_HATCHED] ?: false
        val creature = if (hasChosen && egg != null) {
            Creature(
                eggType = egg,
                name = prefs[Keys.CREATURE_NAME] ?: egg.creatureName,
                stage = try {
                    GrowthStage.valueOf(prefs[Keys.STAGE] ?: GrowthStage.EGG.name)
                } catch (_: Exception) {
                    GrowthStage.EGG
                },
                stats = CreatureStats(
                    hunger = prefs[Keys.HUNGER] ?: 80f,
                    happiness = prefs[Keys.HAPPINESS] ?: 80f,
                    energy = prefs[Keys.ENERGY] ?: 80f,
                    cleanliness = prefs[Keys.CLEANLINESS] ?: 80f,
                    affection = prefs[Keys.AFFECTION] ?: 50f
                ),
                experience = prefs[Keys.EXPERIENCE] ?: 0,
                birthTime = prefs[Keys.BIRTH_TIME] ?: System.currentTimeMillis(),
                lastUpdateTime = prefs[Keys.LAST_UPDATE] ?: System.currentTimeMillis(),
                totalClicks = prefs[Keys.TOTAL_CLICKS] ?: 0,
                totalSteps = prefs[Keys.TOTAL_STEPS] ?: 0,
                isHatched = isHatched,
                isAlive = prefs[Keys.IS_ALIVE] ?: true,
                deathTime = prefs[Keys.DEATH_TIME] ?: 0L,
                timesRevived = prefs[Keys.TIMES_REVIVED] ?: 0,
                equippedSkin = prefs[Keys.EQUIPPED_SKIN] ?: ""
            )
        } else null

        val unlocked = (prefs[Keys.UNLOCKED_SKINS] ?: "")
            .split(",")
            .filter { it.isNotBlank() }
            .toSet()

        val dayKey = prefs[Keys.MISSION_DAY] ?: DailyMissionsState.todayKey()
        val missionRaw = prefs[Keys.MISSION_DATA] ?: ""
        val missions = if (missionRaw.isNotEmpty()) {
            missionRaw.split("|").mapNotNull { part ->
                val bits = part.split(":")
                if (bits.size < 4) return@mapNotNull null
                try {
                    DailyMission(
                        type = MissionType.valueOf(bits[0]),
                        progress = bits[1].toInt(),
                        completed = bits[2].toBoolean(),
                        claimed = bits[3].toBoolean()
                    )
                } catch (_: Exception) {
                    null
                }
            }
        } else DailyMissionsState.defaultMissions()

        val daily = DailyMissionsState(
            dayKey = dayKey,
            missions = if (missions.isEmpty()) DailyMissionsState.defaultMissions() else missions,
            stepsToday = prefs[Keys.STEPS_TODAY] ?: 0
        ).ensureToday()

        return GameState(
            hasChosenEgg = hasChosen,
            selectedEgg = egg,
            clicksProgress = prefs[Keys.CLICKS] ?: 0,
            stepsProgress = prefs[Keys.STEPS] ?: 0,
            stepsBaseline = prefs[Keys.STEPS_BASELINE] ?: -1,
            creature = creature,
            isIncubating = prefs[Keys.IS_INCUBATING] ?: false,
            isHatched = isHatched,
            lastSaveTime = prefs[Keys.LAST_SAVE] ?: System.currentTimeMillis(),
            dailyMissions = daily,
            unlockedSkins = unlocked,
            isLoading = false
        )
    }
}
