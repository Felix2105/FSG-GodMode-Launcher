# FSG God-Mode Launcher v2.1  
**Ubuntu Touch / Lomiri features · ProMaestro Solutions Group · Fix-It Kings**

A state-of-the-art Android home launcher that takes the best of **Ubuntu Touch (Lomiri)** and makes it more powerful, while wearing the official FSG navy-and-gold identity.

**Target:** Android 11+ (API 30+) · works from Android 6 (minSdk 23)  
**Build:** 100 % GitHub Actions – no Android Studio required  
**Package:** `com.fsg.godmode`

---

## Ubuntu Touch features (enhanced)

| Ubuntu Touch / Lomiri concept | God-Mode implementation |
|-------------------------------|-------------------------|
| **Top Indicators** | Live clock + battery % (charging aware) + network placeholder. Tap opens Quick Settings. |
| **Scopes** | 5 full scopes: Home · Dash · Automation · Business · Settings. Horizontal chips + swipe. |
| **Bottom-edge gesture** | Swipe up from bottom → full searchable App Drawer (Dash overlay). |
| **Left-edge gesture** | Swipe right from left edge → previous scope. |
| **Right-edge gesture** | Swipe left from right edge → Quick Settings panel (theme, wallpaper, Tasker, etc.). |
| **Dash** | Dedicated searchable Apps scope with live filter. |
| **HUD / long-press** | Long-press centre of screen shows gesture reminder. |
| **Convergent cards** | Clean card-based UI, gold accents on navy panels. |
| **Back behaviour** | Closes overlays first, then returns to Home scope (never exits launcher). |

### Extra power beyond stock Ubuntu Touch
- Runtime theme engine (Navy-Gold official, Midnight, Platinum, High-Contrast)
- Tasker profile trigger, Zapier webhook, WhatsApp quick-send, Termux deep link
- APEX local dashboard + health endpoints
- AI Maestro card (toggleable)
- Live wallpaper picker
- Gesture sensitivity control
- Full launcher settings page
- Termux:Widget scripts included

---

## Gesture map (memorise once)

```
↑  Bottom edge     →  App Drawer (searchable)
→  Left edge       →  Previous scope
←  Right edge      →  Quick Settings panel
↔  Centre swipe    →  Change scope
●  Long-press      →  HUD reminder
   Tap top bar     →  Quick Settings
```

---

## Colour palette (official logo)

```
Primary Navy     #0D141F
Panel Navy       #1B2737
Panel Elevated   #233143
Accent Gold      #D4AF37
Gold Soft        #DCB756
White            #F5F7FA
Muted Steel      #AEBBCA
```

---

## Quick Start – Build on GitHub

1. Create repository `fsg-godmode-launcher`.
2. Upload **everything** inside this folder to the **root**.
3. Actions → run **Build FSG God-Mode APK**.
4. Download artifact `FSG-GodMode-Launcher-debug-apk` → install `app-debug.apk`.
5. Settings → Apps → Default apps → Home app → **FSG God Mode**.

---

## Project structure

```
FSG-GodMode-Launcher/
├── app/src/main/java/com/fsg/godmode/
│   ├── MainActivity.java          ← scopes, edges, drawer, indicators
│   ├── ThemeManager.java
│   ├── SettingsActivity.java
│   └── WebPanelActivity.java
├── app/src/main/res/…
├── .github/workflows/build.yml
├── termux-widget/
├── docs/fsg-logo.jpg
└── README.md
```

---

**God Mode + Ubuntu Touch power. Precision. Innovation. Excellence.**
