package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.CoastalCyan
import com.example.ui.theme.NavyGlassSurface
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.SunsetGold
import com.example.ui.theme.TextPrimary
import kotlin.math.hypot

@Composable
fun TouchControlsOverlay(
    interactPrompt: String?,
    isSeated: Boolean,
    isDriving: Boolean,
    vehicleSpeedKmh: Int,
    vehicleGear: String,
    cameraSensitivity: Float,
    onJoystickMove: (Float, Float) -> Unit,
    onCameraDrag: (Float, Float) -> Unit,
    onJump: () -> Unit,
    onInteract: () -> Unit,
    onDriveInput: (Float, Float, Float, Float) -> Unit,
    onExitVehicle: () -> Unit,
    onHorn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Box(modifier = modifier.fillMaxSize()) {
        // Right-side 360 camera look swipe surface (independent multi-touch zone)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth(0.60f)
                .fillMaxHeight(0.84f)
                .testTag("camera_look_zone")
                .pointerInput(cameraSensitivity) {
                    awaitPointerEventScope {
                        var activePointer: PointerId? = null
                        var lastPos = Offset.Zero
                        while (true) {
                            val event = awaitPointerEvent()
                            val changes = event.changes
                            if (activePointer == null) {
                                val down = changes.firstOrNull { it.pressed && !it.isConsumed }
                                if (down != null) {
                                    activePointer = down.id
                                    lastPos = down.position
                                    down.consume()
                                }
                            }
                            val current = changes.firstOrNull { it.id == activePointer }
                            if (current != null) {
                                if (!current.pressed) {
                                    activePointer = null
                                } else {
                                    val delta = current.position - lastPos
                                    lastPos = current.position
                                    if (delta != Offset.Zero) {
                                        onCameraDrag(
                                            delta.x * cameraSensitivity,
                                            delta.y * cameraSensitivity
                                        )
                                    }
                                    current.consume()
                                }
                            }
                        }
                    }
                }
        )

        if (!isDriving) {
        // Left-side Virtual Analog Stick (Walk + Outer Ring Auto-Sprint)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 24.dp, bottom = 24.dp)
        ) {
            VirtualAnalogStick(
                onMove = onJoystickMove,
                onSprintThresholdCrossed = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            )
        }

        } else {
            DrivingControls(speedKmh = vehicleSpeedKmh, gear = vehicleGear, onDriveInput = onDriveInput, onExitVehicle = onExitVehicle, onHorn = onHorn)
        }

        // Dynamic Interact Prompt Pill + Action Buttons (Bottom Right)
        if (!isDriving) Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(end = 20.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedVisibility(
                visible = interactPrompt != null,
                enter = fadeIn(tween(150)) + scaleIn(tween(180)),
                exit = fadeOut(tween(120)) + scaleOut(tween(150))
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NavyGlassSurface,
                    border = BorderStroke(1.2.dp, NeonCoral.copy(alpha = 0.75f)),
                    shadowElevation = 6.dp
                ) {
                    Text(
                        text = interactPrompt.orEmpty(),
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                AnimatedVisibility(
                    visible = interactPrompt != null,
                    enter = fadeIn(tween(160)) + scaleIn(tween(200)),
                    exit = fadeOut(tween(120)) + scaleOut(tween(140))
                ) {
                    InteractActionButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onInteract()
                        }
                    )
                }

                JumpActionButton(
                    isSeated = isSeated,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onJump()
                    }
                )
            }
        }
    }
}

@Composable
private fun VirtualAnalogStick(
    onMove: (Float, Float) -> Unit,
    onSprintThresholdCrossed: () -> Unit
) {
    val density = LocalDensity.current
    val baseSizeDp = 128.dp
    val maxRadiusPx = with(density) { 46.dp.toPx() }
    val baseCenterPx = with(density) { (baseSizeDp / 2).toPx() }

    var knobOffset by remember { mutableStateOf(Offset.Zero) }
    var isSprinting by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AnimatedVisibility(
            visible = isSprinting,
            enter = fadeIn(tween(120)),
            exit = fadeOut(tween(120))
        ) {
            Text(
                text = stringResource(R.string.hud_sprint_badge),
                style = MaterialTheme.typography.labelSmall,
                color = SunsetGold,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .size(baseSizeDp)
                .testTag("virtual_joystick")
                .pointerInput(maxRadiusPx) {
                    awaitPointerEventScope {
                        var activePointer: PointerId? = null
                        var wasSprint = false
                        while (true) {
                            val event = awaitPointerEvent()
                            val changes = event.changes
                            if (activePointer == null) {
                                val down = changes.firstOrNull { it.pressed && !it.isConsumed }
                                if (down != null) {
                                    activePointer = down.id
                                }
                            }
                            val current = changes.firstOrNull { it.id == activePointer }
                            if (current != null) {
                                if (!current.pressed) {
                                    activePointer = null
                                    knobOffset = Offset.Zero
                                    isSprinting = false
                                    wasSprint = false
                                    onMove(0f, 0f)
                                } else {
                                    val rawX = current.position.x - baseCenterPx
                                    val rawY = current.position.y - baseCenterPx
                                    val dist = hypot(rawX, rawY)
                                    val clampedDist = dist.coerceAtMost(maxRadiusPx)
                                    val scale = if (dist > 0.001f) clampedDist / dist else 0f
                                    val dx = rawX * scale
                                    val dy = rawY * scale
                                    knobOffset = Offset(dx, dy)
                                    val ratio = clampedDist / maxRadiusPx
                                    val sprintNow = ratio > 0.78f
                                    if (sprintNow && !wasSprint) {
                                        onSprintThresholdCrossed()
                                    }
                                    wasSprint = sprintNow
                                    isSprinting = sprintNow
                                    onMove(dx / maxRadiusPx, dy / maxRadiusPx)
                                    current.consume()
                                }
                            }
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val outerR = size.minDimension / 2f - 4.dp.toPx()

                // Glass base disc
                drawCircle(
                    color = Color(0x550B1220),
                    radius = outerR,
                    center = center
                )

                // Outer ring highlights gold when sprinting
                drawCircle(
                    color = if (isSprinting) SunsetGold.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.26f),
                    radius = outerR,
                    center = center,
                    style = Stroke(width = if (isSprinting) 2.5.dp.toPx() else 1.8.dp.toPx())
                )

                // Inner sprint threshold guide ring
                drawCircle(
                    color = CoastalCyan.copy(alpha = 0.20f),
                    radius = outerR * 0.76f,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )

                // Thumb knob
                val knobCenter = center + knobOffset
                val knobRadius = 24.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = if (isSprinting) {
                            listOf(Color.White, SunsetGold, NeonCoral)
                        } else {
                            listOf(Color.White, CoastalCyan)
                        },
                        center = knobCenter - Offset(6.dp.toPx(), 6.dp.toPx()),
                        radius = knobRadius * 1.4f
                    ),
                    radius = knobRadius,
                    center = knobCenter
                )
            }
        }
    }
}


