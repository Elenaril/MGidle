package com.eggmonsters.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eggmonsters.app.util.SoundHelper
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Minijuego de reacción: aparece un objetivo en posición aleatoria.
 * Toca lo más rápido posible. 10 rondas → puntuación 0-100.
 */
@Composable
fun MiniGameScreen(
    creatureEmoji: String,
    primaryColor: Color,
    onFinished: (score: Int) -> Unit,
    onCancel: () -> Unit
) {
    var round by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var showTarget by remember { mutableStateOf(false) }
    var targetX by remember { mutableFloatStateOf(0.5f) } // 0..1
    var targetY by remember { mutableFloatStateOf(0.5f) }
    var waiting by remember { mutableStateOf(true) }
    var gameOver by remember { mutableStateOf(false) }
    var reactionStart by remember { mutableLongStateOf(0L) }
    var feedback by remember { mutableStateOf("") }

    val totalRounds = 10

    LaunchedEffect(round, gameOver) {
        if (gameOver) return@LaunchedEffect
        if (round >= totalRounds) {
            gameOver = true
            delay(1400)
            onFinished(score.coerceIn(0, 100))
            return@LaunchedEffect
        }

        waiting = true
        showTarget = false
        feedback = ""
        delay(Random.nextLong(700, 1700))

        if (gameOver) return@LaunchedEffect
        targetX = Random.nextFloat() * 0.65f + 0.1f
        targetY = Random.nextFloat() * 0.55f + 0.15f
        reactionStart = System.currentTimeMillis()
        showTarget = true
        waiting = false
    }

    fun onTargetHit() {
        if (!showTarget || gameOver) return
        val reactionMs = System.currentTimeMillis() - reactionStart
        showTarget = false

        val points = when {
            reactionMs < 350 -> 10
            reactionMs < 550 -> 8
            reactionMs < 800 -> 6
            reactionMs < 1200 -> 4
            else -> 2
        }
        score += points
        feedback = when {
            points >= 10 -> "¡Perfecto! +$points"
            points >= 8 -> "¡Genial! +$points"
            points >= 6 -> "Bien +$points"
            else -> "+$points"
        }
        SoundHelper.playMiniGameHit()
        round++
    }

    fun onMiss() {
        if (!showTarget || gameOver) return
        showTarget = false
        feedback = "Fallaste..."
        SoundHelper.playMiniGameMiss()
        round++
    }

    val pulse by rememberInfiniteTransition(label = "target").animateFloat(
        initialValue = 0.92f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0D1B2A), Color(0xFF1B263B), Color(0xFF415A77))
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
            }
            Text(
                text = "Ronda ${round.coerceAtMost(totalRounds)}/$totalRounds",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Pts: $score",
                color = primaryColor,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when {
                    gameOver -> "¡Terminaste!"
                    waiting -> "Prepárate..."
                    showTarget -> "¡TÓCALO!"
                    else -> feedback.ifEmpty { " " }
                },
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        // Área de juego
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 110.dp, bottom = 24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onMiss() }
        ) {
            val density = LocalDensity.current
            val maxW = constraints.maxWidth
            val maxH = constraints.maxHeight
            val targetSizePx = with(density) { 88.dp.toPx() }

            if (showTarget) {
                val x = (targetX * (maxW - targetSizePx)).roundToInt()
                val y = (targetY * (maxH - targetSizePx)).roundToInt()

                Box(
                    modifier = Modifier
                        .offset { IntOffset(x, y) }
                        .scale(pulse)
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(primaryColor, primaryColor.copy(alpha = 0.55f))
                            )
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTargetHit() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = creatureEmoji, fontSize = 38.sp)
                }
            }
        }

        if (gameOver) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B263B)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Puntuación final",
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Text(
                        text = "$score",
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                    Text(
                        text = when {
                            score >= 80 -> "¡Increíble!"
                            score >= 50 -> "¡Muy bien!"
                            else -> "Sigue practicando"
                        },
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
