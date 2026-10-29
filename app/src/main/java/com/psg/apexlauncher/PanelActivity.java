package com.psg.apexlauncher;
import android.app.*;import android.os.*;import android.webkit.*;import android.view.*;import android.widget.*;import android.graphics.Color;
public class PanelActivity extends Activity {
 @Override public void onCreate(Bundle b){super.onCreate(b);String title=getIntent().getStringExtra("title");String url=getIntent().getStringExtra("url");getWindow().setStatusBarColor(0xff071426);LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setBackgroundColor(0xff071426);TextView h=new TextView(this);h.setText("PSG  /  "+(title==null?"APEX":title));h.setTextColor(0xffd8b45a);h.setTextSize(18);h.setPadding(18,12,18,12);l.addView(h);WebView w=new WebView(this);w.getSettings().setJavaScriptEnabled(true);w.getSettings().setDomStorageEnabled(true);w.setWebViewClient(new WebViewClient());l.addView(w,new LinearLayout.LayoutParams(-1,0,1));setContentView(l);if(url!=null)w.loadUrl(url);}
 @Override public void onBackPressed(){super.onBackPressed();}
}
