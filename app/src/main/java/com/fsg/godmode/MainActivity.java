package com.fsg.godmode;

import android.widget.ImageView;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import org.json.JSONObject;

/**
 * FSG God-Mode Launcher – Ubuntu Touch / Lomiri inspired, but more powerful.
 *
 * Ubuntu Touch features implemented & upgraded:
 *  - Top indicator bar (time, battery, network placeholders, God-Mode status)
 *  - Multi-scope / multi-page workspaces with live indicators
 *  - Bottom-edge swipe → full App Drawer (Lomiri-style)
 *  - Left-edge swipe → previous scope
 *  - Right-edge swipe → Quick Settings / Indicators panel
 *  - Horizontal swipe between scopes
 *  - Searchable Dash (Apps scope with live filter)
 *  - Card-based convergent UI with gold-on-navy FSG identity
 *  - Long-press HUD-style quick actions
 *  - Runtime themes, gesture sensitivity, live wallpaper hook
 *  - Deep integrations: Tasker, WhatsApp, Termux, Zapier, APEX
 *  - AI Maestro card + Automation hub
 */
public class MainActivity extends Activity {

    private static final int SCOPE_HOME = 0;
    private static final int SCOPE_APPS = 1;
    private static final int SCOPE_AUTOMATION = 2;
    private static final int SCOPE_BUSINESS = 3;
    private static final int SCOPE_SETTINGS = 4;
    private static final int SCOPE_COUNT = 5;

    private static final String[] SCOPE_NAMES = {
            "HOME", "DASH", "AUTO", "BIZ", "SET"
    };

    private static final float EDGE_ZONE_DP = 28f;
    private static final float BOTTOM_EDGE_DP = 36f;

    private ThemeManager theme;
    private int currentScope = SCOPE_HOME;
    private float downX, downY;
    private long downTime;
    private boolean edgeLeft, edgeRight, edgeBottom;
    private boolean drawerOpen = false;
    private boolean quickPanelOpen = false;

    private FrameLayout rootFrame;
    private LinearLayout mainColumn;
    private LinearLayout scopeContent;
    private TextView clockView, batteryView;
    private ScrollView contentScroll;
    private View drawerOverlay, quickPanelOverlay;

    private List<ResolveInfo> installedApps = new ArrayList<>();
    private String appFilter = "";

    private final Handler clockHandler = new Handler(Looper.getMainLooper());
    private final Runnable clockTick = new Runnable() {
        @Override public void run() {
            updateIndicators();
            clockHandler.postDelayed(this, 30_000);
        }
    };

    private BroadcastReceiver batteryReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        theme = new ThemeManager(this);

