package com.eggmonsters.app.data

import androidx.compose.ui.graphics.Color

enum class EggType(
    val displayName: String,
    val creatureName: String,
    val element: String,
    val personality: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val emoji: String
) {
    FIRE(
        displayName = "Huevo Ígneo",
        creatureName = "Pyrodrake",
        element = "Fuego",
        personality = "Temperamental y valiente",
        primaryColor = Color(0xFFFF5722),
        secondaryColor = Color(0xFFFFAB91),
        emoji = "🔥"
    ),
    WATER(
        displayName = "Huevo Acuático",
        creatureName = "Aqualing",
        element = "Agua",
        personality = "Tranquilo y sabio",
        primaryColor = Color(0xFF2196F3),
        secondaryColor = Color(0xFF90CAF9),
        emoji = "💧"
    ),
    NATURE(
        displayName = "Huevo Natural",
        creatureName = "Florafox",
        element = "Naturaleza",
        personality = "Curioso y juguetón",
        primaryColor = Color(0xFF4CAF50),
        secondaryColor = Color(0xFFA5D6A7),
        emoji = "🌿"
    ),
    CYBER(
        displayName = "Huevo Cibernético",
        creatureName = "Bytebit",
        element = "Tecnología",
        personality = "Inteligente y analítico",
        primaryColor = Color(0xFF9C27B0),
        secondaryColor = Color(0xFFCE93D8),
        emoji = "⚡"
    ),
    SHADOW(
        displayName = "Huevo Sombrío",
        creatureName = "Umbra",
        element = "Oscuridad",
        personality = "Misterioso y leal",
        primaryColor = Color(0xFF424242),
        secondaryColor = Color(0xFF9E9E9E),
        emoji = "🌑"
    ),
    STAR(
        displayName = "Huevo Estelar",
        creatureName = "Lumina",
        element = "Luz",
        personality = "Alegre y optimista",
        primaryColor = Color(0xFFFFC107),
        secondaryColor = Color(0xFFFFECB3),
        emoji = "✨"
    );

    companion object {
        fun fromName(name: String): EggType? =
            entries.find { it.name == name }
    }
}
