package com.india.deva;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.india.deva.network.ConnectivityReceiver;
import com.tapadoo.alerter.Alerter;

import cn.pedant.SweetAlert.SweetAlertDialog;

import android.util.TimingLogger;
import android.util.Log;


public class MainActivity extends AbstractProjectBaseActivity implements SwipeRefreshLayout.OnRefreshListener{
    private String postUrl = "https://juejin.im/";
    private WebView webview;
    boolean doubleBackToExitPressedOnce = false;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TimingLogger timeLogger;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Test", String.valueOf(System.currentTimeMillis()));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("Test onCreate", String.valueOf(System.currentTimeMillis()));
        mSwipeRefreshLayout = findViewById(R.id.swipeContainer);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        checkInternetConnection();


    }

    private void checkInternetConnection() {
        if (ConnectivityReceiver.isConnected()) {
            timeLogger = new TimingLogger(postUrl, "loadWebView");

            loadWebView();
        } else {
            showAlertWithOnClick();
        }
    }

    private void showAlertWithOnClick() {
        Alerter.create(MainActivity.this)
                .setTitle("Internet Alert!")
                .setText(R.string.check_internet)
                .setDismissable(false)
                .setIcon(R.drawable.ic_warning)
                .disableOutsideTouch()
                .setDuration(50000)
                .addButton("Retry",R.style.AlertButton, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ConnectivityReceiver.isConnected()) {
                            Alerter.hide();
                            loadWebView();
                        }else {
                            Toast.makeText(MainActivity.this, "Make sure internet connection", Toast.LENGTH_LONG).show();

                        }

                    }
                })
                .show();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void loadWebView() {
        webview = (WebView) findViewById(R.id.webView);

        webview.setWebViewClient(new MyWebViewClient());
        WebView.setWebContentsDebuggingEnabled(true);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        webview.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webview.loadUrl(postUrl);
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                    checkInternetConnection();

                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        }, 1000);
    }


    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            timeLogger.addSplit("onPageStarted");
            Log.d("Test onPageStarted", String.valueOf(System.currentTimeMillis()));
            super.onPageStarted(view, url, favicon);
            ProgressDialog.getInstance().showLoading(MainActivity.this);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            timeLogger.addSplit("onPageFinished");
            timeLogger.dumpToLog();
            Log.d("Test onPageFinished", String.valueOf(System.currentTimeMillis()));
            super.onPageFinished(view, url);
            ProgressDialog.getInstance().dismiss();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webview.canGoBack()) {
                        webview.goBack();
                    } else {
                        if (doubleBackToExitPressedOnce) {
                            super.onBackPressed();
                        }
                        this.doubleBackToExitPressedOnce = true;
                        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                doubleBackToExitPressedOnce = false;
                            }
                        }, 2000);
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
