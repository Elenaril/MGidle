package com.eggmonsters.app.data

enum class GrowthStage {
    EGG,
    BABY,
    TEEN,
    ADULT
}

/** Skins / apariencia visual según elemento y etapa */
enum class CreatureSkin(val emoji: String, val label: String) {
    // Fuego
    FIRE_BABY("🐣🔥", "Cría ígnea"),
    FIRE_TEEN("🦎🔥", "Dragón joven"),
    FIRE_ADULT("🐉", "Pyrodrake"),
    // Agua
    WATER_BABY("🐣💧", "Cría acuática"),
    WATER_TEEN("🐟", "Alevín"),
    WATER_ADULT("🧜", "Aqualing"),
    // Naturaleza
    NATURE_BABY("🐣🌿", "Brote"),
    NATURE_TEEN("🦊🌿", "Zorro hoja"),
    NATURE_ADULT("🦊✨", "Florafox"),
    // Cyber
    CYBER_BABY("🤖", "Proto-bit"),
    CYBER_TEEN("🦾", "Mecha junior"),
    CYBER_ADULT("👾", "Bytebit"),
    // Sombra
    SHADOW_BABY("🌑🐱", "Sombra bebé"),
    SHADOW_TEEN("🦇", "Noctámbulo"),
    SHADOW_ADULT("😈", "Umbra"),
    // Luz
    STAR_BABY("⭐🐣", "Estrella bebé"),
    STAR_TEEN("🌟", "Luz joven"),
    STAR_ADULT("🦄", "Lumina");

    companion object {
        fun forCreature(egg: EggType, stage: GrowthStage): CreatureSkin {
            return when (egg) {
                EggType.FIRE -> when (stage) {
                    GrowthStage.BABY -> FIRE_BABY
                    GrowthStage.TEEN -> FIRE_TEEN
                    else -> FIRE_ADULT
                }
                EggType.WATER -> when (stage) {
                    GrowthStage.BABY -> WATER_BABY
                    GrowthStage.TEEN -> WATER_TEEN
                    else -> WATER_ADULT
                }
                EggType.NATURE -> when (stage) {
                    GrowthStage.BABY -> NATURE_BABY
                    GrowthStage.TEEN -> NATURE_TEEN
                    else -> NATURE_ADULT
                }
                EggType.CYBER -> when (stage) {
                    GrowthStage.BABY -> CYBER_BABY
                    GrowthStage.TEEN -> CYBER_TEEN
                    else -> CYBER_ADULT
                }
                EggType.SHADOW -> when (stage) {
                    GrowthStage.BABY -> SHADOW_BABY
                    GrowthStage.TEEN -> SHADOW_TEEN
                    else -> SHADOW_ADULT
                }
                EggType.STAR -> when (stage) {
                    GrowthStage.BABY -> STAR_BABY
                    GrowthStage.TEEN -> STAR_TEEN
                    else -> STAR_ADULT
                }
            }
        }
    }
}

data class CreatureStats(
    val hunger: Float = 80f,
    val happiness: Float = 80f,
    val energy: Float = 80f,
    val cleanliness: Float = 80f,
    val affection: Float = 50f
) {
    fun isCritical(): Boolean =
        hunger <= 15f || happiness <= 15f || energy <= 10f || cleanliness <= 10f

    fun isDead(): Boolean =
        hunger <= 0f && happiness <= 0f
}

data class Creature(
    val eggType: EggType,
    val name: String = eggType.creatureName,
    val stage: GrowthStage = GrowthStage.EGG,
    val stats: CreatureStats = CreatureStats(),
    val experience: Int = 0,
    val birthTime: Long = System.currentTimeMillis(),
    val lastUpdateTime: Long = System.currentTimeMillis(),
    val totalClicks: Int = 0,
    val totalSteps: Int = 0,
    val isHatched: Boolean = false,
    val isAlive: Boolean = true,
    val deathTime: Long = 0L,
    val timesRevived: Int = 0,
    val equippedSkin: String = "" // vacío = automático por etapa
) {
    val level: Int
        get() = (experience / 100) + 1

    val skin: CreatureSkin
        get() {
            if (equippedSkin.isNotEmpty()) {
                try {
                    return CreatureSkin.valueOf(equippedSkin)
                } catch (_: Exception) { /* fallback */ }
            }
            return CreatureSkin.forCreature(eggType, stage)
        }

    val displayEmoji: String
        get() = if (!isAlive) "💀" else skin.emoji

    fun canEvolve(): Boolean = isAlive && when (stage) {
        GrowthStage.BABY -> stats.affection >= 60f && experience >= 150
        GrowthStage.TEEN -> stats.affection >= 80f && experience >= 400
        else -> false
    }

    fun nextStage(): GrowthStage = when (stage) {
        GrowthStage.EGG -> GrowthStage.BABY
        GrowthStage.BABY -> GrowthStage.TEEN
        GrowthStage.TEEN -> GrowthStage.ADULT
        GrowthStage.ADULT -> GrowthStage.ADULT
    }

    /** Máximo 2 reanimaciones por criatura */
    fun canRevive(): Boolean = !isAlive && timesRevived < 2
}
