package com.example

import android.graphics.Color
import android.os.Bundle
import android.opengl.GLSurfaceView
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
                        setRenderer(NativeWorldRenderer())
                        renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                        preserveEGLContextOnPause = true
                    }
                }
            )
        }
    }
}

private class NativeWorldRenderer : GLSurfaceView.Renderer {
    override fun onSurfaceCreated(gl: javax.microedition.khronos.opengles.GL10?, config: javax.microedition.khronos.egl.EGLConfig?) {
        NativeWorld.initialize()
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
    external fun initialize()
    external fun resize(width: Int, height: Int)
    external fun render()
}