        getWindow().setStatusBarColor(theme.bg);
        getWindow().setNavigationBarColor(theme.bg);
        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        loadInstalledApps();
        buildRoot();
        showScope(currentScope);
        startClock();
        registerBattery();
    }

    @Override
    protected void onResume() {
        super.onResume();
        theme = new ThemeManager(this);
        getWindow().setStatusBarColor(theme.bg);
        getWindow().setNavigationBarColor(theme.bg);
        updateIndicators();
        showScope(currentScope);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clockHandler.removeCallbacks(clockTick);
        if (batteryReceiver != null) {
            try { unregisterReceiver(batteryReceiver); } catch (Exception ignored) {}
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerOpen) { closeDrawer(); return; }
        if (quickPanelOpen) { closeQuickPanel(); return; }
        if (currentScope != SCOPE_HOME) {
            currentScope = SCOPE_HOME;
            showScope(currentScope);
        }
    }

    private void buildRoot() {
        rootFrame = new FrameLayout(this);
        rootFrame.setBackgroundColor(theme.bg);

        mainColumn = new LinearLayout(this);
        mainColumn.setOrientation(LinearLayout.VERTICAL);
        mainColumn.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));

        mainColumn.addView(buildIndicatorBar(), new LinearLayout.LayoutParams(-1, dp(36)));
        mainColumn.addView(buildScopeStrip(), new LinearLayout.LayoutParams(-1, dp(40)));

        contentScroll = new ScrollView(this);
        contentScroll.setFillViewport(true);
        contentScroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        contentScroll.setBackgroundColor(theme.bg);
        scopeContent = new LinearLayout(this);
        scopeContent.setOrientation(LinearLayout.VERTICAL);
        scopeContent.setPadding(dp(14), dp(8), dp(14), dp(80));
        contentScroll.addView(scopeContent);
        mainColumn.addView(contentScroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        rootFrame.addView(mainColumn);
        rootFrame.setOnTouchListener((v, e) -> handleGlobalTouch(e));
        setContentView(rootFrame);
    }

    private LinearLayout buildIndicatorBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setBackgroundColor(theme.panel);
        bar.setPadding(dp(12), 0, dp(12), 0);

        TextView brand = txt("FSG", 11, theme.gold, true);
        bar.addView(brand, new LinearLayout.LayoutParams(0, -2, 1));

        clockView = txt("--:--", 13, theme.white, true);
        clockView.setGravity(Gravity.CENTER);
        bar.addView(clockView, new LinearLayout.LayoutParams(0, -2, 1));

        LinearLayout right = new LinearLayout(this);
        right.setOrientation(LinearLayout.HORIZONTAL);
        right.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        batteryView = txt("●  --%", 11, theme.gold, false);
        right.addView(batteryView);
        TextView net = txt("  📶", 12, theme.muted, false);
        right.addView(net);
        bar.addView(right, new LinearLayout.LayoutParams(0, -2, 1));

        bar.setOnClickListener(v -> openQuickPanel());
        return bar;
    }

    private void updateIndicators() {
        if (clockView != null) {
            String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            clockView.setText(time);
        }
    }

    private void registerBattery() {
        batteryReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context c, Intent i) {
                int level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
                int pct = level >= 0 ? (level * 100) / scale : -1;
                int status = i.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean charging = status == BatteryManager.BATTERY_STATUS_CHARGING
                        || status == BatteryManager.BATTERY_STATUS_FULL;
                if (batteryView != null && pct >= 0) {
                    String icon = charging ? "⚡" : "●";
                    int col = pct <= 15 ? theme.danger : (pct <= 30 ? theme.goldSoft : theme.gold);
                    batteryView.setText(icon + " " + pct + "%");
                    batteryView.setTextColor(col);
                }
            }
        };
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    private HorizontalScrollView buildScopeStrip() {
        HorizontalScrollView hsv = new HorizontalScrollView(this);
        hsv.setHorizontalScrollBarEnabled(false);
        hsv.setBackgroundColor(theme.bg);

        LinearLayout strip = new LinearLayout(this);
        strip.setOrientation(LinearLayout.HORIZONTAL);
        strip.setPadding(dp(6), dp(4), dp(6), dp(4));
        strip.setGravity(Gravity.CENTER_VERTICAL);

        for (int i = 0; i < SCOPE_COUNT; i++) {
            final int idx = i;
            TextView chip = txt((i == currentScope ? "● " : "○ ") + SCOPE_NAMES[i], 12,
                    i == currentScope ? theme.gold : theme.muted, true);
            chip.setBackground(pill(i == currentScope ? theme.panelElevated : theme.panel));
            chip.setPadding(dp(14), dp(6), dp(14), dp(6));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
            lp.setMargins(dp(4), 0, dp(4), 0);
            strip.addView(chip, lp);
            chip.setOnClickListener(v -> {
                currentScope = idx;
                showScope(currentScope);
            });
        }
        hsv.addView(strip);
        return hsv;
    }

    private boolean handleGlobalTouch(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        int w = rootFrame.getWidth();
        int h = rootFrame.getHeight();
        float edge = dp(EDGE_ZONE_DP);
        float bottom = dp(BOTTOM_EDGE_DP);
        int sens = theme.getGestureSensitivity();

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = x; downY = y; downTime = System.currentTimeMillis();
                edgeLeft = x < edge;
                edgeRight = x > w - edge;
                edgeBottom = y > h - bottom;
                return true;

            case MotionEvent.ACTION_UP:
                float dx = x - downX;
                float dy = y - downY;
                long dt = System.currentTimeMillis() - downTime;

                if (edgeBottom && dy < -dp(sens) && Math.abs(dy) > Math.abs(dx)) {
                    openDrawer();
                    return true;
                }
                if (edgeLeft && dx > dp(sens) && Math.abs(dx) > Math.abs(dy)) {
                    currentScope = (currentScope - 1 + SCOPE_COUNT) % SCOPE_COUNT;
                    showScope(currentScope);
                    return true;
                }
                if (edgeRight && dx < -dp(sens) && Math.abs(dx) > Math.abs(dy)) {
                    openQuickPanel();
                    return true;
                }
                if (!edgeLeft && !edgeRight && !edgeBottom
                        && Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > dp(sens)) {
                    if (dx < 0) currentScope = Math.min(currentScope + 1, SCOPE_COUNT - 1);
                    else currentScope = Math.max(currentScope - 1, 0);
                    showScope(currentScope);
                    return true;
                }
                if (dt > 550 && Math.abs(dx) < dp(20) && Math.abs(dy) < dp(20)
                        && !edgeLeft && !edgeRight && !edgeBottom) {
                    Toast.makeText(this,
                            "HUD · Bottom-edge ↑ apps · Left-edge scopes · Right-edge settings · Swipe pages",
                            Toast.LENGTH_LONG).show();
                    return true;
                }
                break;
        }
        return false;
    }

    private void openDrawer() {
        if (drawerOpen) return;
        closeQuickPanel();
        drawerOpen = true;

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setBackground(cardBg(theme.panel, 20));
        panel.setPadding(dp(16), dp(12), dp(16), dp(24));

        TextView handle = txt("━━━", 14, theme.muted, false);
        handle.setGravity(Gravity.CENTER);
        panel.addView(handle);

        TextView title = txt("APP DRAWER  ·  DASH", 14, theme.gold, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(4), 0, dp(10));
        panel.addView(title);

        EditText search = new EditText(this);
        search.setHint("Search apps…");
        search.setHintTextColor(theme.muted);
        search.setTextColor(theme.white);
        search.setBackground(pill(theme.panelElevated));
        search.setPadding(dp(14), dp(10), dp(14), dp(10));
        search.setSingleLine(true);
        panel.addView(search, new LinearLayout.LayoutParams(-1, -2));

        ScrollView sv = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(0, dp(8), 0, 0);
        sv.addView(list);
        panel.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1f));

        Runnable refresh = () -> {
            list.removeAllViews();
            PackageManager pm = getPackageManager();
            String q = search.getText().toString().toLowerCase().trim();
            int count = 0;
            for (ResolveInfo ri : installedApps) {
                String label = ri.loadLabel(pm).toString();
                if (!q.isEmpty() && !label.toLowerCase().contains(q)) continue;
                if (ri.activityInfo.packageName.equals(getPackageName())) continue;
                addDrawerAppRow(list, label, ri);
                if (++count >= 50) break;
            }
            if (count == 0) {
                TextView empty = txt("No matching apps", 13, theme.muted, false);
                empty.setGravity(Gravity.CENTER);
                empty.setPadding(0, dp(20), 0, 0);
                list.addView(empty);
            }
        };
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) { refresh.run(); }
            public void afterTextChanged(Editable s) {}
        });
        refresh.run();

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, (int) (rootFrame.getHeight() * 0.72f));
        lp.gravity = Gravity.BOTTOM;
        lp.setMargins(dp(8), 0, dp(8), dp(8));

        drawerOverlay = panel;
        rootFrame.addView(panel, lp);

        View dim = new View(this);
        dim.setBackgroundColor(Color.argb(160, 0, 0, 0));
        dim.setOnClickListener(v -> closeDrawer());
        rootFrame.addView(dim, 0, new FrameLayout.LayoutParams(-1, -1));
        drawerOverlay.setTag(dim);
    }

    private void addDrawerAppRow(LinearLayout list, String label, ResolveInfo ri) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(10), dp(10), dp(10));
        row.setBackground(cardBg(theme.panelElevated, 12));

        TextView icon = txt(label.substring(0, 1).toUpperCase(), 16, theme.gold, true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(pill(theme.panel));
        row.addView(icon, new LinearLayout.LayoutParams(dp(36), dp(36)));

        TextView name = txt(label, 14, theme.white, false);
        name.setPadding(dp(12), 0, 0, 0);
        row.addView(name, new LinearLayout.LayoutParams(0, -2, 1));

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = dp(6);
        list.addView(row, p);

        row.setOnClickListener(v -> {
            try {
                Intent i = new Intent(Intent.ACTION_MAIN);
                i.setClassName(ri.activityInfo.packageName, ri.activityInfo.name);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                closeDrawer();
            } catch (Exception ex) {
                Toast.makeText(this, "Cannot launch", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void closeDrawer() {
        if (!drawerOpen) return;
        drawerOpen = false;
        if (drawerOverlay != null) {
            Object dim = drawerOverlay.getTag();
            rootFrame.removeView(drawerOverlay);
            if (dim instanceof View) rootFrame.removeView((View) dim);
            drawerOverlay = null;
        }
    }

    private void openQuickPanel() {
        if (quickPanelOpen) return;
        closeDrawer();
        quickPanelOpen = true;

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setBackground(cardBg(theme.panel, 18));
        panel.setPadding(dp(16), dp(16), dp(16), dp(20));

        panel.addView(txt("QUICK SETTINGS", 14, theme.gold, true));
        panel.addView(txt("Indicators & God-Mode toggles", 11, theme.muted, false));

        addQuickToggle(panel, "🎨  Theme · " + theme.getThemeName(), () -> {
            int next = (theme.getThemeId() + 1) % 4;
            theme.apply(next);
            Toast.makeText(this, "Theme → " + theme.getThemeName(), Toast.LENGTH_SHORT).show();
            recreate();
        });
        addQuickToggle(panel, "🖼  Live Wallpaper", () -> {
            try {
                startActivity(Intent.createChooser(new Intent(Intent.ACTION_SET_WALLPAPER), "Wallpaper"));
            } catch (Exception e) {
                Toast.makeText(this, "Wallpaper picker unavailable", Toast.LENGTH_SHORT).show();
            }
        });
        addQuickToggle(panel, "🏠  Set Default Home", () -> {
            try { startActivity(new Intent(Settings.ACTION_HOME_SETTINGS)); }
            catch (Exception e) { Toast.makeText(this, "Settings → Default apps → Home", Toast.LENGTH_LONG).show(); }
        });
        addQuickToggle(panel, "⚙  Full Settings", () -> {
            closeQuickPanel();
            startActivity(new Intent(this, SettingsActivity.class));
        });
        addQuickToggle(panel, "⚡  Tasker", () -> launchPackage("net.dinglisch.android.taskerm"));
        addQuickToggle(panel, "💬  WhatsApp", () -> launchPackage("com.whatsapp", "com.whatsapp.w4b"));
        addQuickToggle(panel, "🖥  Termux", () -> launchPackage("com.termux"));
        addQuickToggle(panel, "✦  AI Maestro " + (theme.isAiEnabled() ? "ON" : "OFF"), () -> {
            theme.setAiEnabled(!theme.isAiEnabled());
            Toast.makeText(this, "AI → " + (theme.isAiEnabled() ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
        });

        TextView close = txt("CLOSE PANEL", 13, theme.bg, true);
        close.setGravity(Gravity.CENTER);
        close.setBackground(pill(theme.gold));
        close.setPadding(dp(12), dp(12), dp(12), dp(12));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.topMargin = dp(16);
        panel.addView(close, cp);
        close.setOnClickListener(v -> closeQuickPanel());

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(280), -1);
        lp.gravity = Gravity.END;
        lp.setMargins(0, dp(8), dp(8), dp(8));
        quickPanelOverlay = panel;
        rootFrame.addView(panel, lp);

        View dim = new View(this);
        dim.setBackgroundColor(Color.argb(140, 0, 0, 0));
        dim.setOnClickListener(v -> closeQuickPanel());
        rootFrame.addView(dim, 0, new FrameLayout.LayoutParams(-1, -1));
        panel.setTag(dim);
    }

    private void addQuickToggle(LinearLayout panel, String label, Runnable action) {
        TextView t = txt(label, 14, theme.white, false);
        t.setBackground(cardBg(theme.panelElevated, 12));
        t.setPadding(dp(14), dp(12), dp(14), dp(12));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.topMargin = dp(8);
        panel.addView(t, p);
        t.setOnClickListener(v -> action.run());
    }

    private void closeQuickPanel() {
        if (!quickPanelOpen) return;
        quickPanelOpen = false;
        if (quickPanelOverlay != null) {
            Object dim = quickPanelOverlay.getTag();
            rootFrame.removeView(quickPanelOverlay);
            if (dim instanceof View) rootFrame.removeView((View) dim);
            quickPanelOverlay = null;
        }
    }

    private void showScope(int scope) {
        currentScope = scope;
        scopeContent.removeAllViews();
        rebuildScopeStrip();

        switch (scope) {
            case SCOPE_HOME: buildHomeScope(); break;
            case SCOPE_APPS: buildDashScope(); break;
            case SCOPE_AUTOMATION: buildAutomationScope(); break;
            case SCOPE_BUSINESS: buildBusinessScope(); break;
            case SCOPE_SETTINGS: buildSettingsScope(); break;
        }
        contentScroll.scrollTo(0, 0);
    }

    private void rebuildScopeStrip() {
        if (mainColumn.getChildCount() > 1) {
            mainColumn.removeViewAt(1);
            mainColumn.addView(buildScopeStrip(), 1, new LinearLayout.LayoutParams(-1, dp(40)));
        }
    }

    private void buildHomeScope() {
        addGreeting();
        addSection("SCOPES & WORKSPACES", "Ubuntu-style cards · FSG power");

        addTile("▦", "APEX DASHBOARD", "Local command centre · :8080",
                () -> openLocalWeb("http://127.0.0.1:8080/", "APEX Dashboard"));
        addTile("▤", "BUSINESS HQ", "Clients · jobs · quotes · invoices",
                () -> openLocalWeb("http://127.0.0.1:8080/business-hq.html", "Business HQ"));
        addTile("⌘", "LINUX STATUS", "APEX backend health",
                () -> openLocalWeb("http://127.0.0.1:8765/api/health", "Linux Status"));

        if (theme.isAiEnabled()) {
            addTile("✦", "MAESTRO ASSISTANT", "AI integration not configured",
                    () -> Toast.makeText(this,
                            "Maestro AI ready.\nHook a local LLM or cloud key in Settings.",
                            Toast.LENGTH_LONG).show());
        }

        addSection("FAVOURITES", "One-tap launch");
        addFavouriteRow();

        addSection("GESTURE GUIDE", "Lomiri-inspired · enhanced");
        TextView guide = txt(
                "↑ Bottom edge   →  App Drawer (Dash)\n" +
                "→ Left edge     →  Previous scope\n" +
                "← Right edge    →  Quick Settings\n" +
                "↔ Swipe centre  →  Change scope\n" +
                "● Long-press    →  HUD reminder\n" +
                "Tap top bar     →  Quick Settings",
                12, theme.muted, false);
        guide.setPadding(dp(6), dp(4), dp(6), dp(12));
        scopeContent.addView(guide);
    }

    private void addGreeting() {
        String hour = new SimpleDateFormat("H", Locale.getDefault()).format(new Date());
        int h = Integer.parseInt(hour);
        String greet = h < 12 ? "Good morning" : h < 18 ? "Good afternoon" : "Good evening";
        TextView g = txt(greet + ",", 16, theme.muted, false);
        scopeContent.addView(g);
        TextView name = txt("Commander", 22, theme.white, true);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(-1, -2);
        np.bottomMargin = dp(10);
        scopeContent.addView(name, np);
    }

    private void addFavouriteRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        String[][] favs = {
                {"📱", "WhatsApp", "com.whatsapp"},
                {"⚡", "Tasker", "net.dinglisch.android.taskerm"},
                {"🖥", "Termux", "com.termux"},
                {"⚙", "Settings", null}
        };
        for (String[] f : favs) {
            LinearLayout cell = new LinearLayout(this);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER);
            cell.setBackground(cardBg(theme.panelElevated, 14));
            cell.setPadding(dp(8), dp(12), dp(8), dp(12));
            cell.addView(txt(f[0], 22, theme.gold, true));
            cell.addView(txt(f[1], 10, theme.muted, false));
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, -2, 1);
            cp.setMargins(dp(3), 0, dp(3), 0);
            row.addView(cell, cp);
            final String pkg = f[2];
            cell.setOnClickListener(v -> {
                if (pkg == null) startActivity(new Intent(this, SettingsActivity.class));
                else launchPackage(pkg, "com.whatsapp.w4b");
            });
        }
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, -2);
        rp.bottomMargin = dp(12);
        scopeContent.addView(row, rp);
    }

    private void buildDashScope() {
        addSection("DASH", "Searchable app launcher · Ubuntu Touch style");

        EditText search = new EditText(this);
        search.setHint("Filter apps…");
        search.setHintTextColor(theme.muted);
        search.setTextColor(theme.white);
        search.setBackground(pill(theme.panel));
        search.setPadding(dp(14), dp(10), dp(14), dp(10));
        search.setSingleLine(true);
        search.setText(appFilter);
        scopeContent.addView(search, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout listHost = new LinearLayout(this);
        listHost.setOrientation(LinearLayout.VERTICAL);
        scopeContent.addView(listHost);

        Runnable refresh = () -> {
            listHost.removeAllViews();
            appFilter = search.getText().toString().toLowerCase().trim();
            PackageManager pm = getPackageManager();
            int shown = 0;
            for (ResolveInfo ri : installedApps) {
                String label = ri.loadLabel(pm).toString();
                if (!appFilter.isEmpty() && !label.toLowerCase().contains(appFilter)) continue;
                if (ri.activityInfo.packageName.equals(getPackageName())) continue;
                addAppTile(listHost, label, ri);
                if (++shown >= 45) break;
            }
            TextView count = txt(shown + " apps shown", 11, theme.muted, false);
            count.setGravity(Gravity.CENTER);
            count.setPadding(0, dp(8), 0, 0);
            listHost.addView(count);
        };
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) { refresh.run(); }
            public void afterTextChanged(Editable s) {}
        });
        refresh.run();
    }

    private void addAppTile(LinearLayout host, String label, ResolveInfo ri) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(12), dp(10), dp(12), dp(10));
        card.setBackground(cardBg(theme.panel, 12));

        ImageView icon = new ImageView(this);
        icon.setImageDrawable(ri.loadIcon(getPackageManager()));
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setPadding(dp(4), dp(4), dp(4), dp(4));
        icon.setBackground(pill(theme.panelElevated));
        card.addView(icon, new LinearLayout.LayoutParams(dp(38), dp(38)));

        TextView name = txt(label, 14, theme.white, false);
        name.setPadding(dp(12), 0, 0, 0);
        card.addView(name, new LinearLayout.LayoutParams(0, -2, 1));
        card.addView(txt("›", 20, theme.gold, true));

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(58));
        p.topMargin = dp(6);
        host.addView(card, p);

        card.setOnClickListener(v -> {
            try {
                Intent i = new Intent(Intent.ACTION_MAIN);
                i.setClassName(ri.activityInfo.packageName, ri.activityInfo.name);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            } catch (Exception e) {
                Toast.makeText(this, "Cannot launch " + label, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buildAutomationScope() {
        addSection("AUTOMATION HUB", "Tasker · Zapier · WhatsApp · Termux · APEX");

        addTile("⚡", "RUN TASKER PROFILE", "Fire task named FSG_GodMode",
                () -> {
                    try {
                        Intent i = new Intent("net.dinglisch.android.tasker.ACTION_TASK");
                        i.putExtra("task_name", "FSG_GodMode");
                        startActivity(i);
                    } catch (Exception e) {
                        Toast.makeText(this, "Install Tasker & create profile FSG_GodMode", Toast.LENGTH_LONG).show();
                    }
                });
        addTile("🔗", "ZAPIER WEBHOOK", "Send an event to your configured catch hook",
                this::sendZapierWebhook);
        addTile("💬", "WHATSAPP QUICK SEND", "Share sheet / direct chat",
                () -> {
                    try {
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("text/plain");
                        i.setPackage("com.whatsapp");
                        i.putExtra(Intent.EXTRA_TEXT, "From FSG God Mode");
                        startActivity(i);
                    } catch (Exception e) {
                        launchPackage("com.whatsapp", "com.whatsapp.w4b");
                    }
                });
        addTile("🖥", "TERMUX SESSION", "Open terminal",
                () -> launchPackage("com.termux"));
        addTile("📡", "APEX HEALTH CHECK", "Open the local backend health endpoint",
                () -> openLocalWeb("http://127.0.0.1:8765/api/health", "APEX Health"));

        addSection("POWER TIPS", null);
        TextView tips = txt(
                "• Create Tasker profile “FSG_GodMode” for one-tap automation\n" +
                "• Paste your Zapier catch URL in Settings\n" +
                "• Bottom-edge drawer works from any scope\n" +
                "• Right-edge panel = instant toggles",
                12, theme.muted, false);
        tips.setPadding(dp(4), dp(4), dp(4), dp(8));
        scopeContent.addView(tips);
    }

    /**
     * Sends a small JSON event to the user-configured Zapier Catch Hook.
     * Runs on a worker thread so the launcher UI remains responsive.
     */
    private void sendZapierWebhook() {
        final String hook = theme.getZapierUrl() == null ? "" : theme.getZapierUrl().trim();
        if (!hook.startsWith("https://hooks.zapier.com/hooks/catch/")
                || hook.endsWith("/catch/") || hook.contains("YOUR_HOOK")) {
            Toast.makeText(this, "Open Settings and save your complete Zapier Catch Hook URL first.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, "Sending Zapier test event…", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            HttpURLConnection connection = null;
            int code = -1;
            try {
                JSONObject payload = new JSONObject();
                payload.put("source", "FSG God-Mode Launcher");
                payload.put("event", "manual_trigger");
                payload.put("timestamp", System.currentTimeMillis());
                URL url = new URL(hook);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                byte[] body = payload.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
                connection.setFixedLengthStreamingMode(body.length);
                try (OutputStream out = connection.getOutputStream()) {
                    out.write(body);
                }
                code = connection.getResponseCode();
            } catch (Exception ignored) {
                // Error is reported on the main thread below.
            } finally {
                if (connection != null) connection.disconnect();
            }
            final int result = code;
            runOnUiThread(() -> Toast.makeText(this,
                    result >= 200 && result < 300
                            ? "Zapier accepted the event (HTTP " + result + ")."
                            : "Zapier request failed. Check the hook URL and internet connection.",
                    Toast.LENGTH_LONG).show());
        }, "FSG-Zapier-Webhook").start();
    }

    private void buildBusinessScope() {
        addSection("FIX-IT KINGS", "Repair · Renovate · Maintain");

        addTile("🏠", "SERVICE REQUESTS", "Incoming jobs & tickets",
                () -> openLocalWeb("http://127.0.0.1:8080/business-hq.html", "Service Requests"));
        addTile("📋", "QUOTES & INVOICES", "Generate & track",
                () -> openLocalWeb("http://127.0.0.1:8080/", "Quotes"));
        addTile("👥", "CLIENT CRM", "Open Business HQ workspace",
                () -> openLocalWeb("http://127.0.0.1:8080/business-hq.html", "Client CRM"));
        addTile("📊", "DAILY DASHBOARD", "Today’s numbers",
                () -> openLocalWeb("http://127.0.0.1:8080/", "Dashboard"));
        addTile("🛠", "FIELD TOOLS", "Open APEX field workspace",
                () -> openLocalWeb("http://127.0.0.1:8080/", "Field Tools"));
    }

    private void buildSettingsScope() {
        addSection("LAUNCHER SETTINGS", "God Mode configuration");

        addTile("🎨", "THEME  ·  " + theme.getThemeName(), "Cycle official FSG themes",
                () -> {
                    int next = (theme.getThemeId() + 1) % 4;
                    theme.apply(next);
                    Toast.makeText(this, "Theme → " + theme.getThemeName(), Toast.LENGTH_SHORT).show();
                    recreate();
                });
        addTile("📐", "CARD / ICON SCALE", String.format(Locale.US, "Current %.1f×", theme.getIconScale()),
                () -> {
                    float s = theme.getIconScale() + 0.15f;
                    if (s > 1.45f) s = 0.85f;
                    theme.setIconScale(s);
                    Toast.makeText(this, String.format(Locale.US, "Scale → %.1f", s), Toast.LENGTH_SHORT).show();
                });
        addTile("✋", "GESTURE SENSITIVITY", theme.getGestureSensitivity() + " dp",
                () -> {
                    int g = theme.getGestureSensitivity() + 20;
                    if (g > 120) g = 40;
                    theme.setGestureSensitivity(g);
                    Toast.makeText(this, "Sensitivity → " + g + " dp", Toast.LENGTH_SHORT).show();
                    showScope(currentScope);
                });
        addTile("🖼", "LIVE WALLPAPER", "System wallpaper picker",
                () -> {
                    try {
                        startActivity(Intent.createChooser(new Intent(Intent.ACTION_SET_WALLPAPER), "Choose Wallpaper"));
                    } catch (Exception e) {
                        Toast.makeText(this, "Unavailable", Toast.LENGTH_SHORT).show();
                    }
                });
        addTile("🏠", "SET AS DEFAULT HOME", "System home-app selector",
                () -> {
                    try { startActivity(new Intent(Settings.ACTION_HOME_SETTINGS)); }
                    catch (Exception e) {
                        Toast.makeText(this, "Settings → Apps → Default apps → Home app", Toast.LENGTH_LONG).show();
                    }
                });
        addTile("🔗", "ZAPIER & ADVANCED", "Open full settings screen",
                () -> startActivity(new Intent(this, SettingsActivity.class)));
        addTile("✦", "AI ASSISTANT  ·  " + (theme.isAiEnabled() ? "ON" : "OFF"), "Toggle Maestro card",
                () -> {
                    theme.setAiEnabled(!theme.isAiEnabled());
                    showScope(currentScope);
                });
        addTile("ℹ", "ABOUT GOD MODE", "v2.1 · Ubuntu Touch powered",
                () -> Toast.makeText(this,
                        "FSG God-Mode Launcher v2.1\n" +
                        "Ubuntu Touch / Lomiri features +\n" +
                        "ProMaestro Solutions Group identity\n" +
                        "Precision. Innovation. Excellence.",
                        Toast.LENGTH_LONG).show());
    }

    private void addSection(String title, String subtitle) {
        TextView t = txt(title, 12, theme.gold, true);
        t.setPadding(dp(2), dp(10), dp(2), dp(2));
        scopeContent.addView(t);
        if (subtitle != null) {
            TextView s = txt(subtitle, 11, theme.muted, false);
            s.setPadding(dp(2), 0, dp(2), dp(8));
            scopeContent.addView(s);
        }
    }

    private void addTile(String symbol, String title, String desc, Runnable action) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(12), dp(11), dp(12), dp(11));
        card.setBackground(cardBg(theme.panel, 14));

        TextView icon = txt(symbol, 22, theme.gold, true);
        icon.setGravity(Gravity.CENTER);
        card.addView(icon, new LinearLayout.LayoutParams(dp(42), dp(42)));

        LinearLayout words = new LinearLayout(this);
        words.setOrientation(LinearLayout.VERTICAL);
        words.setPadding(dp(10), 0, 0, 0);
        words.addView(txt(title, 13, theme.white, true));
        words.addView(txt(desc, 11, theme.muted, false));
        card.addView(words, new LinearLayout.LayoutParams(0, -2, 1));
        card.addView(txt("›", 22, theme.gold, true));

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(72));
        p.bottomMargin = dp(8);
        scopeContent.addView(card, p);

        card.setOnClickListener(v -> action.run());
        card.setOnLongClickListener(v -> {
            Toast.makeText(this, title, Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void loadInstalledApps() {
        Intent main = new Intent(Intent.ACTION_MAIN, null);
        main.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager pm = getPackageManager();
        installedApps = pm.queryIntentActivities(main, 0);
        installedApps.sort((a, b) -> {
            String la = a.loadLabel(pm).toString().toLowerCase();
            String lb = b.loadLabel(pm).toString().toLowerCase();
            return la.compareTo(lb);
        });
    }

    private void openLocalWeb(String url, String title) {
        Intent i = new Intent(this, WebPanelActivity.class);
        i.putExtra("url", url);
        i.putExtra("title", title);
        startActivity(i);
    }

    private void launchPackage(String... packages) {
        PackageManager pm = getPackageManager();
        for (String pkg : packages) {
            Intent launch = pm.getLaunchIntentForPackage(pkg);
            if (launch != null) {
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(launch);
                return;
            }
        }
        Toast.makeText(this, "App not installed", Toast.LENGTH_SHORT).show();
    }

    private void startClock() {
        updateIndicators();
        clockHandler.postDelayed(clockTick, 30_000);
    }

    private int dp(float n) {
        return (int) (n * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView txt(String s, int sizeSp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sizeSp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setGravity(Gravity.CENTER_VERTICAL);
        return t;
    }

    private GradientDrawable cardBg(int color, int radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radiusDp));
        d.setStroke(dp(1), Color.argb(45, 212, 175, 55));
        return d;
    }

    private GradientDrawable pill(int color) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(22));
        return d;
    }
}
