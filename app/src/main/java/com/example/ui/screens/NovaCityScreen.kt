package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.EngineStatus
import com.example.ui.components.HudOverlay
import com.example.ui.components.NovaCityWebView
import com.example.ui.components.NovaDirectorySheet
import com.example.ui.components.NovaEngineController
import com.example.ui.components.TouchControlsOverlay
import com.example.ui.theme.MidnightObsidian
import com.example.viewmodel.GameViewModel

@Composable
fun NovaCityScreen(
    viewModel: GameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val engineController = remember { NovaEngineController() }

    // Keep persisted settings synchronized with the WebGL engine once ready
    LaunchedEffect(uiState.engineStatus, settings.audioEnabled, settings.highResRender) {
        if (uiState.engineStatus == EngineStatus.READY) {
            engineController.setAudio(settings.audioEnabled)
            engineController.setHighDpr(settings.highResRender)
        }
    }

    // Intercept system back button when Directory Sheet is open
    BackHandler(enabled = uiState.showDirectorySheet) {
        viewModel.setDirectorySheetVisible(false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightObsidian)
    ) {
        // Layer 1: Full-Bleed Hardware-Accelerated 3D WebGL Engine
        NovaCityWebView(
            controller = engineController,
            onEngineReady = { viewModel.onEngineReady() },
            onEngineError = { msg -> viewModel.onEngineError(msg) },
            onTelemetry = { json -> viewModel.onTelemetryJson(json) },
            onToast = { msg -> viewModel.showToast(msg) },
            modifier = Modifier.fillMaxSize()
        )

        // Layer 2 & 3: Native Dual-Zone Touch Controls + Material 3 Telemetry HUD
        if (!uiState.showSplash) {
            TouchControlsOverlay(
                interactPrompt = uiState.interactPrompt,
                isSeated = uiState.isSeated,
                cameraSensitivity = settings.cameraSensitivity,
                onJoystickMove = { x, y -> engineController.setJoystick(x, y) },
                onCameraDrag = { dx, dy -> engineController.addCameraLook(dx, dy) },
                onJump = { engineController.jump() },
                onInteract = { engineController.interact() }
            )

            HudOverlay(
                uiState = uiState,
                onCycleTime = {
                    val next = uiState.timePreset.next()
                    viewModel.updateTimePreset(next)
                    engineController.setTimePreset(next)
                },
                onToggleRain = {
                    val next = !uiState.isRaining
                    viewModel.updateRain(next)
                    engineController.setRain(next)
                },
                onCycleCam = {
                    val next = uiState.camMode.next()
                    viewModel.updateCameraMode(next)
                    engineController.setCameraMode(next)
                },
                onOpenDirectory = {
                    engineController.setJoystick(0f, 0f)
                    viewModel.setDirectorySheetVisible(true)
                }
            )
        }

        // Layer 4: Cinematic Launch Splash Screen
        AnimatedVisibility(
            visible = uiState.showSplash,
            enter = fadeIn(tween(250)),
            exit = fadeOut(tween(420))
        ) {
            SplashOverlay(
                engineStatus = uiState.engineStatus,
                engineErrorMessage = uiState.engineErrorMessage,
                onLaunchClick = {
                    engineController.unlockAudio()
                    viewModel.launchCity()
                },
                onRetryClick = {
                    viewModel.onRetryEngine()
                    engineController.reloadEngine()
                }
            )
        }

        // Layer 5: District Fast-Travel & Settings Modal Sheet
        if (uiState.showDirectorySheet) {
            NovaDirectorySheet(
                playerX = uiState.playerX,
                playerZ = uiState.playerZ,
                settings = settings,
                onFastTravel = { landmark ->
                    engineController.fastTravel(landmark)
                    viewModel.setDirectorySheetVisible(false)
                },
                onSensitivityChange = { value ->
                    viewModel.setCameraSensitivity(value)
                },
                onAudioToggle = { enabled ->
                    viewModel.setAudioEnabled(enabled)
                    engineController.setAudio(enabled)
                },
                onHighResToggle = { enabled ->
                    viewModel.setHighResRender(enabled)
                    engineController.setHighDpr(enabled)
                },
                onResetPlayer = {
                    engineController.resetPlayer()
                    viewModel.setDirectorySheetVisible(false)
                },
                onDismiss = {
                    viewModel.setDirectorySheetVisible(false)
                }
            )
        }
    }
}
