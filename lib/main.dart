// lib/main.dart
import 'dart:async';
import 'package:flutter/material.dart';
import 'package:device_apps/device_apps.dart';
import 'package:flutter/services.dart';
import 'package:android_alarm_manager_plus/android_alarm_manager_plus.dart';
import 'package:intl/intl.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await AndroidAlarmManager.initialize();
  runApp(MyApp());
}

const platform = MethodChannel('com.example.app_hider/channel');

class MyApp extends StatefulWidget {
  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  List<Application>? apps;
  Set<String> selectedPackageNames = {};
  bool useDeviceDisable = true;
  bool useLauncher = false;
  bool useAccessibility = false;
  bool strictMode = false;
  TimeOfDay startTime = TimeOfDay(hour: 9, minute: 0);
  TimeOfDay endTime = TimeOfDay(hour: 17, minute: 0);

  @override
  void initState() {
    super.initState();
    _loadApps();
  }

  Future<void> _loadApps() async {
    final all = await DeviceApps.getInstalledApplications(
      includeSystemApps: false,
      includeAppIcons: false,
      onlyAppsWithLaunchIntent: true,
    );
    setState(() { apps = all; });
  }

  Future<void> _applyHide() async {
    final pkgs = selectedPackageNames.toList();
    final payload = {
      'packages': pkgs,
      'strictMode': strictMode,
    };
    if (useDeviceDisable) {
      await platform.invokeMethod('disablePackages', payload);
    }
    if (useLauncher) {
      await platform.invokeMethod('launcherHidePackages', payload);
    }
    if (useAccessibility) {
      await platform.invokeMethod('startBlockingAccessibility', payload);
    }

    final now = DateTime.now();
    var todayEnd = DateTime(now.year, now.month, now.day, endTime.hour, endTime.minute);
    if (todayEnd.isBefore(now)) {
      todayEnd = todayEnd.add(Duration(days: 1));
    }
    final millis = todayEnd.millisecondsSinceEpoch;
    await AndroidAlarmManager.oneShotAt(
      DateTime.fromMillisecondsSinceEpoch(millis),
      (pkgs.hashCode & 0x7fffffff),
      _alarmUnhide,
      exact: true,
      wakeup: true,
      params: pkgs,
    );

    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Hidden — will unhide at ${DateFormat.jm().format(todayEnd)}')));
  }

  static Future<void> _alarmUnhide(List<dynamic>? params) async {
    final pkgs = params?.cast<String>() ?? [];
    final platform = MethodChannel('com.example.app_hider/channel');
    try {
      await platform.invokeMethod('enablePackages', {'packages': pkgs});
      await platform.invokeMethod('stopBlockingAccessibility', {'packages': pkgs});
      await platform.invokeMethod('launcherUnhidePackages', {'packages': pkgs});
    } catch (e) {}
  }

  Future<void> _unhideNow() async {
    final pkgs = selectedPackageNames.toList();
    await platform.invokeMethod('enablePackages', {'packages': pkgs});
    await platform.invokeMethod('stopBlockingAccessibility', {'packages': pkgs});
    await platform.invokeMethod('launcherUnhidePackages', {'packages': pkgs});
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Unhidden')));
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'App Hider PoC',
      home: Scaffold(
        appBar: AppBar(title: Text('App Hider PoC')),
        body: apps == null ? Center(child: CircularProgressIndicator()) : Column(
          children: [
            SwitchListTile(
              title: Text('Use Device Disable (PackageManager)'),
              value: useDeviceDisable,
              onChanged: (v) => setState(() { useDeviceDisable = v; }),
            ),
            SwitchListTile(
              title: Text('Use Custom Launcher hiding'),
              value: useLauncher,
              onChanged: (v) => setState(() { useLauncher = v; }),
            ),
            SwitchListTile(
              title: Text('Use Accessibility blocking overlay'),
              value: useAccessibility,
              onChanged: (v) => setState(() { useAccessibility = v; }),
            ),
            CheckboxListTile(
              title: Text('Strict mode (requires Device Owner)'),
              value: strictMode,
              onChanged: (v) => setState(() { strictMode = v ?? false; }),
            ),
            ListTile(
              title: Text('Start time: ${startTime.format(context)}'),
              onTap: () async {
                final t = await showTimePicker(context: context, initialTime: startTime);
                if (t != null) setState(() => startTime = t);
              },
            ),
            ListTile(
              title: Text('End time: ${endTime.format(context)}'),
              onTap: () async {
                final t = await showTimePicker(context: context, initialTime: endTime);
                if (t != null) setState(() => endTime = t);
              },
            ),
            Expanded(
              child: ListView.builder(
                itemCount: apps!.length,
                itemBuilder: (context, i) {
                  final a = apps![i];
                  final isSel = selectedPackageNames.contains(a.packageName);
                  return ListTile(
                    title: Text(a.appName),
                    subtitle: Text(a.packageName),
                    trailing: Checkbox(value: isSel, onChanged: (v) {
                      setState(() {
                        if (v == true) selectedPackageNames.add(a.packageName);
                        else selectedPackageNames.remove(a.packageName);
                      });
                    }),
                  );
                },
              ),
            ),
            Row(
              children: [
                Expanded(child: ElevatedButton(onPressed: _applyHide, child: Text('Hide selected'))),
                Expanded(child: ElevatedButton(onPressed: _unhideNow, child: Text('Unhide now'))),
              ],
            ),
            ElevatedButton(onPressed: () async {
              final res = await platform.invokeMethod('getUsageForPackages', {'packages': selectedPackageNames.toList()});
              showDialog(context: context, builder: (_) => AlertDialog(title: Text('Usage stats'), content: Text('$res')));
            }, child: Text('Show usage stats for selected apps')),
            SizedBox(height: 8),
          ],
        ),
      ),
    );
  }
}
