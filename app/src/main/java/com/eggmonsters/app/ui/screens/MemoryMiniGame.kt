package com.eggmonsters.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eggmonsters.app.util.SoundHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Minijuego de memoria: 4x3 = 6 parejas.
 * Puntuación según tiempo y fallos.
 */
@Composable
fun MemoryMiniGame(
    primaryColor: Color,
    onFinished: (score: Int) -> Unit,
    onCancel: () -> Unit
) {
    val emojis = listOf("🔥", "💧", "🌿", "⚡", "🌑", "✨")
    val pairs = remember { (emojis + emojis).shuffled() }

    var flipped by remember { mutableStateOf(setOf<Int>()) }
    var matched by remember { mutableStateOf(setOf<Int>()) }
    var firstPick by remember { mutableStateOf<Int?>(null) }
    var lock by remember { mutableStateOf(false) }
    var moves by remember { mutableIntStateOf(0) }
    var startTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var finished by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun onCardTap(index: Int) {
        if (lock || index in matched || index in flipped || finished) return

        val newFlipped = flipped + index
        flipped = newFlipped

        if (firstPick == null) {
            firstPick = index
        } else {
            lock = true
            moves++
            val a = firstPick!!
            val b = index
            if (pairs[a] == pairs[b]) {
                matched = matched + a + b
                SoundHelper.playMiniGameHit()
                flipped = emptySet()
                firstPick = null
                lock = false
                if (matched.size == pairs.size) {
                    finished = true
                    val elapsed = (System.currentTimeMillis() - startTime) / 1000f
                    // Mejor score: pocos movimientos y rápido
                    val moveScore = (30 - moves).coerceIn(0, 25)
                    val timeScore = (40 - elapsed.toInt()).coerceIn(0, 40)
                    val score = (35 + moveScore + timeScore).coerceIn(20, 100)
                    scope.launch {
                        delay(800)
                        onFinished(score)
                    }
                }
            } else {
                SoundHelper.playMiniGameMiss()
                scope.launch {
                    delay(700)
                    flipped = emptySet()
                    firstPick = null
                    lock = false
                }
            }
        }
    }

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
                Icon(Icons.Default.Close, null, tint = Color.White)
            }
            Text("Memoria", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Mov: $moves", color = primaryColor, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 72.dp, start = 16.dp, end = 16.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (finished) "¡Completado!" else "Encuentra las parejas",
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val rows = 3
            val cols = 4
            for (r in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (c in 0 until cols) {
                        val index = r * cols + c
                        val isFaceUp = index in flipped || index in matched
                        val scale by animateFloatAsState(
                            targetValue = if (isFaceUp) 1f else 0.95f,
                            label = "scale"
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.85f)
                                .scale(scale)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (index in matched) primaryColor.copy(alpha = 0.5f)
                                    else if (isFaceUp) Color.White.copy(alpha = 0.2f)
                                    else Color.White.copy(alpha = 0.1f)
                                )
                                .clickable { onCardTap(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isFaceUp) pairs[index] else "❓",
                                fontSize = 28.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
