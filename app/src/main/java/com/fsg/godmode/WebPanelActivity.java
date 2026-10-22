package com.fsg.godmode;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Secure local-only WebView for APEX dashboard panels.
 * Only 127.0.0.1 addresses are permitted.
 */
public class WebPanelActivity extends Activity {

    private WebView web;
    private ThemeManager theme;

    private int dp(float n) {
        return (int) (n * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        theme = new ThemeManager(this);
        getWindow().setStatusBarColor(theme.bg);
        getWindow().setNavigationBarColor(theme.bg);

        String url = getIntent().getStringExtra("url");
        String title = getIntent().getStringExtra("title");
        if (title == null) title = "Panel";

        if (url == null || !isAllowed(url)) {
            Toast.makeText(this, "Only local APEX addresses (127.0.0.1) are allowed", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(theme.bg);

        // Top bar
        TextView bar = new TextView(this);
        bar.setText("‹   FSG GOD MODE     /     " + title.toUpperCase());
        bar.setTextColor(theme.gold);
        bar.setTextSize(14);
        bar.setTypeface(null, android.graphics.Typeface.BOLD);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(16), 0, dp(8), 0);
        bar.setBackgroundColor(theme.panel);
        root.addView(bar, new LinearLayout.LayoutParams(-1, dp(52)));
        bar.setOnClickListener(v -> finish());

        web = new WebView(this);
        web.setBackgroundColor(Color.WHITE);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        web.setWebViewClient(new WebViewClient());
        root.addView(web, new LinearLayout.LayoutParams(-1, 0, 1f));

        setContentView(root);
        web.loadUrl(url);
    }

    private boolean isAllowed(String url) {
        return url.startsWith("http://127.0.0.1:8080/")
                || url.equals("http://127.0.0.1:8080/")
                || url.startsWith("http://127.0.0.1:8765/");
    }

    @Override
    public void onBackPressed() {
        if (web != null && web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }
}
