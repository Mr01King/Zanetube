package com.zane.zanetube

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.TextView

class BubbleService : Service() {
    private var wm: WindowManager? = null
    private var bubble: TextView? = null
    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val view = TextView(this).apply {
            text = "▶ Z"
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFF6750A4.toInt())
            setPadding(22, 16, 22, 16)
            setOnClickListener { startActivity(Intent(this@BubbleService, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        }
        val type = if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE
        val params = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT).apply {
            gravity = Gravity.TOP or Gravity.START; x = 30; y = 180
        }
        view.setOnTouchListener(object : android.view.View.OnTouchListener {
            var sx = 0f; var sy = 0f; var wx = 0; var wy = 0
            override fun onTouch(v: android.view.View, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> { sx=e.rawX; sy=e.rawY; wx=params.x; wy=params.y; return true }
                    MotionEvent.ACTION_MOVE -> { params.x=wx+(e.rawX-sx).toInt(); params.y=wy+(e.rawY-sy).toInt(); wm?.updateViewLayout(view, params); return true }
                    MotionEvent.ACTION_UP -> { v.performClick(); return true }
                }; return false
            }
        })
        bubble = view
        wm?.addView(view, params)
    }
    override fun onDestroy() { bubble?.let { wm?.removeView(it) }; bubble=null; super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}
