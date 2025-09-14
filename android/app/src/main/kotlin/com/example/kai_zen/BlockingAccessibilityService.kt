package com.example.kai_zen

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.content.Intent
import android.util.Log

class BlockingAccessibilityService : AccessibilityService() {
    companion object {
        var blockedPackages = setOf<String>()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: return
            if (blockedPackages.contains(pkg)) {
                // open a blocking activity
                val i = Intent(this, BlockingActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                i.putExtra("blocked_pkg", pkg)
                startActivity(i)
            }
        }
    }

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i("BlockingService","connected")
    }
}
