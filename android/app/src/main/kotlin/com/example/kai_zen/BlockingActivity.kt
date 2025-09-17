package com.example.kai_zen

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

class BlockingActivity : Activity() {
    companion object {
        private val instances = mutableListOf<BlockingActivity>()
        fun finishAll() {
            for (inst in instances.toList()) {
                try { inst.finish() } catch (ignored: Exception) {}
            }
            instances.clear()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instances.add(this)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        val pkg = intent.getStringExtra("blockedPkg") ?: "App"

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 200, 50, 50)

        val message = TextView(this)
        message.text = "\u26A0\uFE0F $pkg is blocked during your focus time!\n\nOpen the App Hider and press <Unhide> to restore access."
        message.textSize = 18f
        message.textAlignment = TextView.TEXT_ALIGNMENT_CENTER

        layout.addView(message)
        setContentView(layout)
    }

    override fun onDestroy() {
        super.onDestroy()
        instances.remove(this)
    }

    override fun onBackPressed() {
        // disable back navigation to enforce blocking
    }
}
