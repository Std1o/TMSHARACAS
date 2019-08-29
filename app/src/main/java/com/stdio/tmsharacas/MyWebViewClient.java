package com.stdio.tmsharacas;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import static com.stdio.tmsharacas.MainActivity.TAG;

public class MyWebViewClient extends WebViewClient {

    Activity activity;
    Context context;

    public MyWebViewClient (Context mContext, Activity mActivity) {

    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return handleUri(view, url);
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();
        return handleUri(view, url);
    }

    private boolean handleUri(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }

    public MyWebViewClient() {
        super();
    }


    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        Log.d(TAG, "URL地址:" + url);
        MainActivity.progressBar.setVisibility(View.VISIBLE);
        MainActivity.mWebView.setVisibility(View.GONE);
        super.onPageStarted(view, url, favicon);
    }
}