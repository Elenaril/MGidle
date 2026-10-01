package com.eggmonsters.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eggmonsters.app.data.Creature

@Composable
fun DeathScreen(
    creature: Creature,
    onRevive: () -> Unit,
    onStartOver: () -> Unit
) {
    val canRevive = creature.canRevive()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A0000), Color(0xFF2D1B1B), Color(0xFF1A1A2E))
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "💀", fontSize = 80.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${creature.name} se ha ido...",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFEF9A9A),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Lo descuidaste demasiado tiempo.\nLas estadísticas llegaron a cero.",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp, bottom = 32.dp)
        )

        if (canRevive) {
            Text(
                text = "Reanimaciones restantes: ${2 - creature.timesRevived}",
                color = Color(0xFFFFD54F),
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Button(
                onClick = onRevive,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "✨ Reanimar con Huevo de Segunda Oportunidad",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        } else {
            Text(
                text = "Ya usaste las 2 reanimaciones de esta criatura.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        OutlinedButton(
            onClick = onStartOver,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
        ) {
            Text("Empezar de nuevo con otro huevo")
        }
    }
}
