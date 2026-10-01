package com.eggmonsters.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eggmonsters.app.data.Creature
import com.eggmonsters.app.data.CreatureSkin
import com.eggmonsters.app.data.EggType
import com.eggmonsters.app.data.GrowthStage

@Composable
fun SkinPickerPanel(
    creature: Creature,
    unlockedSkins: Set<String>,
    onSelect: (CreatureSkin?) -> Unit, // null = auto
    onClose: () -> Unit
) {
    val available = CreatureSkin.entries.filter {
        it.name.startsWith(creature.eggType.name) || it in listOf(
            CreatureSkin.forCreature(creature.eggType, GrowthStage.BABY),
            CreatureSkin.forCreature(creature.eggType, GrowthStage.TEEN),
            CreatureSkin.forCreature(creature.eggType, GrowthStage.ADULT)
        )
    }.distinct()

    // También skins desbloqueadas de otros elementos (cosméticos raros)
    val extras = CreatureSkin.entries.filter { it.name in unlockedSkins }

    val all = (available + extras).distinct()

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
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Apariencia",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }
                }

                Text(
                    "Evoluciona para desbloquear más formas de tu elemento",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Auto
                SkinChip(
                    emoji = creature.eggType.emoji,
                    label = "Automático",
                    selected = creature.equippedSkin.isEmpty(),
                    unlocked = true,
                    onClick = { onSelect(null) }
                )

                Spacer(Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 280.dp)
                ) {
                    items(all) { skin ->
                        val unlocked = skin.name.startsWith(creature.eggType.name) ||
                            skin.name in unlockedSkins ||
                            skin == CreatureSkin.forCreature(creature.eggType, creature.stage)
                        SkinChip(
                            emoji = skin.emoji,
                            label = skin.label,
                            selected = creature.equippedSkin == skin.name,
                            unlocked = unlocked,
                            onClick = { if (unlocked) onSelect(skin) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SkinChip(
    emoji: String,
    label: String,
    selected: Boolean,
    unlocked: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) Color.White.copy(alpha = 0.2f)
                else Color.White.copy(alpha = 0.06f)
            )
            .then(
                if (selected) Modifier.border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(12.dp))
                else Modifier
            )
            .clickable(enabled = unlocked, onClick = onClick)
            .padding(10.dp)
    ) {
        Text(
            text = if (unlocked) emoji else "🔒",
            fontSize = 28.sp
        )
        Text(
            text = label,
            color = if (unlocked) Color.White.copy(alpha = 0.8f) else Color.Gray,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}
