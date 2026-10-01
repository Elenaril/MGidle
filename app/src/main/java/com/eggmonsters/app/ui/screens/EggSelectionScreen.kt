package com.eggmonsters.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun EggSelectionScreen(
    onEggSelected: (EggType) -> Unit
) {
    var selectedPreview by remember { mutableStateOf<EggType?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460))
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "EggMonsters",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD54F)
        )

        Text(
            text = "Elige tu huevo legendario",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(EggType.entries) { egg ->
                EggCard(
                    egg = egg,
                    isSelected = selectedPreview == egg,
                    onClick = { selectedPreview = egg }
                )
            }
        }

        // Panel de información del huevo seleccionado
        selectedPreview?.let { egg ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${egg.emoji} ${egg.displayName}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = egg.primaryColor
                    )
                    Text(
                        text = "Criatura: ${egg.creatureName}",
                        color = Color.White,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "Elemento: ${egg.element}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = egg.personality,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            Button(
                onClick = { onEggSelected(egg) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = egg.primaryColor
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Elegir este huevo",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        } ?: run {
            Spacer(modifier = Modifier.height(80.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun EggCard(
    egg: EggType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .scale(scale)
            .clickable { onClick() }
            .then(
                if (isSelected) {
                    Modifier.border(3.dp, egg.primaryColor, RoundedCornerShape(20.dp))
                } else Modifier
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Huevo visual
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(egg.secondaryColor, egg.primaryColor)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = egg.emoji,
                    fontSize = 36.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = egg.displayName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = egg.element,
                fontSize = 12.sp,
                color = egg.secondaryColor,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
