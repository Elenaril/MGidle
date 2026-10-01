package com.eggmonsters.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eggmonsters.app.data.EggType
import com.eggmonsters.app.data.GameState

@Composable
fun IncubationScreen(
    state: GameState,
    onEggClick: () -> Unit
) {
    val egg = state.selectedEgg ?: return

    // Animación de "respiración" del huevo
    val infiniteTransition = rememberInfiniteTransition(label = "egg")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Escala extra al hacer clic
    var clickScale by remember { mutableFloatStateOf(1f) }
    LaunchedEffect(state.clicksProgress) {
        clickScale = 0.92f
        kotlinx.coroutines.delay(80)
        clickScale = 1f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460))
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = egg.displayName,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = egg.primaryColor
        )

        Text(
            text = "Toca el huevo y camina para incubarlo",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        // ───── Huevo interactivo ─────
        Box(
            modifier = Modifier
                .size(220.dp)
                .scale(pulse * clickScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            egg.secondaryColor,
                            egg.primaryColor,
                            egg.primaryColor.copy(alpha = 0.7f)
                        )
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onEggClick() },
            contentAlignment = Alignment.Center
        ) {
            // Grietas según progreso
            val crackLevel = (state.incubationPercent / 20).toInt() // 0-5
            Text(
                text = when {
                    crackLevel >= 5 -> "💥"
                    crackLevel >= 3 -> "🥚✨"
                    else -> egg.emoji
                },
                fontSize = 72.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ───── Barras de progreso ─────
        ProgressCard(
            icon = Icons.Default.TouchApp,
            label = "Clics",
            current = state.clicksProgress,
            max = 1000,
            color = egg.primaryColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        ProgressCard(
            icon = Icons.Default.DirectionsWalk,
            label = "Pasos reales",
            current = state.stepsProgress,
            max = 1000,
            color = Color(0xFF4CAF50)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Porcentaje total
        Text(
            text = "${state.incubationPercent.toInt()}% incubado",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )

        if (state.isReadyToHatch) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "¡Está a punto de nacer!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD54F)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Camina con el móvil en el bolsillo.\nLos pasos se cuentan automáticamente.",
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProgressCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    current: Int,
    max: Int,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "$current / $max",
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { current / max.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = color,
                trackColor = Color.White.copy(alpha = 0.15f)
            )
        }
    }
}
