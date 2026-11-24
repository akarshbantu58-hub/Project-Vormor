package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.model.CameraZoomMode
import com.example.model.DistrictLandmark
import com.example.model.TimeOfDayPreset
import java.util.Locale

class NovaEngineController {
    private var webView: WebView? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    internal fun attach(view: WebView) {
        webView = view
    }

    internal fun detach() {
        webView = null
    }

    private fun evalJs(script: String) {
        val target = webView ?: return
        if (Looper.myLooper() == Looper.getMainLooper()) {
            target.evaluateJavascript(script, null)
        } else {
            mainHandler.post { target.evaluateJavascript(script, null) }
        }
    }

    fun setEnginePaused(paused: Boolean) {
        val target = webView ?: return
        mainHandler.post { if (paused) target.onPause() else target.onResume() }
    }

    fun setJoystick(x: Float, y: Float) {
        val fx = String.format(Locale.US, "%.3f", x.coerceIn(-1f, 1f))
        val fy = String.format(Locale.US, "%.3f", y.coerceIn(-1f, 1f))
        evalJs("window.NovaGame && window.NovaGame.setJoy($fx,$fy);")
    }

    fun addCameraLook(dx: Float, dy: Float) {
        val fdx = String.format(Locale.US, "%.2f", dx)
        val fdy = String.format(Locale.US, "%.2f", dy)
        evalJs("window.NovaGame && window.NovaGame.addLook($fdx,$fdy);")
    }

    fun jump() {
        evalJs("window.NovaGame && window.NovaGame.jump();")
    }

    fun interact() {
        evalJs("window.NovaGame && window.NovaGame.interact();")
    }

    fun setTimePreset(preset: TimeOfDayPreset) {
        evalJs("window.NovaGame && window.NovaGame.setTime(${preset.index});")
    }

    fun setRain(enabled: Boolean) {
        evalJs("window.NovaGame && window.NovaGame.setRain($enabled);")
    }

    fun setCameraMode(mode: CameraZoomMode) {
        evalJs("window.NovaGame && window.NovaGame.setCamMode(${mode.index});")
    }

    fun setAudio(enabled: Boolean) {
        evalJs("window.NovaGame && window.NovaGame.setAudio($enabled);")
    }

    fun setHighDpr(enabled: Boolean) {
        evalJs("window.NovaGame && window.NovaGame.setHighDpr($enabled);")
    }

    fun fastTravel(landmark: DistrictLandmark) {
        val x = String.format(Locale.US, "%.2f", landmark.spawnX)
        val y = String.format(Locale.US, "%.2f", landmark.spawnY)
        val z = String.format(Locale.US, "%.2f", landmark.spawnZ)
        val yaw = String.format(Locale.US, "%.2f", landmark.spawnYaw)
        val safeName = landmark.name.replace("'", "\\'")
        evalJs("window.NovaGame && window.NovaGame.fastTravel($x,$y,$z,$yaw,'$safeName');")
    }

    fun resetPlayer() {
        evalJs("window.NovaGame && window.NovaGame.resetPlayer();")
    }

    fun unlockAudio() {
        evalJs("window.NovaGame && window.NovaGame.unlockAudio();")
    }

    fun reloadEngine() {
        mainHandler.post {
            webView?.loadUrl(ASSET_ENGINE_URL)
        }
    }

    companion object {
        const val ASSET_ENGINE_URL = "file:///android_asset/nova_city.html"
    }
}

private class NovaJsBridge(
    private val onReady: () -> Unit,
    private val onError: (String) -> Unit,
    private val onTelemetry: (String) -> Unit,
    private val onToast: (String) -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onEngineReady() {
        mainHandler.post { onReady() }
    }

    @JavascriptInterface
    fun onEngineError(message: String) {
        mainHandler.post { onError(message) }
    }

    @JavascriptInterface
    fun onTelemetry(json: String) {
        mainHandler.post { onTelemetry(json) }
    }

    @JavascriptInterface
    fun onToast(message: String) {
        mainHandler.post { onToast(message) }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun NovaCityWebView(
    controller: NovaEngineController,
    onEngineReady: () -> Unit,
    onEngineError: (String) -> Unit,
    onTelemetry: (String) -> Unit,
    onToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var webViewRef: WebView? = remember { null }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> webViewRef?.onResume()
                Lifecycle.Event.ON_PAUSE -> webViewRef?.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            controller.detach()
            webViewRef?.destroy()
            webViewRef = null
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(Color.parseColor("#060A12"))
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                isHorizontalScrollBarEnabled = false
                isVerticalScrollBarEnabled = false
                overScrollMode = View.OVER_SCROLL_NEVER

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    allowFileAccess = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    mediaPlaybackRequiresUserGesture = false
                    useWideViewPort = true
                    loadWithOverviewMode = true
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean = true
                }

                webViewClient = object : WebViewClient() {
                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        if (request?.isForMainFrame == true) {
                            onEngineError(error?.description?.toString() ?: "WebView load error")
                        }
                    }
                }

                addJavascriptInterface(
                    NovaJsBridge(
                        onReady = onEngineReady,
                        onError = onEngineError,
                        onTelemetry = onTelemetry,
                        onToast = onToast
                    ),
                    "AndroidBridge"
                )

                webViewRef = this
                controller.attach(this)
                loadUrl(NovaEngineController.ASSET_ENGINE_URL)
            }
        }
    )
}
