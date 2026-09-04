package com.ilmeeo.app;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.GeolocationPermissions;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private WebView webview;
    private BottomNavigationView bottomNav;
    private static final int LOCATION_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.alifeo_green));

        webview = findViewById(R.id.webView);
        bottomNav = findViewById(R.id.bottomNav);

        // Notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 201);
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE);
        } else {
            initializeWebView();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                setupWebView("https://ilmeeo.alifeo.com/");
            } else if (id == R.id.nav_search) {
                setupWebView("https://ilmeeo.alifeo.com/timeline.html");
            } else if (id == R.id.nav_cart) {
                setupWebView("https://ilmeeo.alifeo.com/basic.html");
            } else if (id == R.id.nav_orders) {
                setupWebView("https://ilmeeo.alifeo.com/explore.html");
            }
            return true;
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeWebView();
            } else {
                Toast.makeText(this, "Location permission required for some features.", Toast.LENGTH_SHORT).show();
                initializeWebView();
            }
        }
    }

    private void initializeWebView() {
        if (!CheckNetwork.isInternetAvailable(this)) {
            new AlertDialog.Builder(this)
                    .setTitle("No internet connection")
                    .setMessage("Please check your mobile data or Wi-Fi connection.")
                    .setPositiveButton("OK", (dialog, which) -> finish())
                    .show();
        } else {
            setupWebView("https://ilmeeo.alifeo.com/");
        }
    }

    private void setupWebView(String url) {
        webview.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String urlStr = request.getUrl().toString();


                if (urlStr.contains("docs.google.com/forms")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlStr));
                    startActivity(intent);
                    return true;
                }

                // Amazon links
                if (urlStr.contains("amazon.") || urlStr.contains("a.co")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlStr));
                    startActivity(intent);
                    return true;
                }

                // YouTube links
                if (urlStr.contains("youtube.com") || urlStr.contains("youtu.be")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlStr));
                    startActivity(intent);
                    return true;
                }

                return false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String urlStr) {
                // Amazon links
                if (urlStr.contains("amazon.") || urlStr.contains("a.co")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlStr));
                    startActivity(intent);
                    return true;
                }

                // YouTube links
                if (urlStr.contains("youtube.com") || urlStr.contains("youtu.be")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlStr));
                    startActivity(intent);
                    return true;
                }

                return false;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                view.loadData(
                        "<html><body style='text-align:center; padding-top:50%; font-family:sans-serif;'>" +
                                "<h2 style='color:red;'>😥 Oops! No Internet</h2>" +
                                "<p>Please check your connection and try again.</p>" +
                                "</body></html>", "text/html", "UTF-8"
                );
            }
        });

        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(false);
        webSettings.setGeolocationEnabled(true);

        String dir = this.getDir("geolocation", Context.MODE_PRIVATE).getPath();
        webSettings.setGeolocationDatabasePath(dir);

        webSettings.setUserAgentString(
                "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.106 Mobile Safari/537.36"
        );

        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin,
                                                           GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        webview.setDownloadListener((url1, userAgent, contentDisposition, mimetype, contentLength) -> {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url1));
            request.setMimeType(mimetype);
            request.addRequestHeader("User-Agent", userAgent);
            request.setDescription("Downloading file...");
            request.setTitle(URLUtil.guessFileName(url1, contentDisposition, mimetype));
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                    URLUtil.guessFileName(url1, contentDisposition, mimetype));

            DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            dm.enqueue(request);
            Toast.makeText(getApplicationContext(), "Download started", Toast.LENGTH_SHORT).show();
        });

        webview.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        webview.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        if (webview.canGoBack()) {
            webview.goBack();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Exit App")
                    .setMessage("Do you want to exit?")
                    .setPositiveButton("Yes", (dialog, which) -> finish())
                    .setNegativeButton("No", null)
                    .show();
        }
    }
}

// ✅ Internet checker class
class CheckNetwork {
    public static boolean isInternetAvailable(Context context) {
        NetworkInfo info = ((ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return info != null && info.isConnected();
    }
}
