# AppHider PoC

Proof-of-concept Flutter app demonstrating three approaches to hide/block apps on Android:

1. PackageManager enable/disable (works for many user apps)
2. Custom-launcher-style hiding (disables LAUNCHER component)
3. Accessibility blocking overlay (detects launches and shows a blocking activity)

Features included:
- Select installed apps, choose strategies, schedule hide interval, hide/unhide.
- Usage stats (requires Usage Access permission).
- Strict mode hints for Device Owner features (requires adb/device owner provisioning).
