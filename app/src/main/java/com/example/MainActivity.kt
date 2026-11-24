package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import android.opengl.GLSurfaceView
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

private val Ink = Color(0xFF08090C)
private val Panel = Color(0xDD111318)
private val Steel = Color(0xFFB8BBC2)
private val Gold = Color(0xFFD0B783)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = AndroidColor.BLACK
        window.navigationBarColor = AndroidColor.BLACK
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
            or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        setContent {
            var menuVisible by remember { mutableStateOf(true) }
            var settingsVisible by remember { mutableStateOf(false) }
            var traffic by remember { mutableStateOf(0) }
            var aiming by remember { mutableStateOf(0) }
            var cameraHeight by remember { mutableStateOf(1) }
            var vibration by remember { mutableStateOf(false) }
            var frameLimiter by remember { mutableStateOf(true) }

            BackHandler(enabled = settingsVisible || !menuVisible) {
                if (settingsVisible) settingsVisible = false else menuVisible = true
            }

            Box(Modifier.fillMaxSize().background(Ink)) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        GLSurfaceView(context).apply {
                            setEGLContextClientVersion(3)
                            setRenderer(NativeWorldRenderer(context.assets))
                            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                            preserveEGLContextOnPause = true
                            setOnTouchListener { view, event ->
                                val w = view.width.toFloat().coerceAtLeast(1f)
                                val h = view.height.toFloat().coerceAtLeast(1f)
                                when (event.actionMasked) {
                                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                                        if (event.x < w * 0.48f && event.y > h * 0.52f) {
                                            movePointer = event.getPointerId(event.actionIndex)
                                        } else {
                                            lookPointer = event.getPointerId(event.actionIndex)
                                            lastLookX = event.x
                                        }
                                    }
                                    MotionEvent.ACTION_MOVE -> {
                                        val mi = (0 until event.pointerCount).firstOrNull { event.getPointerId(it) == movePointer }
                                        if (mi != null) {
                                            val cx = w * 0.22f
                                            val cy = h * 0.78f
                                            NativeWorld.setMoveInput(((event.getX(mi) - cx) / (w * 0.17f)).coerceIn(-1f, 1f),
                                                ((event.getY(mi) - cy) / (h * 0.17f)).coerceIn(-1f, 1f))
                                        }
                                        val li = (0 until event.pointerCount).firstOrNull { event.getPointerId(it) == lookPointer }
                                        if (li != null) {
                                            val x = event.getX(li)
                                            NativeWorld.setLookInput(x - lastLookX)
                                            lastLookX = x
                                        }
                                    }
                                    MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                                        val id = event.getPointerId(event.actionIndex)
                                        if (id == movePointer) { movePointer = -1; NativeWorld.setMoveInput(0f, 0f) }
                                        if (id == lookPointer) lookPointer = -1
                                    }
                                }
                                true
                            }
                        }
                    }
                )

                if (menuVisible) {
                    if (settingsVisible) {
                        SettingsScreen(
                            traffic = traffic, onTraffic = { traffic = (traffic + 1) % 3 },
                            aiming = aiming, onAiming = { aiming = (aiming + 1) % 2 },
                            cameraHeight = cameraHeight, onCameraHeight = { cameraHeight = (cameraHeight + 1) % 3 },
                            vibration = vibration, onVibration = { vibration = !vibration },
                            frameLimiter = frameLimiter, onFrameLimiter = { frameLimiter = !frameLimiter },
                            onBack = { settingsVisible = false }
                        )
                    } else {
                        MainMenuScreen(
                            onStart = { menuVisible = false },
                            onLoad = { Toast.makeText(this, "No saved game yet", Toast.LENGTH_SHORT).show() },
                            onSettings = { settingsVisible = true }
                        )
                    }
                }
            }
        }
    }

    companion object {
        private var movePointer = -1
        private var lookPointer = -1
        private var lastLookX = 0f
    }
}

