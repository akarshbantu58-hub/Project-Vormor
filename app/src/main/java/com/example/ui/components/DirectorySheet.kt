package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.DistrictLandmark
import com.example.model.LandmarkCategory
import com.example.model.NovaCityWorldData
import com.example.model.UserSettings
import com.example.ui.theme.CoastalCyan
import com.example.ui.theme.DeepNavySurface
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ElevatedCardNavy
import com.example.ui.theme.MidnightObsidian
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SunsetGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.hypot
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaDirectorySheet(
    playerX: Float,
    playerZ: Float,
    settings: UserSettings,
    onFastTravel: (DistrictLandmark) -> Unit,
    onSensitivityChange: (Float) -> Unit,
    onAudioToggle: (Boolean) -> Unit,
    onHighResToggle: (Boolean) -> Unit,
    onResetPlayer: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepNavySurface,
        contentColor = TextPrimary,
        scrimColor = MidnightObsidian.copy(alpha = 0.72f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.map_sheet_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "District 01 • Veyra Prime",
                        style = MaterialTheme.typography.labelMedium,
                        color = CoastalCyan
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_directory_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.cd_close_sheet),
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = NeonCoral,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = NeonCoral
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = stringResource(R.string.map_tab_districts),
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    modifier = Modifier.testTag("tab_fast_travel")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = stringResource(R.string.map_tab_controls),
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    modifier = Modifier.testTag("tab_settings")
                )
            }

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(NovaCityWorldData.landmarks, key = { it.id }) { landmark ->
                        val distMeters = hypot(
                            (landmark.spawnX - playerX).toDouble(),
                            (landmark.spawnZ - playerZ).toDouble()
                        ).roundToInt()

                        LandmarkCard(
                            landmark = landmark,
                            distanceMeters = distMeters,
                            onTravel = { onFastTravel(landmark) }
                        )
                    }
                }
            } else {
                SettingsAndGuideContent(
                    settings = settings,
                    onSensitivityChange = onSensitivityChange,
                    onAudioToggle = onAudioToggle,
                    onHighResToggle = onHighResToggle,
                    onResetPlayer = onResetPlayer
                )
            }
        }
    }
}

@Composable
private fun LandmarkCard(
    landmark: DistrictLandmark,
    distanceMeters: Int,
    onTravel: () -> Unit
) {
    val accent = categoryColor(landmark.category)
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ElevatedCardNavy,
        border = BorderStroke(1.dp, SlateBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon(landmark.category),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = landmark.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = "${distanceMeters}m",
                        style = MaterialTheme.typography.labelSmall,
                        color = CoastalCyan
                    )
                }
                Text(
                    text = landmark.highlights,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Button(
                onClick = onTravel,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent.copy(alpha = 0.22f),
                    contentColor = accent
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                modifier = Modifier.testTag("travel_${landmark.id}")
            ) {
                Icon(
                    imageVector = Icons.Filled.MyLocation,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = stringResource(R.string.action_travel),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun SettingsAndGuideContent(
    settings: UserSettings,
    onSensitivityChange: (Float) -> Unit,
    onAudioToggle: (Boolean) -> Unit,
    onHighResToggle: (Boolean) -> Unit,
    onResetPlayer: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ElevatedCardNavy,
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.settings_camera_sensitivity),
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "${(settings.cameraSensitivity * 100).roundToInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = CoastalCyan
                            )
                        }
                        Slider(
                            value = settings.cameraSensitivity,
                            onValueChange = onSensitivityChange,
                            valueRange = 0.4f..2.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonCoral,
                                activeTrackColor = NeonCoral,
                                inactiveTrackColor = SlateBorder
                            ),
                            modifier = Modifier.testTag("sensitivity_slider")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.settings_sound_effects),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Switch(
                            checked = settings.audioEnabled,
                            onCheckedChange = onAudioToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MidnightObsidian,
                                checkedTrackColor = MintEmerald
                            ),
                            modifier = Modifier.testTag("audio_switch")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.settings_high_dpr),
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Toggle off for extra smoothness on older GPUs",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                        Switch(
                            checked = settings.highResRender,
                            onCheckedChange = onHighResToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MidnightObsidian,
                                checkedTrackColor = CoastalCyan
                            ),
                            modifier = Modifier.testTag("high_res_switch")
                        )
                    }
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ElevatedCardNavy,
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_controls_heading),
                        style = MaterialTheme.typography.titleMedium,
                        color = SunsetGold
                    )
                    Text(
                        text = "• ${stringResource(R.string.settings_controls_left)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "• ${stringResource(R.string.settings_controls_right)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "• ${stringResource(R.string.settings_controls_interact)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onResetPlayer,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.2.dp, NeonCoral),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCoral),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("respawn_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.settings_reset_position),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

private fun categoryIcon(category: LandmarkCategory): ImageVector = when (category) {
    LandmarkCategory.SKYLINE -> Icons.Filled.Apartment
    LandmarkCategory.COMMERCE -> Icons.Filled.Storefront
    LandmarkCategory.COASTAL -> Icons.Filled.DirectionsBoat
    LandmarkCategory.LIVING -> Icons.Filled.Home
    LandmarkCategory.SECRET -> Icons.Filled.LockOpen
}

private fun categoryColor(category: LandmarkCategory): Color = when (category) {
    LandmarkCategory.SKYLINE -> CoastalCyan
    LandmarkCategory.COMMERCE -> NeonCoral
    LandmarkCategory.COASTAL -> MintEmerald
    LandmarkCategory.LIVING -> ElectricViolet
    LandmarkCategory.SECRET -> SunsetGold
}
