package com.eggmonsters.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eggmonsters.app.data.DailyMission
import com.eggmonsters.app.data.DailyMissionsState

@Composable
fun MissionsPanel(
    missionsState: DailyMissionsState,
    onClaim: (DailyMission) -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .align(Alignment.Center),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B263B)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFFFFD54F))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Misiones del día",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }
                }

                Text(
                    text = "Se reinician cada día",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                missionsState.missions.forEach { mission ->
                    MissionRow(mission, onClaim)
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun MissionRow(
    mission: DailyMission,
    onClaim: (DailyMission) -> Unit
) {
    val progress = mission.progress.coerceAtMost(mission.type.target)
    val pct = progress / mission.type.target.toFloat()

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (mission.claimed) Color(0xFF2E4A3E)
            else Color.White.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        mission.type.title,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        mission.type.description,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
                Text(
                    "$progress/${mission.type.target}",
                    color = Color(0xFFFFD54F),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { pct },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = Color(0xFFFFD54F),
                trackColor = Color.White.copy(alpha = 0.15f)
            )

            if (mission.isDone && !mission.claimed) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { onClaim(mission) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Reclamar +${mission.type.rewardXp} XP / +${mission.type.rewardAffection.toInt()} Afecto",
                        fontSize = 12.sp
                    )
                }
            } else if (mission.claimed) {
                Text(
                    "✓ Reclamada",
                    color = Color(0xFF81C784),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}
