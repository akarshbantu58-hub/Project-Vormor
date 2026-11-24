package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PauseGold = Color(0xFFFFC857)
private val PauseText = Color(0xFFF5F1E8)

@Composable
fun PauseOverlay(
    onResume: () -> Unit,
    onMap: () -> Unit,
    onSettings: () -> Unit,
    onSave: () -> Unit,
    onMainMenu: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(Color(0xCC070A10)), contentAlignment = Alignment.Center) {
        Surface(
            color = Color(0xF0181D26),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(0.72f).border(1.dp, PauseGold.copy(alpha = .55f), RoundedCornerShape(14.dp))
        ) {
            Row(Modifier.padding(22.dp), horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(0.8f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text("WILD HORIZON", color = PauseGold, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text("PAUSED", color = PauseText, fontSize = 30.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                    Text("Take a moment. The city will be here.", color = Color(0xFFB8BEC7), fontSize = 11.sp)
                    Spacer(Modifier.height(8.dp))
                    PauseAction("RESUME", Icons.Filled.PlayArrow, true, onResume)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PauseAction("MAP & DISTRICTS", Icons.Filled.Map, false, onMap)
                    PauseAction("SETTINGS", Icons.Filled.Settings, false, onSettings)
                    PauseAction("SAVE GAME", Icons.Filled.Save, false, onSave)
                    PauseAction("MAIN MENU", Icons.Filled.Home, false, onMainMenu)
                }
            }
        }
    }
}

@Composable
private fun PauseAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, primary: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(46.dp).clickable(onClick = onClick),
        color = if (primary) Color(0xFF93621F) else Color(0xFF252C36),
        shape = RoundedCornerShape(7.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (primary) PauseGold else Color(0xFF56606D))
    ) {
        Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = PauseGold, modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(10.dp))
            Text(label, color = PauseText, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = .8.sp)
        }
    }
}
