package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.GameUiState
import com.example.model.MinimapBlockKind
import com.example.model.NovaCityWorldData
import com.example.model.TimeOfDayPreset
import com.example.ui.theme.CoastalCyan
import com.example.ui.theme.MapBackdropSlate
import com.example.ui.theme.MapBackgroundDark
import com.example.ui.theme.MapBuildingBlue
import com.example.ui.theme.MapOceanBlue
import com.example.ui.theme.MapParkGreen
import com.example.ui.theme.MapRoadSlate
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.NavyGlassSurface
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SunsetGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HudOverlay(
    uiState: GameUiState,
    onCycleTime: () -> Unit,
    onToggleRain: () -> Unit,
    onCycleCam: () -> Unit,
    onOpenDirectory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        // Top HUD Bar: Zone Card (Left) + Quick Action Pills (Right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ZoneTelemetryCard(
                    zoneName = uiState.zoneLabel,
                    credits = uiState.credits,
                    stamina = uiState.stamina,
                    elevationY = uiState.playerY
                )

                NovaMinimapRadar(
                    playerX = uiState.playerX,
                    playerZ = uiState.playerZ,
                    cameraYaw = uiState.cameraYaw,
                    onClick = onOpenDirectory
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HudPillButton(
                        icon = timeIcon(uiState.timePreset),
                        label = stringResource(uiState.timePreset.labelRes),
                        accentColor = when (uiState.timePreset) {
                            TimeOfDayPreset.SUNSET -> NeonCoral
                            TimeOfDayPreset.NIGHT -> CoastalCyan
                            else -> SunsetGold
                        },
                        contentDesc = stringResource(R.string.cd_time_cycle),
                        tag = "time_toggle_button",
                        onClick = onCycleTime
                    )

                    HudPillButton(
                        icon = if (uiState.isRaining) Icons.Filled.WaterDrop else Icons.Filled.CloudQueue,
                        label = stringResource(if (uiState.isRaining) R.string.hud_rain_on else R.string.hud_rain_off),
                        accentColor = if (uiState.isRaining) CoastalCyan else TextSecondary,
                        highlighted = uiState.isRaining,
                        contentDesc = stringResource(R.string.cd_rain_toggle),
                        tag = "rain_toggle_button",
                        onClick = onToggleRain
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HudPillButton(
                        icon = Icons.Filled.Videocam,
                        label = stringResource(uiState.camMode.labelRes),
                        accentColor = TextPrimary,
                        contentDesc = stringResource(R.string.cd_camera_mode),
                        tag = "camera_mode_button",
                        onClick = onCycleCam
                    )

                    HudPillButton(
                        icon = Icons.Filled.Explore,
                        label = "MAP",
                        accentColor = NeonCoral,
                        highlighted = true,
                        contentDesc = stringResource(R.string.cd_directory),
                        tag = "directory_button",
                        onClick = onOpenDirectory
                    )
                }
            }
        }

        // Animated Game Toast Banner
        AnimatedVisibility(
            visible = uiState.toastMessage != null,
            enter = fadeIn(tween(180)) + slideInVertically { -it / 2 },
            exit = fadeOut(tween(220)) + slideOutVertically { -it / 2 },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 86.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = NavyGlassSurface,
                border = BorderStroke(1.dp, CoastalCyan.copy(alpha = 0.65f)),
                shadowElevation = 8.dp
            ) {
                Text(
                    text = uiState.toastMessage.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ZoneTelemetryCard(
    zoneName: String,
    credits: Int,
    stamina: Int,
    elevationY: Float
) {
    val formattedCredits = NumberFormat.getNumberInstance(Locale.US).format(credits)
    val animatedStamina by animateFloatAsState(
        targetValue = (stamina / 100f).coerceIn(0f, 1f),
        animationSpec = tween(180),
        label = "stamina_bar"
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = NavyGlassSurface,
        border = BorderStroke(1.dp, SlateBorder),
        shadowElevation = 6.dp,
        modifier = Modifier
            .widthIn(min = 172.dp, max = 224.dp)
            .testTag("zone_telemetry_card")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.hud_brand_tag),
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCoral,
                    maxLines = 1
                )
                if (elevationY > 10f) {
                    Text(
                        text = "${elevationY.toInt()}m",
                        style = MaterialTheme.typography.labelSmall,
                        color = SunsetGold
                    )
                }
            }

            Text(
                text = zoneName,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = "${stringResource(R.string.hud_vc_label)} $formattedCredits",
                    style = MaterialTheme.typography.labelMedium,
                    color = MintEmerald
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.14f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedStamina)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(if (stamina < 25) SunsetGold else CoastalCyan)
                    )
                }
            }
        }
    }
}

