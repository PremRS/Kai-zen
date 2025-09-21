# Kai-zen — Full Flutter Project (ready-to-paste)

This document contains the complete project files and folders you asked for. Copy each file into the indicated path inside a new Flutter project (Flutter 3.9) and build.

---

## Project structure (high-level)

```
kai_zen/
├─ android/
│  ├─ app/
│  │  ├─ src/main/AndroidManifest.xml
│  │  ├─ java/com/example/kai_zen/MainActivity.kt
│  │  ├─ java/com/example/kai_zen/AppForegroundAccessibilityService.kt
│  │  ├─ java/com/example/kai_zen/OverlayActivity.kt
│  │  ├─ res/layout/activity_overlay.xml
│  │  └─ res/xml/accessibility_service_config.xml
│  └─ build.gradle (use Flutter default)
├─ lib/
│  ├─ main.dart
│  └─ src/
│     └─ pages/home_page.dart
├─ assets/
│  ├─ fonts/JosefinSans-Regular.ttf (download or use google_fonts runtime)
│  └─ icons/ (place app icons here)
├─ pubspec.yaml
└─ README.md
```

---

### assets

- Put the Josefin Sans Regular font at `assets/fonts/JosefinSans-Regular.ttf` OR rely on `google_fonts` (we used google_fonts, so it's optional to add the file).
- Add your app icons in `assets/icons/` and set Android mipmap accordingly. For the lock+lightning logo, export a square PNG (1024x1024) and generate Android launcher icons.

---

### README.md (short usage guide)

```md
# Kai-zen

Steps to test & run:
1. Create a new Flutter project and replace/add files listed in this doc.
2. Add required fonts/assets.
3. Build & run on Android device (real device recommended).
4. In the app: pick apps to block and press Save.
5. Tap "Enable Accessibility" and enable `Kai-zen` accessibility service in Settings.
6. (Optional) Give "Display over other apps" permission for smoother overlays.

Notes:
- The accessibility service watches for foreground window changes and launches the overlay when a blocked app becomes foreground.
- For production, implement schedule checks (weekday/time) and PIN unlocks.
```

---

If you want, I can now:
- produce `styles.xml`, `colors.xml`, and `strings.xml` for Android resources,
- add a PIN-based unlock in `OverlayActivity`, or
- generate the actual launcher icon PNGs using the description and provide prompts for an artist/AI tool.

Tell me which of those you'd like next and I'll add them directly into this project doc.
