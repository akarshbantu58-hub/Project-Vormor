package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.EngineStatus
import com.example.model.UserSettings

private val MenuGold = Color(0xFFFFC857)
private val MenuPanel = Color(0xE9161B24)
private val MenuLine = Color(0xFF59616C)
private val MenuText = Color(0xFFF5F1E8)
private val MenuMuted = Color(0xFFB8BEC7)

@Composable
fun SplashOverlay(
    engineStatus: EngineStatus,
    engineErrorMessage: String?,
    settings: UserSettings,
    onLaunchClick: () -> Unit,
    onRetryClick: () -> Unit,
    onSensitivityChange: (Float) -> Unit,
    onAudioToggle: (Boolean) -> Unit,
    onHighResToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSettings by remember { mutableStateOf(false) }
    var selectedSettingsTab by remember { mutableIntStateOf(0) }

    BackHandler(enabled = showSettings) { showSettings = false }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF080B10))) {
        Image(
            painter = painterResource(id = R.drawable.img_nova_city_hero_1795347430294),
            contentDescription = "WILD HORIZON city artwork",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Color.Black.copy(alpha = 0.18f),
                    0.34f to Color(0xFF090D13).copy(alpha = 0.42f),
                    0.60f to Color(0xFF090D13).copy(alpha = 0.88f),
                    1f to Color(0xFF070A0F).copy(alpha = 0.98f)
                )
            )
        )

        Column(
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("OPEN WORLD • SINGLE PLAYER", color = MenuGold, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text("WILD", color = MenuText, fontSize = 43.sp, lineHeight = 44.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                Text("HORIZON", color = MenuGold, fontSize = 31.sp, lineHeight = 34.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp)
                Text("THE CITY IS YOURS", color = MenuText.copy(alpha = 0.88f), fontSize = 10.sp, letterSpacing = 3.sp)
            }

            if (showSettings) {
                SettingsPanel(
                    selectedTab = selectedSettingsTab,
                    onTabChange = { selectedSettingsTab = it },
                    settings = settings,
                    onSensitivityChange = onSensitivityChange,
                    onAudioToggle = onAudioToggle,
                    onHighResToggle = onHighResToggle,
                    onBack = { showSettings = false }
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("MAIN MENU", color = MenuGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    MenuAction(
                        title = "START GAME",
                        subtitle = "Enter the open world",
                        icon = Icons.Filled.PlayArrow,
                        highlighted = true,
                        onClick = onLaunchClick,
                        enabled = engineStatus == EngineStatus.READY,
                        tag = "launch_city_button"
                    )
                    MenuAction(
                        title = "SETTINGS",
                        subtitle = "Display, audio and controls",
                        icon = Icons.Filled.Settings,
                        onClick = { showSettings = true },
                        tag = "open_settings_button"
                    )
                    MenuAction(
                        title = "CONTROLS",
                        subtitle = "Movement and camera guide",
                        icon = Icons.Filled.Gamepad,
                        onClick = { selectedSettingsTab = 2; showSettings = true },
                        tag = "open_controls_button"
                    )

                    when (engineStatus) {
                        EngineStatus.LOADING -> Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = MenuGold, strokeWidth = 2.dp, modifier = Modifier.size(17.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("LOADING CITY ENGINE…", color = MenuMuted, fontSize = 11.sp)
                        }
                        EngineStatus.ERROR -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(engineErrorMessage ?: "The city could not be loaded.", color = Color(0xFFFF8B7D), fontSize = 12.sp)
                            OutlinedButton(onClick = onRetryClick, modifier = Modifier.fillMaxWidth().testTag("retry_engine_button")) {
                                Icon(Icons.Filled.Refresh, null)
                                Spacer(Modifier.width(8.dp))
                                Text("RETRY")
                            }
                        }
                        EngineStatus.READY -> Text("CITY ENGINE READY  •  TOUCH TO BEGIN", color = Color(0xFFB6D7C4), fontSize = 10.sp, letterSpacing = 1.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("WILD HORIZON  •  BUILD 01", color = MenuMuted, fontSize = 9.sp, letterSpacing = 1.sp)
                Text("PORTRAIT EDITION", color = MenuGold, fontSize = 9.sp, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
private fun MenuAction(
    title: String,
    subtitle: String,
    icon: ImageVector,
    highlighted: Boolean = false,
    enabled: Boolean = true,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(66.dp).testTag(tag).clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(7.dp),
        color = if (highlighted) Color(0xE8A96F20) else MenuPanel,
        border = BorderStroke(1.dp, if (highlighted) MenuGold.copy(alpha = 0.8f) else MenuLine.copy(alpha = 0.65f)),
        shadowElevation = if (highlighted) 8.dp else 2.dp
    ) {
        Row(Modifier.padding(horizontal = 15.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (highlighted) Color.White else MenuGold, modifier = Modifier.size(25.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, color = MenuText, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text(subtitle, color = MenuMuted, fontSize = 10.sp)
            }
            Text("›", color = MenuGold, fontSize = 27.sp, fontWeight = FontWeight.Light)
        }
    }
}

@Composable
private fun SettingsPanel(
    selectedTab: Int,
    onTabChange: (Int) -> Unit,
    settings: UserSettings,
    onSensitivityChange: (Float) -> Unit,
    onAudioToggle: (Boolean) -> Unit,
    onHighResToggle: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().background(MenuPanel, RoundedCornerShape(9.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(38.dp).clickable(onClick = onBack),
                shape = RoundedCornerShape(6.dp), color = Color(0xFF303844)
            ) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.ArrowBack, "Back", tint = MenuText) } }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("SETTINGS", color = MenuText, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                Text("CUSTOMIZE YOUR GAME", color = MenuGold, fontSize = 9.sp, letterSpacing = 1.5.sp)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            listOf("DISPLAY", "AUDIO", "CONTROLS").forEachIndexed { index, label ->
                Surface(
                    modifier = Modifier.weight(1f).height(37.dp).clickable { onTabChange(index) },
                    shape = RoundedCornerShape(5.dp),
                    color = if (selectedTab == index) Color(0xFF9C6825) else Color(0xFF252C36),
                    border = BorderStroke(1.dp, if (selectedTab == index) MenuGold else MenuLine)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(label, color = MenuText, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    }
                }
            }
        }
        when (selectedTab) {
            0 -> {
                SettingSwitchRow("HIGH RESOLUTION", "Uses a higher device pixel ratio; may reduce FPS.", settings.highResRender, onHighResToggle)
                Text("Recommended on Galaxy Tab A9: OFF for smoother gameplay.", color = MenuGold, fontSize = 10.sp)
                Text("4K assets are reserved for supported high-end devices. The game scales rendering to device capability.", color = MenuMuted, fontSize = 10.sp)
            }
            1 -> {
                SettingSwitchRow("SOUND & MUSIC", "Enable game audio.", settings.audioEnabled, onAudioToggle)
                Text("Audio settings are saved on this device.", color = MenuMuted, fontSize = 10.sp)
            }
            else -> {
                Text("CAMERA SENSITIVITY", color = MenuText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("${(settings.cameraSensitivity * 100).toInt()}%", color = MenuGold, fontSize = 11.sp)
                Slider(
                    value = settings.cameraSensitivity,
                    onValueChange = onSensitivityChange,
                    valueRange = 0.4f..2.0f,
                    colors = SliderDefaults.colors(thumbColor = MenuGold, activeTrackColor = MenuGold, inactiveTrackColor = MenuLine),
                    modifier = Modifier.testTag("sensitivity_slider")
                )
                Text("LEFT STICK  •  MOVE / RUN", color = MenuMuted, fontSize = 10.sp)
                Text("DRAG RIGHT SIDE  •  ROTATE CAMERA", color = MenuMuted, fontSize = 10.sp)
                Text("ACTION BUTTON  •  INTERACT / ENTER", color = MenuMuted, fontSize = 10.sp)
                Text("JUMP BUTTON  •  JUMP", color = MenuMuted, fontSize = 10.sp)
            }
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(44.dp), border = BorderStroke(1.dp, MenuGold), colors = ButtonDefaults.outlinedButtonColors(contentColor = MenuGold)) {
            Icon(Icons.Filled.Check, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("DONE", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = MenuText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(description, color = MenuMuted, fontSize = 10.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF171B22), checkedTrackColor = MenuGold)
        )
    }
}