@Composable
private fun MainMenuScreen(onStart: () -> Unit, onLoad: () -> Unit, onSettings: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xD9080A10), Color(0xF5000000), Color.Black)))) {
        MapTexture(Modifier.fillMaxSize())
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("W I L D   H O R I Z O N", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text("AN OPEN WORLD", color = Gold, fontSize = 8.sp, letterSpacing = 3.sp, modifier = Modifier.padding(top = 5.dp))
            Spacer(Modifier.height(22.dp))
            CinematicPanels(Modifier.fillMaxWidth().weight(1f))
            Spacer(Modifier.height(18.dp))
            MenuButton("▶", "START GAME", true, onStart)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MenuButton("▣", "LOAD GAME", false, onLoad, Modifier.weight(1f))
                MenuButton("⚙", "SETTINGS", false, onSettings, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Text("WILD HORIZON  •  MOBILE EDITION", color = Color(0xFF777B84), fontSize = 8.sp, letterSpacing = 1.5.sp)
        }
    }
}

@Composable
private fun CinematicPanels(modifier: Modifier = Modifier) {
    Box(modifier.heightIn(min = 230.dp, max = 430.dp), contentAlignment = Alignment.Center) {
        ArtPanel(Modifier.fillMaxWidth(.48f).fillMaxHeight(.72f).align(Alignment.CenterStart).rotate(-8f), 0)
        ArtPanel(Modifier.fillMaxWidth(.48f).fillMaxHeight(.84f).align(Alignment.Center).rotate(1f), 1)
        ArtPanel(Modifier.fillMaxWidth(.42f).fillMaxHeight(.72f).align(Alignment.CenterEnd).rotate(8f), 2)
        Text("EXPLORE  •  DRIVE  •  LIVE", color = Color.White.copy(alpha = .85f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 5.dp))
    }
}

