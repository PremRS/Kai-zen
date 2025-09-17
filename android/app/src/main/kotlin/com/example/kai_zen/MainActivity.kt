package com.example.kai_zen

import android.app.admin.DevicePolicyManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    private val CHANNEL = "com.example.kai_zen/channel"

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "disablePackages" -> {
                    val packages = call.argument<List<String>>("packages") ?: listOf()
                    val strict = call.argument<Boolean>("strictMode") ?: false
                    val ok = disablePackages(packages, strict)
                    result.success(ok)
                }
                "enablePackages" -> {
                    val packages = call.argument<List<String>>("packages") ?: listOf()
                    val ok = enablePackages(packages)
                    result.success(ok)
                }
                "launcherHidePackages" -> {
                    val packages = call.argument<List<String>>("packages") ?: listOf()
                    val ok = launcherHide(packages)
                    result.success(ok)
                }
                "launcherUnhidePackages" -> {
                    val packages = call.argument<List<String>>("packages") ?: listOf()
                    val ok = launcherUnhide(packages)
                    result.success(ok)
                }
                "startBlockingAccessibility" -> {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    result.success(true)
                }
                "stopBlockingAccessibility" -> {
                    try {
                        BlockingActivity.finishAll()
                        BlockingAccessibilityService.blockedPackages.clear()
                        result.success(true)
                    } catch (e: Exception) {
                        result.success(false)
                    }
                }
                "getUsageForPackages" -> {
                    val packages = call.argument<List<String>>("packages") ?: listOf()
                    val usage = getUsageStatsForPackages(packages)
                    result.success(usage)
                }
                else -> result.notImplemented()
            }
        }
    }

    private fun disablePackages(packages: List<String>, strict: Boolean): Boolean {
        val pm = packageManager
        var ok = true
        for (pkg in packages) {
            try {
                pm.setApplicationEnabledSetting(pkg,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP)
                Log.i("AppHider", "Disabled $pkg")
                if (strict) {
                    val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                    val adminComponent = ComponentName(this, DeviceAdminReceiverImpl::class.java)
                    if (dpm.isDeviceOwnerApp(packageName)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            dpm.setUninstallBlocked(adminComponent, pkg, true)
                        }
                    } else {
                        Log.w("AppHider", "Strict mode requested but app is not device owner")
                    }
                }
            } catch (e: Exception) {
                Log.e("AppHider", "Failed to disable $pkg: ${e.message} - falling back to overlay")
                try {
                    BlockingAccessibilityService.blockedPackages.add(pkg)
                    val intent = Intent(this, BlockingActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.putExtra("blockedPkg", pkg)
                    startActivity(intent)
                } catch (ex: Exception) {
                    Log.e("AppHider", "Fallback overlay failed for $pkg: ${ex.message}")
                }
                ok = false
            }
        }
        return ok
    }

    private fun enablePackages(packages: List<String>): Boolean {
        val pm = packageManager
        var ok = true
        for (pkg in packages) {
            try {
                pm.setApplicationEnabledSetting(pkg,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP)
                val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                val adminComponent = ComponentName(this, DeviceAdminReceiverImpl::class.java)
                if (dpm.isDeviceOwnerApp(packageName)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        dpm.setUninstallBlocked(adminComponent, pkg, false)
                    }
                }
                Log.i("AppHider", "Enabled $pkg")
                BlockingAccessibilityService.blockedPackages.remove(pkg)
                BlockingActivity.finishAll()
            } catch (e: Exception) {
                Log.e("AppHider", "Failed to enable $pkg: ${e.message} - clearing overlay if present")
                BlockingAccessibilityService.blockedPackages.remove(pkg)
                BlockingActivity.finishAll()
                ok = false
            }
        }
        return ok
    }

    private fun launcherHide(packages: List<String>): Boolean {
        val pm = packageManager
        var ok = true
        for (pkg in packages) {
            try {
                val intent = packageManager.getLaunchIntentForPackage(pkg)
                if (intent != null) {
                    val comp = intent.component
                    if (comp != null) {
                        pm.setComponentEnabledSetting(comp,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP)
                        Log.i("AppHider", "Launcher-hidden $pkg")
                    }
                }
            } catch (e: Exception) {
                Log.e("AppHider", "launcherHide failed for $pkg: ${e.message}")
                ok = false
            }
        }
        return ok
    }

    private fun launcherUnhide(packages: List<String>): Boolean {
        val pm = packageManager
        var ok = true
        for (pkg in packages) {
            try {
                val intent = packageManager.getLaunchIntentForPackage(pkg)
                if (intent != null) {
                    val comp = intent.component
                    if (comp != null) {
                        pm.setComponentEnabledSetting(comp,
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP)
                        Log.i("AppHider", "Launcher-unhidden $pkg")
                    }
                }
            } catch (e: Exception) {
                Log.e("AppHider", "launcherUnhide failed for $pkg: ${e.message}")
                ok = false
            }
        }
        return ok
    }

    private fun getUsageStatsForPackages(packages: List<String>): Map<String, Map<String, Long>> {
        val usageMap = mutableMapOf<String, Map<String, Long>>()
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val start = end - 1000L*60*60*24
        val stats: List<UsageStats> = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
        val byPkg = stats.associateBy { it.packageName }
        for (pkg in packages) {
            val s = byPkg[pkg]
            if (s != null) {
                usageMap[pkg] = mapOf(
                    "lastTimeUsed" to s.lastTimeUsed,
                    "totalTimeForeground" to s.totalTimeInForeground
                )
            } else {
                usageMap[pkg] = mapOf("lastTimeUsed" to 0L, "totalTimeForeground" to 0L)
            }
        }
        return usageMap
    }
}
