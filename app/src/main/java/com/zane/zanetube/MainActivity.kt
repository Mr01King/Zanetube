package com.zane.zanetube

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient

class MainActivity : Activity() {
    private lateinit var web: WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.webChromeClient = WebChromeClient()
        web.webViewClient = WebViewClient()
        web.addJavascriptInterface(Bridge(), "Android")
        setContentView(web)
        web.loadUrl("file:///android_asset/index.html")
    }
    inner class Bridge {
        @JavascriptInterface fun playAudio(url: String) {
            val i = Intent(this@MainActivity, PlaybackService::class.java).putExtra("url", url)
            startForegroundService(i)
        }
        @JavascriptInterface fun showBubble() {
            if (!Settings.canDrawOverlays(this@MainActivity)) {
                val i = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivity(i)
            } else startService(Intent(this@MainActivity, BubbleService::class.java))
        }
    }
    override fun onDestroy() { web.destroy(); super.onDestroy() }
}
