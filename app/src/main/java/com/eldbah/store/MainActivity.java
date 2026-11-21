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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends Activity {
    private static final String HOME_URL = "https://eldbah.com";
    private WebView web;
    private SwipeRefreshLayout refreshLayout;
    private ProgressBar progress;
    private View splash;
    private View offlineView;
    private boolean pageFailed = false;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(18, 81, 181));
        getWindow().setNavigationBarColor(Color.rgb(16, 24, 32));

        FrameLayout root = new FrameLayout(this);
        web = new WebView(this);
        refreshLayout = new SwipeRefreshLayout(this);
        refreshLayout.setColorSchemeColors(Color.rgb(18, 81, 181));
        refreshLayout.setOnRefreshListener(() -> web.reload());
        refreshLayout.addView(web, new SwipeRefreshLayout.LayoutParams(-1, -1));
        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setIndeterminate(false);
        root.addView(refreshLayout, new FrameLayout.LayoutParams(-1, -1));
        root.addView(progress, new FrameLayout.LayoutParams(-1, dp(3), Gravity.TOP));
        splash = makeSplash();
        root.addView(splash, new FrameLayout.LayoutParams(-1, -1));
        offlineView = makeOfflineView();
        offlineView.setVisibility(View.GONE);
        root.addView(offlineView, new FrameLayout.LayoutParams(-1, -1));
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
        web.setOnLongClickListener(v -> {
            WebView.HitTestResult hit = web.getHitTestResult();
            String shareUrl = hit != null && hit.getExtra() != null ? hit.getExtra() : web.getUrl();
            if (shareUrl == null || shareUrl.isEmpty()) return false;
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            String title = web.getTitle();
            String message = (title != null && !title.trim().isEmpty() ? title + "\n" : "") + "شوف المنتج ده من مؤسسة الدبّاح: " + shareUrl;
            share.putExtra(Intent.EXTRA_TEXT, message);
            try { startActivity(Intent.createChooser(share, "مشاركة رابط المنتج")); }
            catch (Exception ignored) { Toast.makeText(MainActivity.this, "تعذر فتح المشاركة", Toast.LENGTH_SHORT).show(); }
            return true;
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView v, int p) {
                progress.setProgress(p);
                progress.setVisibility(p >= 100 ? View.GONE : View.VISIBLE);
                if (p >= 100) refreshLayout.setRefreshing(false);
                if (p >= 100 && !pageFailed) hideSplash();
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
            @Override public void onPageStarted(WebView v, String url, android.graphics.Bitmap favicon) {
                pageFailed = false;
                offlineView.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
                if (splash != null && splash.getVisibility() == View.GONE && v.getUrl() == null) {
                    splash.setAlpha(1f);
                    splash.setVisibility(View.VISIBLE);
                }
            }
            @Override public void onPageFinished(WebView v, String url) {
                refreshLayout.setRefreshing(false);
                if (!pageFailed) {
                    hideSplash();
                    offlineView.setVisibility(View.GONE);
                }
            }
            @Override public void onReceivedError(WebView v, WebResourceRequest request, android.webkit.WebResourceError error) {
                if (request.isForMainFrame()) showOffline();
            }
            @Override public void onReceivedHttpError(WebView v, WebResourceRequest request, android.webkit.WebResourceResponse response) {
                if (request.isForMainFrame() && response.getStatusCode() >= 400) showOffline();
            }
        });
        if (state != null) {
            if (web.restoreState(state) == null) web.loadUrl(HOME_URL);
        } else web.loadUrl(HOME_URL);
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

    private View makeOfflineView() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(dp(28), dp(30), dp(28), dp(30));
        panel.setBackgroundColor(Color.rgb(248, 250, 253));
        TextView title = new TextView(this);
        title.setText("تعذر الاتصال بالمتجر");
        title.setTextColor(Color.rgb(18, 81, 181));
        title.setTextSize(22);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        panel.addView(title, new LinearLayout.LayoutParams(-1, -2));
        TextView message = new TextView(this);
        message.setText("تأكد من اتصال الإنترنت وحاول مرة أخرى.");
        message.setTextColor(Color.rgb(50, 60, 75));
        message.setTextSize(15);
        message.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams messageLp = new LinearLayout.LayoutParams(-1, -2);
        messageLp.topMargin = dp(12);
        panel.addView(message, messageLp);
        Button retry = new Button(this);
        retry.setText("إعادة المحاولة");
        retry.setOnClickListener(v -> loadHome());
        LinearLayout.LayoutParams retryLp = new LinearLayout.LayoutParams(-2, -2);
        retryLp.topMargin = dp(22);
        panel.addView(retry, retryLp);
        return panel;
    }

    private void loadHome() {
        pageFailed = false;
        offlineView.setVisibility(View.GONE);
        if (splash != null) {
            splash.setAlpha(1f);
            splash.setVisibility(View.VISIBLE);
        }
        progress.setVisibility(View.VISIBLE);
        web.loadUrl(HOME_URL);
    }

    private void showOffline() {
        pageFailed = true;
        refreshLayout.setRefreshing(false);
        progress.setVisibility(View.GONE);
        hideSplash();
        offlineView.setVisibility(View.VISIBLE);
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
        boolean internalHost = host.equals("eldbah.com") || host.equals("www.eldbah.com") || host.endsWith(".eldbah.com");
        if (internalHost && scheme.equals("http")) {
            // Normalize internal links to HTTPS because cleartext traffic is disabled.
            Uri secureUri = uri.buildUpon().scheme("https").clearPort().build();
            web.loadUrl(secureUri.toString());
            return true;
        }
        if (internalHost && scheme.equals("https")) return false;
        try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); }
        catch (Exception ignored) { Toast.makeText(this, "لا يوجد تطبيق مناسب لفتح الرابط", Toast.LENGTH_SHORT).show(); }
        return true;
    }

    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + 0.5f); }

    @Override protected void onSaveInstanceState(Bundle out) {
        if (web != null) web.saveState(out);
        super.onSaveInstanceState(out);
    }
    @Override public void onBackPressed() {
        if (offlineView != null && offlineView.getVisibility() == View.VISIBLE) {
            if (web != null && web.canGoBack()) {
                offlineView.setVisibility(View.GONE);
                web.goBack();
            } else super.onBackPressed();
        } else if (web != null && web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }
    @Override protected void onDestroy() {
        if (refreshLayout != null) refreshLayout.setOnRefreshListener(null);
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