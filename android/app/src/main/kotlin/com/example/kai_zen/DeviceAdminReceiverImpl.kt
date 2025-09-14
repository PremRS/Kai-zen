package com.example.kai_zen

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class DeviceAdminReceiverImpl : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        Log.i("DeviceAdmin", "Enabled")
    }
    override fun onDisabled(context: Context, intent: Intent) {
        Log.i("DeviceAdmin", "Disabled")
    }
}
