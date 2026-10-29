# PSG APEX Native Launcher — source build

Native Java Android launcher source for the PSG navy-and-gold executive experience, intended for Samsung Galaxy A20 / Android 11. This is a buildable source project intended for GitHub Actions or Android Studio; it is **not represented as already compiled or device-tested**.

## Included
- Android HOME/DEFAULT launcher activity and native interface.
- Ten workspace screens: Executive Home, Business HQ, Documents Vault, AI Command Centre, Communications, Personal & Family, Health & Fitness, Productivity, APEX Linux Mobile, Settings.
- Horizontal swipe workspace navigation and bottom navigation.
- Installed-app discovery using Android PackageManager, real app icons, launch intents, search, and long-press favourite pin/unpin.
- Persistent selected workspace, favourites and wallpaper URI.
- Android dialer, SMS, WhatsApp/WhatsApp Business launch attempts, image picker and document picker.
- APEX local web panels for `127.0.0.1:8080` and `127.0.0.1:8766`, plus read-only health request to `127.0.0.1:8765/api/health`.
- GitHub Actions workflow to build and upload a debug APK artifact.

## Build APK on GitHub
1. Upload/extract this source into `Felix2105/FSG-GodMode-Launcher` (preserve any existing work first) or another repository.
2. Push to `main` or `master`, or open **Actions → Build PSG APEX Native Launcher → Run workflow**.
3. Download the `PSG-APEX-Native-Launcher-debug-APK` artifact from a successful run; unzip it to obtain `app-debug.apk`.
4. Install on the Galaxy A20, set as default launcher, and test all device-specific functions before relying on it.

## Important limits / honest status
- Source has not been compiled in this workspace and the APK has not been installed or tested on a Galaxy A20.
- Android's document picker can access only locations Android exposes. Direct browsing of the proot Debian filesystem is **not** implemented as a privileged bridge; use Termux/shared storage or configure a separate bridge.
- The Linux/Business screens open existing local web services. They do not start, stop, repair, migrate or modify those services or the SQLite business database.
- The AI screen launches installed tools or a browser; no local AI model is bundled.
- “Quick Controls” is an in-app menu of common actions, not a replacement for Android's privileged system quick-settings panel.
- Health, Digital Wellbeing, app visibility, wallpaper permissions and localhost access can vary by Android build and installed apps.

## Data safety
The app uses its own preferences only. It does not intentionally write to the existing APEX backend, business database or recovery archive. Keep a separate copy of `~/apex-recovery/apex-snapshot-20261019-212550.tar.gz`.
