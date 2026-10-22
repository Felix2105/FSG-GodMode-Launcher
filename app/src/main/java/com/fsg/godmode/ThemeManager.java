package com.fsg.godmode;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

/**
 * Runtime theme engine for FSG God-Mode Launcher.
 * All colors derived from official ProMaestro Solutions Group logo.
 */
public class ThemeManager {

    public static final String PREFS = "fsg_godmode_prefs";
    public static final String KEY_THEME = "theme_id";
    public static final String KEY_ICON_SIZE = "icon_size";
    public static final String KEY_GESTURE_SENS = "gesture_sens";
    public static final String KEY_ZAPIER_URL = "zapier_url";
    public static final String KEY_AI_ENABLED = "ai_enabled";

    public static final int THEME_NAVY_GOLD = 0;
    public static final int THEME_MIDNIGHT = 1;
    public static final int THEME_PLATINUM = 2;
    public static final int THEME_HIGH_CONTRAST = 3;

    private final SharedPreferences prefs;

    // Current palette
    public int bg;
    public int panel;
    public int panelElevated;
    public int gold;
    public int goldSoft;
    public int white;
    public int muted;
    public int success;
    public int danger;

    public ThemeManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        apply(prefs.getInt(KEY_THEME, THEME_NAVY_GOLD));
    }

    public void apply(int themeId) {
        prefs.edit().putInt(KEY_THEME, themeId).apply();
        switch (themeId) {
            case THEME_MIDNIGHT:
                bg = Color.rgb(5, 8, 14);
                panel = Color.rgb(18, 26, 38);
                panelElevated = Color.rgb(28, 38, 52);
                gold = Color.rgb(180, 150, 60);
                goldSoft = Color.rgb(200, 170, 80);
                white = Color.rgb(220, 225, 230);
                muted = Color.rgb(140, 155, 170);
                break;
            case THEME_PLATINUM:
                bg = Color.rgb(20, 22, 28);
                panel = Color.rgb(40, 44, 55);
                panelElevated = Color.rgb(55, 60, 75);
                gold = Color.rgb(200, 200, 210);
                goldSoft = Color.rgb(230, 230, 240);
                white = Color.rgb(250, 250, 255);
                muted = Color.rgb(160, 165, 180);
                break;
            case THEME_HIGH_CONTRAST:
                bg = Color.BLACK;
                panel = Color.rgb(20, 20, 20);
                panelElevated = Color.rgb(40, 40, 40);
                gold = Color.rgb(255, 215, 0);
                goldSoft = Color.rgb(255, 235, 100);
                white = Color.WHITE;
                muted = Color.rgb(180, 180, 180);
                break;
            case THEME_NAVY_GOLD:
            default:
                // Exact logo colors
                bg = Color.rgb(13, 20, 31);          // #0D141F
                panel = Color.rgb(27, 39, 55);       // #1B2737
                panelElevated = Color.rgb(35, 49, 67); // #233143
                gold = Color.rgb(212, 175, 55);      // #D4AF37
                goldSoft = Color.rgb(220, 183, 86);  // #DCB756
                white = Color.rgb(245, 247, 250);    // #F5F7FA
                muted = Color.rgb(174, 187, 202);    // #AEBBCA
                break;
        }
        success = Color.rgb(46, 204, 113);
        danger = Color.rgb(231, 76, 60);
    }

    public int getThemeId() {
        return prefs.getInt(KEY_THEME, THEME_NAVY_GOLD);
    }

    public String getThemeName() {
        switch (getThemeId()) {
            case THEME_MIDNIGHT: return "Midnight";
            case THEME_PLATINUM: return "Platinum";
            case THEME_HIGH_CONTRAST: return "High Contrast";
            default: return "Navy Gold (Official)";
        }
    }

    public float getIconScale() {
        return prefs.getFloat(KEY_ICON_SIZE, 1.0f);
    }

    public void setIconScale(float scale) {
        prefs.edit().putFloat(KEY_ICON_SIZE, scale).apply();
    }

    public int getGestureSensitivity() {
        return prefs.getInt(KEY_GESTURE_SENS, 70); // dp threshold
    }

    public void setGestureSensitivity(int dp) {
        prefs.edit().putInt(KEY_GESTURE_SENS, dp).apply();
    }

    public String getZapierUrl() {
        return prefs.getString(KEY_ZAPIER_URL, "https://hooks.zapier.com/hooks/catch/");
    }

    public void setZapierUrl(String url) {
        prefs.edit().putString(KEY_ZAPIER_URL, url).apply();
    }

    public boolean isAiEnabled() {
        return prefs.getBoolean(KEY_AI_ENABLED, true);
    }

    public void setAiEnabled(boolean on) {
        prefs.edit().putBoolean(KEY_AI_ENABLED, on).apply();
    }
}
