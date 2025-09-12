package com.example.app_hider

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    private val CHANNEL = "com.example.app_hider/channel"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
            call, result ->
            when (call.method) {
                "disablePackages" -> {
                    val pkgs: List<String> = call.argument("packages") ?: listOf()
                    disablePackages(pkgs)
                    result.success(true)
                }
                "enablePackages" -> {
                    val pkgs: List<String> = call.argument("packages") ?: listOf()
                    enablePackages(pkgs)
                    result.success(true)
                }
                "launcherHidePackages" -> {
                    val pkgs: List<String> = call.argument("packages") ?: listOf()
                    launcherHide(pkgs)
                    result.success(true)
                }
                "launcherUnhidePackages" -> {
                    val pkgs: List<String> = call.argument("packages") ?: listOf()
                    launcherUnhide(pkgs)
                    result.success(true)
                }
                "getUsageForPackages" -> {
                    val pkgs: List<String> = call.argument("packages") ?: listOf()
                    val stats = getUsageForPackages(pkgs)
                    result.success(stats.toString())
                }
                else -> result.notImplemented()
            }
        }
    }

    private fun disablePackages(pkgs: List<String>) {
        val pm = applicationContext.packageManager
        for (pkg in pkgs) {
            try {
                pm.setApplicationEnabledSetting(pkg,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER,
                    PackageManager.DONT_KILL_APP)
            } catch (e: Exception) {
                Log.e("AppHider", "Error disabling $pkg", e)
            }
        }
    }

    private fun enablePackages(pkgs: List<String>) {
        val pm = applicationContext.packageManager
        for (pkg in pkgs) {
            try {
                pm.setApplicationEnabledSetting(pkg,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP)
            } catch (e: Exception) {
                Log.e("AppHider", "Error enabling $pkg", e)
            }
        }
    }

    private fun launcherHide(pkgs: List<String>) {
        val pm = applicationContext.packageManager
        for (pkg in pkgs) {
            try {
                val launchIntent = pm.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    val comp = launchIntent.component
                    pm.setComponentEnabledSetting(comp!!,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP)
                }
            } catch (e: Exception) {
                Log.e("AppHider", "Error hiding launcher for $pkg", e)
            }
        }
    }

    private fun launcherUnhide(pkgs: List<String>) {
        val pm = applicationContext.packageManager
        for (pkg in pkgs) {
            try {
                val launchIntent = pm.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    val comp = launchIntent.component
                    pm.setComponentEnabledSetting(comp!!,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP)
                }
            } catch (e: Exception) {
                Log.e("AppHider", "Error unhiding launcher for $pkg", e)
            }
        }
    }

    private fun getUsageForPackages(pkgs: List<String>): Map<String, Long> {
        val result = mutableMapOf<String, Long>()
        try {
            val usm = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val end = System.currentTimeMillis()
            val begin = end - 1000L * 60L * 60L * 24L // last 24h
            val stats: List<UsageStats> = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, begin, end)
            for (pkg in pkgs) {
                val stat = stats.find { it.packageName == pkg }
                if (stat != null) {
                    result[pkg] = stat.totalTimeInForeground
                } else {
                    result[pkg] = 0L
                }
            }
        } catch (e: Exception) {
            Log.e("AppHider", "Error fetching usage stats", e)
        }
        return result
    }
}
