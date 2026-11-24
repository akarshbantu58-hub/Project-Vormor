package com.example

import android.graphics.Color
import android.os.Bundle
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK
        setContent {
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
        }
    }
    companion object {
        private var movePointer = -1
        private var lookPointer = -1
        private var lastLookX = 0f
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