@Composable
private fun ArtPanel(modifier: Modifier, type: Int) {
    Canvas(modifier.clip(RoundedCornerShape(3.dp)).border(1.dp, Color(0xFF9BA2AA).copy(alpha = .55f))) {
        drawRect(Brush.verticalGradient(listOf(Color(0xFF5B829C), Color(0xFF26333C), Color(0xFF11151A))))
        // Cinematic sky and distant skyline
        drawCircle(Color(0xFFFFE5B0).copy(alpha = .42f), size.minDimension * .12f, Offset(size.width * .72f, size.height * .18f))
        for (i in 0..7) {
            val bw = size.width * .13f
            val bh = size.height * (.2f + (i % 4) * .08f)
            drawRect(Color(0xFF20272D), Offset(i * size.width / 8f, size.height * .58f - bh), Size(bw, bh))
            drawRect(Color(0xFFB6A27C).copy(alpha = .35f), Offset(i * size.width / 8f + 4f, size.height * .61f - bh), Size(3f, bh * .7f))
        }
        // Industrial cranes / mountain silhouettes
        val path = Path().apply {
            moveTo(0f, size.height * .72f)
            lineTo(size.width * .28f, size.height * .46f)
            lineTo(size.width * .44f, size.height * .69f)
            lineTo(size.width * .68f, size.height * .4f)
            lineTo(size.width, size.height * .7f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(path, Color(0xFF15191D).copy(alpha = .9f))
        if (type == 1) {
            drawLine(Color(0xFF15191D), Offset(size.width * .2f, size.height * .12f), Offset(size.width * .78f, size.height * .12f), 4f)
            drawLine(Color(0xFF15191D), Offset(size.width * .68f, size.height * .12f), Offset(size.width * .68f, size.height * .65f), 4f)
        }
        drawRect(Color.Black.copy(alpha = .12f))
    }
}

@Composable
private fun MapTexture(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        for (x in 0..12) {
            val px = size.width * x / 12f
            drawLine(Color.White.copy(alpha = .045f), Offset(px, 0f), Offset(px + size.width * .05f, size.height), 1f)
        }
        for (y in 0..18) {
            val py = size.height * y / 18f
            drawLine(Color.White.copy(alpha = .04f), Offset(0f, py), Offset(size.width, py - size.height * .025f), 1f)
        }
        val route = Path().apply {
            moveTo(size.width * .05f, size.height * .24f)
            lineTo(size.width * .34f, size.height * .29f)
            lineTo(size.width * .53f, size.height * .18f)
            lineTo(size.width * .94f, size.height * .32f)
            lineTo(size.width * .78f, size.height * .55f)
        }
        drawPath(route, Color(0xFFB7A57C).copy(alpha = .15f), style = Stroke(width = 2f))
    }
}

@Composable
private fun MenuButton(icon: String, label: String, primary: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val borderColor = if (primary) Gold else Color(0xFF777B84)
    Column(
        modifier.fillMaxWidth().height(if (primary) 58.dp else 62.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(if (primary) Color(0xFF27231B).copy(alpha = .92f) else Color(0xCC090A0D))
            .border(1.dp, borderColor.copy(alpha = .8f), RoundedCornerShape(3.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Text("$icon   $label", color = if (primary) Gold else Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
    }
}

@Composable
private fun SettingsScreen(
    traffic: Int, onTraffic: () -> Unit,
    aiming: Int, onAiming: () -> Unit,
    cameraHeight: Int, onCameraHeight: () -> Unit,
    vibration: Boolean, onVibration: () -> Unit,
    frameLimiter: Boolean, onFrameLimiter: () -> Unit,
    onBack: () -> Unit
) {
    val trafficValue = listOf("LIGHT", "NORMAL", "HEAVY")[traffic]
    val aimingValue = if (aiming == 0) "FREE AIM" else "ASSISTED"
    val cameraValue = listOf("LOW", "MEDIUM", "HIGH")[cameraHeight]
    Box(Modifier.fillMaxSize().background(Color(0xF208090C))) {
        MapTexture(Modifier.fillMaxSize())
        Column(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 22.dp)) {
            Text("WILD HORIZON", color = Gold, fontSize = 10.sp, letterSpacing = 2.5.sp, fontWeight = FontWeight.Bold)
            Text("SETTINGS", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.padding(top = 10.dp, bottom = 20.dp))
            Text("GAME", color = Steel, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 8.dp))
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                SettingRow("TRAFFIC MODE", trafficValue, onTraffic)
                SettingRow("TARGETING MODE", aimingValue, onAiming)
                SettingRow("CAMERA HEIGHT", cameraValue, onCameraHeight)
                SettingRow("VIBRATION FEEDBACK", if (vibration) "ON" else "OFF", onVibration)
                SettingRow("FRAME LIMITER", if (frameLimiter) "ON" else "OFF", onFrameLimiter)
                Spacer(Modifier.height(18.dp))
                Text("DISPLAY", color = Steel, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, modifier = Modifier.padding(vertical = 8.dp))
                SettingRow("SCREEN ORIENTATION", "PORTRAIT", {})
                SettingRow("GRAPHICS PROFILE", "DEVICE AUTO", {})
            }
            Text("RESTORE DEFAULTS", color = Color(0xFFBFC1C7), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, modifier = Modifier.fillMaxWidth().clickable {
                onBack()
            }.padding(vertical = 14.dp), textAlign = TextAlign.Center)
            Row(Modifier.fillMaxWidth().clickable(onClick = onBack).padding(vertical = 12.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                Text("‹   BACK", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            }
        }
    }
}

@Composable
private fun SettingRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(bottom = 5.dp).clip(RoundedCornerShape(2.dp))
            .background(Color(0xAA17191E)).clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 17.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFFE0E1E4), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp, modifier = Modifier.weight(1f))
        Text(value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
    }
}

private class NativeWorldRenderer(private val assets: android.content.res.AssetManager) : GLSurfaceView.Renderer {
    override fun onSurfaceCreated(gl: javax.microedition.khronos.opengles.GL10?, config: javax.microedition.khronos.egl.EGLConfig?) {
        NativeWorld.initialize(assets)
    }
    override fun onSurfaceChanged(gl: javax.microedition.khronos.opengles.GL10?, width: Int, height: Int) {
        NativeWorld.resize(width, height)
    }
    override fun onDrawFrame(gl: javax.microedition.khronos.opengles.GL10?) {
        NativeWorld.render()
    }
}

private object NativeWorld {
    init { System.loadLibrary("wildhorizon") }
    external fun initialize(assets: android.content.res.AssetManager)
    external fun resize(width: Int, height: Int)
    external fun render()
    external fun setMoveInput(x: Float, y: Float)
    external fun setLookInput(deltaX: Float)
}
