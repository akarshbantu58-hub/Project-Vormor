package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background\nimport androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize\nimport androidx.compose.foundation.layout.padding\nimport androidx.compose.foundation.layout.size\nimport androidx.compose.foundation.shape.RoundedCornerShape\nimport androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.filled.Pause\nimport androidx.compose.material3.Icon\nimport androidx.compose.material3.Surface\nimport androidx.compose.ui.Alignment\nimport androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.unit.dp\nimport androidx.compose.ui.text.font.FontWeight\nimport androidx.compose.ui.unit.sp
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
import com.example.ui.theme.MidnightObsidian\nimport com.example.ui.screens.PauseOverlay
import com.example.viewmodel.GameViewModel

@Composable
fun NovaCityScreen(
    viewModel: GameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val engineController = remember { NovaEngineController() }\n    var showPause by remember { androidx.compose.runtime.mutableStateOf(false) }

    // Keep persisted settings synchronized with the WebGL engine once ready
    LaunchedEffect(uiState.engineStatus, settings.audioEnabled, settings.highResRender) {
        if (uiState.engineStatus == EngineStatus.READY) {
            engineController.setAudio(settings.audioEnabled)
            engineController.setHighDpr(settings.highResRender)
        }
    }

    BackHandler(enabled = !uiState.showSplash && !uiState.showDirectorySheet) {\n        showPause = !showPause\n        engineController.setJoystick(0f, 0f)\n        engineController.setEnginePaused(showPause)\n    }\n\n    // Intercept system back button when Directory Sheet is open
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
        if (!uiState.showSplash && !showPause) {
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

        if (!uiState.showSplash && !showPause) {\n            Surface(\n                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).clickable {\n                    engineController.setJoystick(0f, 0f)\n                    showPause = true\n                    engineController.setEnginePaused(true)\n                },\n                color = Color(0xD91A202A), shape = RoundedCornerShape(24.dp),\n                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFC857))\n            ) {\n                androidx.compose.foundation.layout.Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {\n                    Icon(Icons.Filled.Pause, null, tint = Color(0xFFFFC857), modifier = Modifier.size(17.dp))\n                    androidx.compose.foundation.layout.Spacer(Modifier.size(7.dp))\n                    androidx.compose.material3.Text("PAUSE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)\n                }\n            }\n        }\n\n        if (showPause) {\n            PauseOverlay(\n                onResume = { showPause = false; engineController.setEnginePaused(false) },\n                onMap = { showPause = false; engineController.setEnginePaused(false); viewModel.setDirectorySheetVisible(true) },\n                onSettings = { showPause = false; engineController.setEnginePaused(false); viewModel.setDirectorySheetVisible(true) },\n                onSave = { viewModel.showToast("Save slot support is being added") },\n                onMainMenu = { showPause = false; engineController.setEnginePaused(false); viewModel.setDirectorySheetVisible(false); viewModel.returnToMenu() }\n            )\n        }\n\n        // Layer 4: Cinematic Launch Splash Screen
        AnimatedVisibility(
            visible = uiState.showSplash,
            enter = fadeIn(tween(250)),
            exit = fadeOut(tween(420))
        ) {
            SplashOverlay(
                engineStatus = uiState.engineStatus,
                engineErrorMessage = uiState.engineErrorMessage,
                settings = settings,
                onLaunchClick = {
                    engineController.unlockAudio()
                    viewModel.launchCity()
                },
                onRetryClick = {
                    viewModel.onRetryEngine()
                    engineController.reloadEngine()
                },
                onSensitivityChange = { viewModel.setCameraSensitivity(it) },
                onAudioToggle = {
                    viewModel.setAudioEnabled(it)
                    engineController.setAudio(it)
                },
                onHighResToggle = {
                    viewModel.setHighResRender(it)
                    engineController.setHighDpr(it)
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
