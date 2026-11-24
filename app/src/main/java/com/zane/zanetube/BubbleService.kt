package com.zane.zanetube

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.TextView
import kotlin.math.abs

/** Floating bubble: drag to move, tap to open ZaneTube, long-press to close. */
class BubbleService : Service() {
    private var wm: WindowManager? = null
    private var bubble: View? = null

    override fun onCreate() {
        super.onCreate()
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return }
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        wm = windowManager

        val density = resources.displayMetrics.density
        val view = TextView(this).apply {
            text = "▶ Z"
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(0xFFFFFFFF.toInt())
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0xFF6750A4.toInt())
            }
            val size = (60 * density).toInt()
            minWidth = size
            minHeight = size
            elevation = 6 * density
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 30
            y = 180
        }

        val touchSlop = ViewConfiguration.get(this).scaledTouchSlop
        val longPress = ViewConfiguration.getLongPressTimeout().toLong()
        view.setOnTouchListener(object : View.OnTouchListener {
            var startX = 0f; var startY = 0f; var winX = 0; var winY = 0
            var downTime = 0L; var dragging = false

            override fun onTouch(v: View, e: MotionEvent): Boolean {
                when (e.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = e.rawX; startY = e.rawY; winX = params.x; winY = params.y
                        downTime = e.eventTime; dragging = false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = e.rawX - startX; val dy = e.rawY - startY
                        if (!dragging && (abs(dx) > touchSlop || abs(dy) > touchSlop)) dragging = true
                        if (dragging) {
                            params.x = winX + dx.toInt()
                            params.y = winY + dy.toInt()
                            windowManager.updateViewLayout(v, params)
                        }
                    }
                    MotionEvent.ACTION_UP -> if (!dragging) {
                        if (e.eventTime - downTime >= longPress) stopSelf() else openApp()
                    }
                }
                return true
            }
        })

        bubble = view
        windowManager.addView(view, params)
    }

    private fun openApp() {
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_NOT_STICKY

    override fun onDestroy() {
        bubble?.let { runCatching { wm?.removeView(it) } }
        bubble = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
