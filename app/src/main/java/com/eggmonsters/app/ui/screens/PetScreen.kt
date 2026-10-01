package com.eggmonsters.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eggmonsters.app.data.Creature
import com.eggmonsters.app.data.GrowthStage

@Composable
fun PetScreen(
    creature: Creature,
    onFeed: () -> Unit,
    onPlay: () -> Unit,
    onClean: () -> Unit,
    onSleep: () -> Unit,
    onPet: () -> Unit,
    onOpenMissions: () -> Unit = {},
    onOpenSkins: () -> Unit = {}
) {
    val egg = creature.eggType
    val stats = creature.stats

    val infiniteTransition = rememberInfiniteTransition(label = "pet")
    val bounce by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460))
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar: misiones + skins
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onOpenMissions) {
                Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFFFFD54F), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(4.dp))
                Text("Misiones", color = Color(0xFFFFD54F), fontSize = 13.sp)
            }
            TextButton(onClick = onOpenSkins) {
                Icon(Icons.Default.Palette, null, tint = Color(0xFFCE93D8), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(4.dp))
                Text("Skins", color = Color(0xFFCE93D8), fontSize = 13.sp)
            }
        }

        Text(
            text = creature.name,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = egg.primaryColor
        )
        Text(
            text = stageLabel(creature.stage) + "  •  Nivel ${creature.level}  •  ${creature.skin.label}",
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Criatura visual (tocable → skins)
        Box(
            modifier = Modifier
                .size(180.dp)
                .scale(bounce)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(egg.secondaryColor, egg.primaryColor)
                    )
                )
                .clickable { onOpenSkins() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = creature.displayEmoji,
                fontSize = 64.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        StatBar("Hambre", stats.hunger, Color(0xFFFF7043), Icons.Default.Restaurant)
        StatBar("Felicidad", stats.happiness, Color(0xFFFFCA28), Icons.Default.SentimentSatisfied)
        StatBar("Energía", stats.energy, Color(0xFF42A5F5), Icons.Default.Bolt)
        StatBar("Limpieza", stats.cleanliness, Color(0xFF66BB6A), Icons.Default.Soap)
        StatBar("Afecto", stats.affection, Color(0xFFEC407A), Icons.Default.Favorite)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Cuida de tu criatura",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionButton("Comer", Icons.Default.Restaurant, Color(0xFFFF7043), onFeed)
            ActionButton(
                label = if (stats.energy < 10f) "Cansado" else "Jugar",
                icon = Icons.Default.SportsEsports,
                color = if (stats.energy < 10f) Color.Gray else Color(0xFFFFCA28),
                onClick = onPlay
            )
            ActionButton("Limpiar", Icons.Default.Soap, Color(0xFF66BB6A), onClean)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionButton("Dormir", Icons.Default.Bedtime, Color(0xFF42A5F5), onSleep)
            ActionButton("Acariciar", Icons.Default.Favorite, Color(0xFFEC407A), onPet)
        }

        Spacer(modifier = Modifier.height(28.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.08f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Elemento: ${egg.element}",
                    color = egg.secondaryColor,
                    fontSize = 14.sp
                )
                Text(
                    text = egg.personality,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
                if (creature.timesRevived > 0) {
                    Text(
                        text = "Reanimado ${creature.timesRevived}/2 veces",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StatBar(
    label: String,
    value: Float,
    color: Color,
    icon: ImageVector
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${value.toInt()}%",
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { value / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = Color.White.copy(alpha = 0.12f)
        )
    }
}

@Composable
private fun ActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledIconButton(
            onClick = onClick,
            modifier = Modifier.size(64.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = color.copy(alpha = 0.85f)
            )
        ) {
            Icon(icon, label, tint = Color.White, modifier = Modifier.size(28.dp))
        }
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

private fun stageLabel(stage: GrowthStage): String = when (stage) {
    GrowthStage.EGG -> "Huevo"
    GrowthStage.BABY -> "Bebé"
    GrowthStage.TEEN -> "Adolescente"
    GrowthStage.ADULT -> "Adulto"
}