@Composable
private fun HudPillButton(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    contentDesc: String,
    tag: String,
    highlighted: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (highlighted) accentColor.copy(alpha = 0.22f) else NavyGlassSurface,
        border = BorderStroke(
            width = 1.dp,
            color = if (highlighted) accentColor.copy(alpha = 0.78f) else SlateBorder
        ),
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(999.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = contentDesc }
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = TextPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun NovaMinimapRadar(
    playerX: Float,
    playerZ: Float,
    cameraYaw: Float,
    onClick: () -> Unit
) {
    val minimapCd = stringResource(R.string.cd_minimap)

    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(NavyGlassSurface)
            .border(1.2.dp, SlateBorder, RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = minimapCd }
            .testTag("minimap_radar")
            .padding(5.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp))
        ) {
            drawRect(color = MapBackgroundDark)

            // World bounds roughly [-110..110] in X and [-100..110] in Z
            val worldMinX = -110f
            val worldMinZ = -100f
            val worldSpan = 220f
            val scaleX = size.width / worldSpan
            val scaleZ = size.height / worldSpan

            for (block in NovaCityWorldData.minimapBlocks) {
                val color = when (block.kind) {
                    MinimapBlockKind.ROAD -> MapRoadSlate
                    MinimapBlockKind.OCEAN -> MapOceanBlue
                    MinimapBlockKind.PARK -> MapParkGreen
                    MinimapBlockKind.BUILDING -> MapBuildingBlue
                    MinimapBlockKind.BACKDROP -> MapBackdropSlate
                }
                drawRect(
                    color = color,
                    topLeft = Offset(
                        (block.x - worldMinX) * scaleX,
                        (block.z - worldMinZ) * scaleZ
                    ),
                    size = Size(block.width * scaleX, block.depth * scaleZ)
                )
            }

            // Player dot + viewing cone
            val px = ((playerX - worldMinX) * scaleX).coerceIn(4f, size.width - 4f)
            val pz = ((playerZ - worldMinZ) * scaleZ).coerceIn(4f, size.height - 4f)

            val forwardX = -sin(cameraYaw)
            val forwardZ = -cos(cameraYaw)
            val coneLen = 14.dp.toPx()
            val spread = 0.45f
            val leftAngle = kotlin.math.atan2(forwardZ, forwardX) - spread
            val rightAngle = kotlin.math.atan2(forwardZ, forwardX) + spread

            val conePath = Path().apply {
                moveTo(px, pz)
                lineTo(px + cos(leftAngle) * coneLen, pz + sin(leftAngle) * coneLen)
                lineTo(px + cos(rightAngle) * coneLen, pz + sin(rightAngle) * coneLen)
                close()
            }
            drawPath(path = conePath, color = CoastalCyan.copy(alpha = 0.36f))

            drawCircle(
                color = NeonCoral,
                radius = 4.2.dp.toPx(),
                center = Offset(px, pz)
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = Offset(px, pz)
            )
        }
    }
}

private fun timeIcon(preset: TimeOfDayPreset): ImageVector = when (preset) {
    TimeOfDayPreset.MORNING -> Icons.Filled.WbTwilight
    TimeOfDayPreset.NOON -> Icons.Filled.WbSunny
    TimeOfDayPreset.SUNSET -> Icons.Filled.WbTwilight
    TimeOfDayPreset.NIGHT -> Icons.Filled.NightsStay
}
