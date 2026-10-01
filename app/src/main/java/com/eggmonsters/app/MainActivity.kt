package com.eggmonsters.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.eggmonsters.app.ui.screens.*
import com.eggmonsters.app.ui.theme.EggMonstersTheme
import com.eggmonsters.app.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNeededPermissions()

        setContent {
            EggMonstersTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val state by viewModel.uiState.collectAsState()

                    when {
                        state.isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFFFFB74D))
                            }
                        }
                        state.isCreatureDead && state.creature != null -> {
                            DeathScreen(
                                creature = state.creature!!,
                                onRevive = { viewModel.revive() },
                                onStartOver = { viewModel.startOver() }
                            )
                        }
                        state.showMiniGame && state.creature != null -> {
                            val c = state.creature!!
                            if (state.miniGameType == 1) {
                                MemoryMiniGame(
                                    primaryColor = c.eggType.primaryColor,
                                    onFinished = { viewModel.onMiniGameFinished(it) },
                                    onCancel = { viewModel.closeMiniGame() }
                                )
                            } else {
                                MiniGameScreen(
                                    creatureEmoji = c.displayEmoji,
                                    primaryColor = c.eggType.primaryColor,
                                    onFinished = { viewModel.onMiniGameFinished(it) },
                                    onCancel = { viewModel.closeMiniGame() }
                                )
                            }
                        }
                        !state.hasChosenEgg -> {
                            EggSelectionScreen(
                                onEggSelected = { viewModel.selectEgg(it) }
                            )
                        }
                        !state.isHatched -> {
                            IncubationScreen(
                                state = state,
                                onEggClick = { viewModel.onEggClicked() }
                            )
                        }
                        else -> {
                            val creature = state.creature
                            if (creature != null) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    PetScreen(
                                        creature = creature,
                                        onFeed = { viewModel.feed() },
                                        onPlay = { viewModel.openMiniGame() },
                                        onClean = { viewModel.clean() },
                                        onSleep = { viewModel.sleep() },
                                        onPet = { viewModel.pet() },
                                        onOpenMissions = { viewModel.toggleMissions() },
                                        onOpenSkins = { viewModel.toggleSkinPicker() }
                                    )
                                    if (state.showMissions) {
                                        MissionsPanel(
                                            missionsState = state.dailyMissions,
                                            onClaim = { viewModel.claimMission(it) },
                                            onClose = { viewModel.toggleMissions() }
                                        )
                                    }
                                    if (state.showSkinPicker) {
                                        SkinPickerPanel(
                                            creature = creature,
                                            unlockedSkins = state.unlockedSkins,
                                            onSelect = { viewModel.equipSkin(it) },
                                            onClose = { viewModel.toggleSkinPicker() }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveNow()
    }

    private fun requestNeededPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }
}