@Composable
private fun DrivingControls(
    speedKmh: Int,
    gear: String,
    onDriveInput: (Float, Float, Float, Float) -> Unit,
    onExitVehicle: () -> Unit,
    onHorn: () -> Unit
) {
    var throttle by remember { mutableStateOf(0f) }
    var steer by remember { mutableStateOf(0f) }
    var brake by remember { mutableStateOf(0f) }
    var handbrake by remember { mutableStateOf(0f) }
    fun sendDriveInput() = onDriveInput(throttle, steer, brake, handbrake)
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DriveHoldButton("◀", "Steer left") { pressed -> steer = if (pressed) -1f else 0f; sendDriveInput() }
                DriveHoldButton("▶", "Steer right") { pressed -> steer = if (pressed) 1f else 0f; sendDriveInput() }
            }
            Text("STEERING", color = Color.White.copy(alpha = 0.72f), fontWeight = FontWeight.Bold)
        }
        Column(
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 22.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Surface(color = Color(0xDD101B2B), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, CoastalCyan.copy(alpha = 0.75f))) {
                Row(modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("$speedKmh", color = SunsetGold, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall)
                        Text("KM/H", color = TextPrimary, style = MaterialTheme.typography.labelSmall)
                    }
                    Text("GEAR $gear", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                }
            }
            Surface(
                modifier = Modifier.clip(RoundedCornerShape(14.dp)).clickable(onClick = onHorn),
                color = Color(0xDD20364A), border = BorderStroke(1.dp, CoastalCyan.copy(alpha = 0.7f))
            ) {
                Text("HORN", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 19.dp, vertical = 10.dp))
            }
            DriveHoldButton("BRAKE", "Brake") { pressed -> brake = if (pressed) 1f else 0f; sendDriveInput() }
            DriveHoldButton("GAS", "Accelerate") { pressed -> throttle = if (pressed) 1f else 0f; sendDriveInput() }
            DriveHoldButton("HANDBRAKE", "Handbrake") { pressed -> handbrake = if (pressed) 1f else 0f; sendDriveInput() }
            Surface(
                modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable {
                    onDriveInput(0f, 0f, 0f, 0f)
                    onExitVehicle()
                },
                color = Color(0xDD8E2633),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
            ) {
                Text("EXIT CAR", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp))
            }
        }
    }
}

@Composable
private fun DriveHoldButton(
    label: String,
    description: String,
    onHold: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = when (label) { "HANDBRAKE" -> 104.dp; "GAS", "BRAKE" -> 82.dp; else -> 62.dp }, height = 62.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xCC101B2B))
            .border(1.5.dp, CoastalCyan.copy(alpha = 0.8f), RoundedCornerShape(18.dp))
            .pointerInput(description) {
                detectTapGestures(onPress = {
                    onHold(true)
                    tryAwaitRelease()
                    onHold(false)
                })
            }
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun InteractActionButton(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "interact_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val interactCd = stringResource(R.string.cd_interact)

    Box(
        modifier = Modifier
            .size(80.dp)
            .scale(pulseScale)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(CoastalCyan, NeonCoral)
                )
            )
            .border(2.dp, Color.White.copy(alpha = 0.9f), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = Color.White),
                role = Role.Button,
                onClick = onClick
            )
            .semantics { contentDescription = interactCd }
            .testTag("interact_button"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.TouchApp,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = stringResource(R.string.hud_interact),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White
            )
        }
    }
}

@Composable
private fun JumpActionButton(
    isSeated: Boolean,
    onClick: () -> Unit
) {
    val jumpCd = stringResource(R.string.cd_jump)

    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(NavyGlassSurface)
            .border(
                width = 2.dp,
                color = if (isSeated) SunsetGold else Color.White.copy(alpha = 0.34f),
                shape = CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = CoastalCyan),
                role = Role.Button,
                onClick = onClick
            )
            .semantics { contentDescription = jumpCd }
            .testTag("jump_button"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Icon(
                imageVector = if (isSeated) Icons.Filled.DirectionsWalk else Icons.Filled.ArrowUpward,
                contentDescription = null,
                tint = if (isSeated) SunsetGold else TextPrimary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = stringResource(if (isSeated) R.string.hud_stand else R.string.hud_jump),
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary
            )
        }
    }
}
