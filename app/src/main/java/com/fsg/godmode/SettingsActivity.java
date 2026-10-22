package com.fsg.godmode;

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Dedicated settings screen for advanced configuration
 * (Zapier URL, theme persistence already handled by ThemeManager).
 */
public class SettingsActivity extends Activity {

    private ThemeManager theme;

    private int dp(float n) {
        return (int) (n * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView txt(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private GradientDrawable card(int color) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(14));
        return d;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        theme = new ThemeManager(this);
        getWindow().setStatusBarColor(theme.bg);
        getWindow().setNavigationBarColor(theme.bg);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(theme.bg);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(28));
        scroll.addView(root);

        // Header
        TextView back = txt("‹  BACK TO GOD MODE", 14, theme.gold, true);
        back.setPadding(0, 0, 0, dp(16));
        back.setOnClickListener(v -> finish());
        root.addView(back);

        root.addView(txt("GOD MODE SETTINGS", 22, theme.white, true));
        root.addView(txt("ProMaestro Solutions Group", 12, theme.muted, false));

        // Zapier URL
        root.addView(spacer(18));
        root.addView(txt("ZAPIER WEBHOOK URL", 12, theme.gold, true));
        EditText zap = new EditText(this);
        zap.setText(theme.getZapierUrl());
        zap.setTextColor(theme.white);
        zap.setHintTextColor(theme.muted);
        zap.setBackground(card(theme.panel));
        zap.setPadding(dp(14), dp(12), dp(14), dp(12));
        zap.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        root.addView(zap, new LinearLayout.LayoutParams(-1, -2));

        TextView saveZap = txt("SAVE ZAPIER URL", 14, theme.bg, true);
        saveZap.setGravity(Gravity.CENTER);
        saveZap.setBackground(card(theme.gold));
        saveZap.setPadding(dp(12), dp(14), dp(12), dp(14));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2);
        sp.topMargin = dp(10);
        root.addView(saveZap, sp);
        saveZap.setOnClickListener(v -> {
            theme.setZapierUrl(zap.getText().toString().trim());
            Toast.makeText(this, "Zapier URL saved", Toast.LENGTH_SHORT).show();
        });

        // Theme quick switch
        root.addView(spacer(24));
        root.addView(txt("ACTIVE THEME", 12, theme.gold, true));
        root.addView(txt(theme.getThemeName(), 16, theme.white, true));

        String[] names = {"Navy Gold (Official)", "Midnight", "Platinum", "High Contrast"};
        for (int i = 0; i < 4; i++) {
            final int id = i;
            TextView btn = txt(names[i], 14, theme.white, false);
            btn.setBackground(card(theme.panel));
            btn.setPadding(dp(14), dp(12), dp(14), dp(12));
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, -2);
            bp.topMargin = dp(6);
            root.addView(btn, bp);
            btn.setOnClickListener(v -> {
                theme.apply(id);
                Toast.makeText(this, "Theme → " + names[id], Toast.LENGTH_SHORT).show();
                recreate(); // refresh this screen with new colors
            });
        }

        // Info
        root.addView(spacer(28));
        root.addView(txt("ABOUT", 12, theme.gold, true));
        TextView about = txt(
                "FSG God-Mode Launcher v2.0\n" +
                "Package: com.fsg.godmode\n" +
                "Min SDK 23 · Target SDK 34\n" +
                "Built for Android 11+\n\n" +
                "Colors extracted from the official\n" +
                "ProMaestro Solutions Group / Fix-It Kings logo.\n\n" +
                "Integrations: Tasker · WhatsApp · Termux · Zapier · APEX\n" +
                "Live wallpaper · Multi-page gestures · Runtime themes",
                12, theme.muted, false);
        about.setPadding(0, dp(6), 0, 0);
        root.addView(about);

        setContentView(scroll);
    }

    private TextView spacer(int h) {
        TextView s = new TextView(this);
        s.setHeight(dp(h));
        return s;
    }
}
