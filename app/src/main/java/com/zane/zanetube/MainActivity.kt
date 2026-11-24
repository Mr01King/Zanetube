package com.zane.zanetube

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.webkit.WebViewAssetLoader

class MainActivity : Activity() {
    private lateinit var web: WebView
    private lateinit var root: FrameLayout
    private var fullscreenView: View? = null
    private var fullscreenCallback: WebChromeClient.CustomViewCallback? = null
    private var bubblePending = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Serve the bundled page from an https origin. YouTube's embed player refuses to
        // play (error 153) when the page is loaded from file:// because there is no referrer.
        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.mediaPlaybackRequiresUserGesture = false
        web.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? =
                assetLoader.shouldInterceptRequest(request.url)

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                if (request.url.host == APP_HOST) return false
                // Links such as "Watch on YouTube" open outside the app.
                try { startActivity(Intent(Intent.ACTION_VIEW, request.url)) } catch (e: ActivityNotFoundException) { /* no app can open this link */ }
                return true
            }
        }
        web.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                if (fullscreenView != null) { callback.onCustomViewHidden(); return }
                fullscreenView = view
                fullscreenCallback = callback
                root.addView(view, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
                web.visibility = View.GONE
            }

            override fun onHideCustomView() = exitFullscreen()
        }
        web.addJavascriptInterface(Bridge(), "Android")

        root = FrameLayout(this).apply { addView(web) }
        setContentView(root)
        if (savedInstanceState == null) web.loadUrl("https://$APP_HOST/assets/index.html")
        else web.restoreState(savedInstanceState)
    }

    private fun exitFullscreen() {
        fullscreenView?.let { root.removeView(it) }
        fullscreenView = null
        fullscreenCallback?.onCustomViewHidden()
        fullscreenCallback = null
        web.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        // Start the bubble automatically after the user returns from granting the overlay permission.
        if (bubblePending && Settings.canDrawOverlays(this)) {
            bubblePending = false
            startService(Intent(this, BubbleService::class.java))
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when {
            fullscreenView != null -> exitFullscreen()
            web.canGoBack() -> web.goBack()
            else -> super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        web.saveState(outState)
    }

    inner class Bridge {
        @JavascriptInterface fun playAudio(url: String) = runOnUiThread {
            if (!url.startsWith("https://")) return@runOnUiThread
            if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
            startService(
                Intent(this@MainActivity, PlaybackService::class.java)
                    .setAction(PlaybackService.ACTION_PLAY)
                    .putExtra(PlaybackService.EXTRA_URL, url)
            )
        }

        @JavascriptInterface fun stopAudio() = runOnUiThread {
            startService(Intent(this@MainActivity, PlaybackService::class.java).setAction(PlaybackService.ACTION_STOP))
        }

        @JavascriptInterface fun showBubble(): String {
            val allowed = Settings.canDrawOverlays(this@MainActivity)
            runOnUiThread {
                if (allowed) {
                    startService(Intent(this@MainActivity, BubbleService::class.java))
                } else {
                    bubblePending = true
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                }
            }
            return if (allowed) "started" else "permission"
        }
    }

    override fun onDestroy() {
        (web.parent as? ViewGroup)?.removeView(web)
        web.destroy()
        super.onDestroy()
    }

    companion object {
        private const val APP_HOST = "appassets.androidplatform.net"
    }
}
