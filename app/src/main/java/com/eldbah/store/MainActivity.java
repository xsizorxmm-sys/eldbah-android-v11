package com.eldbah.store;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {
    private WebView web;
    private ProgressBar progress;
    private View splash;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(18, 81, 181));
        getWindow().setNavigationBarColor(Color.rgb(16, 24, 32));

        FrameLayout root = new FrameLayout(this);
        web = new WebView(this);
        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setIndeterminate(false);
        root.addView(web, new FrameLayout.LayoutParams(-1, -1));
        root.addView(progress, new FrameLayout.LayoutParams(-1, dp(3), Gravity.TOP));
        splash = makeSplash();
        root.addView(splash, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        web.setBackgroundColor(Color.WHITE);

        web.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView v, int p) {
                progress.setProgress(p);
                progress.setVisibility(p >= 100 ? View.GONE : View.VISIBLE);
                if (p >= 100) hideSplash();
            }
        });
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                return route(r.getUrl());
            }
            @SuppressWarnings("deprecation")
            @Override public boolean shouldOverrideUrlLoading(WebView v, String url) {
                return route(Uri.parse(url));
            }
            @Override public void onPageFinished(WebView v, String url) { hideSplash(); }
        });
        if (state != null) web.restoreState(state);
        else web.loadUrl("https://eldbah.com");
    }

    private View makeSplash() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(24), dp(30), dp(24), dp(30));
        box.setBackgroundColor(Color.rgb(244, 248, 252));

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.ic_launcher_logo);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(190), dp(190));
        lp.bottomMargin = dp(20);
        box.addView(logo, lp);

        TextView name = new TextView(this);
        name.setText("مؤسسة الدبّاح");
        name.setTextColor(Color.rgb(18, 81, 181));
        name.setTextSize(26);
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        name.setGravity(Gravity.CENTER);
        box.addView(name, new LinearLayout.LayoutParams(-1, -2));

        TextView line = new TextView(this);
        line.setText("للعدد والأدوات والمعدات");
        line.setTextColor(Color.rgb(16, 24, 32));
        line.setTextSize(15);
        line.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lineLp = new LinearLayout.LayoutParams(-1, -2);
        lineLp.topMargin = dp(8);
        box.addView(line, lineLp);

        ProgressBar spinner = new ProgressBar(this);
        LinearLayout.LayoutParams spinLp = new LinearLayout.LayoutParams(dp(34), dp(34));
        spinLp.topMargin = dp(28);
        box.addView(spinner, spinLp);
        TextView loading = new TextView(this);
        loading.setText("جاري تجهيز المتجر...");
        loading.setTextColor(Color.rgb(90, 105, 125));
        loading.setTextSize(13);
        loading.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams loadLp = new LinearLayout.LayoutParams(-1, -2);
        loadLp.topMargin = dp(10);
        box.addView(loading, loadLp);
        return box;
    }

    private void hideSplash() {
        if (splash != null && splash.getVisibility() == View.VISIBLE) {
            splash.animate().alpha(0f).setDuration(220).withEndAction(() -> {
                if (splash != null) splash.setVisibility(View.GONE);
            }).start();
        }
    }

    private boolean route(Uri uri) {
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase();
        if ((scheme.equals("https") || scheme.equals("http")) &&
            (host.equals("eldbah.com") || host.equals("www.eldbah.com") || host.endsWith(".eldbah.com"))) return false;
        try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); }
        catch (Exception ignored) { }
        return true;
    }

    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + 0.5f); }

    @Override protected void onSaveInstanceState(Bundle out) {
        if (web != null) web.saveState(out);
        super.onSaveInstanceState(out);
    }
    @Override public void onBackPressed() {
        if (web != null && web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }
    @Override protected void onDestroy() {
        if (web != null) {
            web.stopLoading();
            web.setWebViewClient(null);
            web.setWebChromeClient(null);
            web.destroy();
            web = null;
        }
        super.onDestroy();
    }
}
