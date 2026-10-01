package com.eggmonsters.app.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.eggmonsters.app.data.*
import com.eggmonsters.app.util.NotificationHelper
import com.eggmonsters.app.util.SoundHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application)
    private val _uiState = MutableStateFlow(GameState(isLoading = true))
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    private val sensorManager =
        application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private var decayJob: Job? = null
    private var saveJob: Job? = null
    private var notifyJob: Job? = null
    private var isListeningSteps = false
    private var lastNotifiedHunger = false
    private var lastNotifiedEnergy = false
    private var lastNotifiedDirty = false
    private var dailyStepsBaseline = -1

    private val stepListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event == null) return
            val totalStepsFromBoot = event.values[0].toInt()
            val state = _uiState.value

            if (state.isIncubating && !state.isHatched) {
                if (state.stepsBaseline < 0) {
                    _uiState.update { it.copy(stepsBaseline = totalStepsFromBoot) }
                    scheduleSave()
                } else {
                    val stepsSinceStart = (totalStepsFromBoot - state.stepsBaseline).coerceAtLeast(0)
                    val newProgress = stepsSinceStart.coerceAtMost(1000)
                    if (newProgress != state.stepsProgress) {
                        _uiState.update { it.copy(stepsProgress = newProgress) }
                        scheduleSave()
                        checkHatch()
                    }
                }
            }

            if (state.isHatched && state.creature?.isAlive == true) {
                if (dailyStepsBaseline < 0) {
                    dailyStepsBaseline = totalStepsFromBoot
                } else {
                    val todaySteps = (totalStepsFromBoot - dailyStepsBaseline).coerceAtLeast(0)
                    if (todaySteps != state.dailyMissions.stepsToday) {
                        _uiState.update {
                            it.copy(dailyMissions = it.dailyMissions.copy(stepsToday = todaySteps))
                        }
                        bumpMission(MissionType.STEPS, todaySteps, absolute = true)
                    }
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    init {
        NotificationHelper.createChannel(application)
        loadGame()
    }

    private fun loadGame() {
        viewModelScope.launch {
            val loaded = repository.load()
            val restored = applyOfflineDecay(loaded).let {
                it.copy(dailyMissions = it.dailyMissions.ensureToday())
            }
            _uiState.value = restored.copy(isLoading = false)

            if (restored.isIncubating && !restored.isHatched) {
                startStepListening()
            }
            if (restored.isHatched && restored.creature?.isAlive == true) {
                startStepListening()
                startStatDecay()
                startNotificationWatcher()
            }
        }
    }

    private fun applyOfflineDecay(state: GameState): GameState {
        val creature = state.creature ?: return state
        if (!creature.isHatched || !creature.isAlive) return state

        val now = System.currentTimeMillis()
        val minutesPassed = ((now - creature.lastUpdateTime) / 60_000f).coerceAtLeast(0f)
        if (minutesPassed < 1f) return state

        val newStats = creature.stats.copy(
            hunger = (creature.stats.hunger - 1.2f * minutesPassed).coerceAtLeast(0f),
            happiness = (creature.stats.happiness - 0.8f * minutesPassed).coerceAtLeast(0f),
            energy = (creature.stats.energy - 0.6f * minutesPassed).coerceAtLeast(0f),
            cleanliness = (creature.stats.cleanliness - 0.7f * minutesPassed).coerceAtLeast(0f)
        )

        var updated = creature.copy(stats = newStats, lastUpdateTime = now)
        if (newStats.isDead()) {
            updated = updated.copy(isAlive = false, deathTime = now)
        }
        return state.copy(creature = updated)
    }

    private fun scheduleSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(400)
            repository.save(_uiState.value)
        }
    }

    fun saveNow() {
        viewModelScope.launch { repository.save(_uiState.value) }
    }

    fun selectEgg(eggType: EggType) {
        _uiState.update {
            it.copy(
                hasChosenEgg = true,
                selectedEgg = eggType,
                isIncubating = true,
                clicksProgress = 0,
                stepsProgress = 0,
                stepsBaseline = -1,
                creature = Creature(eggType = eggType),
                isHatched = false,
                dailyMissions = DailyMissionsState()
            )
        }
        startStepListening()
        scheduleSave()
        SoundHelper.playSuccess()
    }

    fun onEggClicked() {
        val state = _uiState.value
        if (!state.isIncubating || state.isHatched) return
        if (state.clicksProgress >= 1000) return

        val newClicks = (state.clicksProgress + 1).coerceAtMost(1000)
        _uiState.update { it.copy(clicksProgress = newClicks) }
        SoundHelper.playClick()
        if (newClicks % 50 == 0) vibrate(30)
        scheduleSave()
        checkHatch()
    }

    private fun startStepListening() {
        if (isListeningSteps || stepSensor == null) return
        sensorManager.registerListener(stepListener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        isListeningSteps = true
    }

    private fun stopStepListening() {
        if (!isListeningSteps) return
        sensorManager.unregisterListener(stepListener)
        isListeningSteps = false
    }

    private fun checkHatch() {
        val state = _uiState.value
        if (state.isReadyToHatch && !state.isHatched) hatch()
    }

    private fun hatch() {
        stopStepListening()
        vibrate(500)
        SoundHelper.playHatch()

        val current = _uiState.value.creature ?: return
        val hatched = current.copy(
            stage = GrowthStage.BABY,
            isHatched = true,
            birthTime = System.currentTimeMillis(),
            lastUpdateTime = System.currentTimeMillis()
        )
        val babySkin = CreatureSkin.forCreature(hatched.eggType, GrowthStage.BABY)

        _uiState.update {
            it.copy(
                isHatched = true,
                isIncubating = false,
                creature = hatched,
                unlockedSkins = it.unlockedSkins + babySkin.name
            )
        }
        scheduleSave()
        startStepListening()
        startStatDecay()
        startNotificationWatcher()
    }

    fun feed() {
        if (!isAlive()) return
        updateStats {
            it.copy(
                hunger = (it.hunger + 25f).coerceAtMost(100f),
                happiness = (it.happiness + 5f).coerceAtMost(100f)
            )
        }
        addExperience(8)
        bumpMission(MissionType.FEED, 1)
        SoundHelper.playFeed()
        vibrate(40)
    }

    fun openMiniGame() {
        if (!isAlive()) return
        val energy = _uiState.value.creature?.stats?.energy ?: 0f
        if (energy < 10f) return
        val type = Random.nextInt(2)
        _uiState.update { it.copy(showMiniGame = true, miniGameType = type) }
    }

    fun closeMiniGame() {
        _uiState.update { it.copy(showMiniGame = false) }
    }

    fun onMiniGameFinished(score: Int) {
        _uiState.update { it.copy(showMiniGame = false) }
        if (!isAlive()) return

        val happinessGain = (score * 0.25f).coerceIn(5f, 30f)
        val energyCost = 12f
        val affectionGain = (score * 0.08f).coerceIn(2f, 10f)
        val exp = (score / 5).coerceIn(5, 25)

        updateStats {
            it.copy(
                happiness = (it.happiness + happinessGain).coerceAtMost(100f),
                energy = (it.energy - energyCost).coerceAtLeast(0f),
                affection = (it.affection + affectionGain).coerceAtMost(100f)
            )
        }
        addExperience(exp)
        bumpMission(MissionType.PLAY, 1)
        checkHappinessMission()
        SoundHelper.playPlay()
        if (score >= 70) vibrate(80)
    }

    fun clean() {
        if (!isAlive()) return
        updateStats {
            it.copy(
                cleanliness = (it.cleanliness + 30f).coerceAtMost(100f),
                happiness = (it.happiness + 8f).coerceAtMost(100f)
            )
        }
        addExperience(6)
        bumpMission(MissionType.CLEAN, 1)
        SoundHelper.playClean()
        vibrate(40)
    }

    fun sleep() {
        if (!isAlive()) return
        updateStats {
            it.copy(
                energy = (it.energy + 40f).coerceAtMost(100f),
                hunger = (it.hunger - 5f).coerceAtLeast(0f)
            )
        }
        addExperience(5)
        SoundHelper.playSleep()
    }

    fun pet() {
        if (!isAlive()) return
        updateStats {
            it.copy(
                affection = (it.affection + 6f).coerceAtMost(100f),
                happiness = (it.happiness + 10f).coerceAtMost(100f)
            )
        }
        addExperience(4)
        bumpMission(MissionType.PET, 1)
        checkHappinessMission()
        SoundHelper.playPet()
        vibrate(50)
    }

    private fun isAlive(): Boolean = _uiState.value.creature?.isAlive == true

    private fun updateStats(transform: (CreatureStats) -> CreatureStats) {
        _uiState.update { state ->
            val creature = state.creature ?: return@update state
            if (!creature.isAlive) return@update state
            val newStats = transform(creature.stats)
            var updated = creature.copy(
                stats = newStats,
                lastUpdateTime = System.currentTimeMillis()
            )
            if (newStats.isDead()) {
                updated = updated.copy(isAlive = false, deathTime = System.currentTimeMillis())
            }
            state.copy(creature = updated)
        }
        scheduleSave()
        checkEvolution()
    }

    private fun addExperience(amount: Int) {
        _uiState.update { state ->
            val creature = state.creature ?: return@update state
            if (!creature.isAlive) return@update state
            state.copy(creature = creature.copy(experience = creature.experience + amount))
        }
        scheduleSave()
        checkEvolution()
    }

    private fun checkEvolution() {
        val creature = _uiState.value.creature ?: return
        if (creature.canEvolve()) {
            val newStage = creature.nextStage()
            val evolved = creature.copy(stage = newStage, experience = 0)
            val newSkin = CreatureSkin.forCreature(evolved.eggType, newStage)
            _uiState.update {
                it.copy(
                    creature = evolved,
                    unlockedSkins = it.unlockedSkins + newSkin.name
                )
            }
            scheduleSave()
            vibrate(600)
            SoundHelper.playEvolve()
            NotificationHelper.notifyReadyToEvolve(getApplication(), evolved.name)
        }
    }

    fun revive() {
        val creature = _uiState.value.creature ?: return
        if (!creature.canRevive()) return

        val revived = creature.copy(
            isAlive = true,
            timesRevived = creature.timesRevived + 1,
            stats = CreatureStats(
                hunger = 50f,
                happiness = 50f,
                energy = 50f,
                cleanliness = 50f,
                affection = creature.stats.affection.coerceAtLeast(30f)
            ),
            lastUpdateTime = System.currentTimeMillis()
        )
        _uiState.update { it.copy(creature = revived) }
        scheduleSave()
        startStatDecay()
        startNotificationWatcher()
        startStepListening()
        SoundHelper.playSuccess()
        vibrate(300)
    }

    fun startOver() {
        viewModelScope.launch {
            repository.clear()
            stopStepListening()
            decayJob?.cancel()
            notifyJob?.cancel()
            dailyStepsBaseline = -1
            _uiState.value = GameState(isLoading = false)
        }
    }

    fun toggleMissions() {
        _uiState.update { it.copy(showMissions = !it.showMissions) }
    }

    fun claimMission(mission: DailyMission) {
        if (!mission.isDone || mission.claimed) return
        addExperience(mission.type.rewardXp)
        updateStats {
            it.copy(affection = (it.affection + mission.type.rewardAffection).coerceAtMost(100f))
        }
        _uiState.update { state ->
            val updated = state.dailyMissions.missions.map {
                if (it.type == mission.type) it.copy(claimed = true, completed = true) else it
            }
            state.copy(dailyMissions = state.dailyMissions.copy(missions = updated))
        }
        scheduleSave()
        SoundHelper.playSuccess()
    }

    private fun bumpMission(type: MissionType, amount: Int, absolute: Boolean = false) {
        _uiState.update { state ->
            val missions = state.dailyMissions.missions.map { m ->
                if (m.type != type || m.claimed) return@map m
                val newProgress = if (absolute) amount else m.progress + amount
                m.copy(
                    progress = newProgress,
                    completed = newProgress >= m.type.target
                )
            }
            state.copy(dailyMissions = state.dailyMissions.copy(missions = missions))
        }
        scheduleSave()
    }

    private fun checkHappinessMission() {
        val happiness = _uiState.value.creature?.stats?.happiness ?: 0f
        bumpMission(MissionType.HAPPINESS, happiness.toInt(), absolute = true)
    }

    fun toggleSkinPicker() {
        _uiState.update { it.copy(showSkinPicker = !it.showSkinPicker) }
    }

    fun equipSkin(skin: CreatureSkin?) {
        _uiState.update { state ->
            val c = state.creature ?: return@update state
            state.copy(
                creature = c.copy(equippedSkin = skin?.name ?: ""),
                showSkinPicker = false
            )
        }
        scheduleSave()
        SoundHelper.playClick()
    }

    private fun startStatDecay() {
        decayJob?.cancel()
        decayJob = viewModelScope.launch {
            while (isActive) {
                delay(60_000)
                applyDecay()
            }
        }
    }

    private fun applyDecay() {
        _uiState.update { state ->
            val creature = state.creature ?: return@update state
            if (!creature.isHatched || !creature.isAlive) return@update state

            val now = System.currentTimeMillis()
            val minutesPassed = ((now - creature.lastUpdateTime) / 60_000f).coerceAtLeast(0f)
            val newStats = creature.stats.copy(
                hunger = (creature.stats.hunger - 1.2f * minutesPassed).coerceAtLeast(0f),
                happiness = (creature.stats.happiness - 0.8f * minutesPassed).coerceAtLeast(0f),
                energy = (creature.stats.energy - 0.6f * minutesPassed).coerceAtLeast(0f),
                cleanliness = (creature.stats.cleanliness - 0.7f * minutesPassed).coerceAtLeast(0f)
            )
            var updated = creature.copy(stats = newStats, lastUpdateTime = now)
            if (newStats.isDead()) {
                updated = updated.copy(isAlive = false, deathTime = now)
            }
            state.copy(creature = updated)
        }
        scheduleSave()
    }

    private fun startNotificationWatcher() {
        notifyJob?.cancel()
        notifyJob = viewModelScope.launch {
            while (isActive) {
                delay(90_000)
                checkAndNotify()
            }
        }
    }

    private fun checkAndNotify() {
        val creature = _uiState.value.creature ?: return
        if (!creature.isAlive || !creature.isHatched) return
        val ctx = getApplication<Application>()
        val s = creature.stats

        if (s.hunger <= 20f && !lastNotifiedHunger) {
            NotificationHelper.notifyHungry(ctx, creature.name)
            lastNotifiedHunger = true
        } else if (s.hunger > 35f) lastNotifiedHunger = false

        if (s.energy <= 15f && !lastNotifiedEnergy) {
            NotificationHelper.notifyLowEnergy(ctx, creature.name)
            lastNotifiedEnergy = true
        } else if (s.energy > 30f) lastNotifiedEnergy = false

        if (s.cleanliness <= 20f && !lastNotifiedDirty) {
            NotificationHelper.notifyDirty(ctx, creature.name)
            lastNotifiedDirty = true
        } else if (s.cleanliness > 40f) lastNotifiedDirty = false
    }

    private fun vibrate(durationMs: Long) {
        val context = getApplication<Application>()
        val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMs)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopStepListening()
        decayJob?.cancel()
        saveJob?.cancel()
        notifyJob?.cancel()
        SoundHelper.release()
    }
}
