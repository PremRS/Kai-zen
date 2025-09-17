package com.example.kai_zen

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.content.Intent
import android.util.Log

class BlockingAccessibilityService : AccessibilityService() {
    companion object {
        val blockedPackages = mutableSetOf<String>() 
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: return
            if (blockedPackages.contains(pkg)) {
                val intent = Intent(this, BlockingActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra("blockedPkg", pkg)
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {}

    fun updateBlockedApps(pkgs: List<String>) {
        blockedPackages.clear()
        blockedPackages.addAll(pkgs)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i("BlockingService","connected")
    }
}
