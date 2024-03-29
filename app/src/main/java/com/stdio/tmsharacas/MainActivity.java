package com.stdio.tmsharacas;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.widget.ProgressBar;

import static com.stdio.tmsharacas.WebViewHelper.afterChosePic;
import static com.stdio.tmsharacas.WebViewHelper.cameraUri;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static final String TAG = "MainActivity";
    public static final int REQUEST_CAMERA = 1;
    public static final int REQUEST_CHOOSE = 2;
    public static WebView mWebView;
    private String url = "https://tvsharing.ru/user/profile";
    public static ProgressBar progressBar;
    public static String cookie;
    public static SharedPreferences.Editor e;
    public static boolean isLoggedIn;
    ProgressDialog dialog;

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressBar);
        dialog = new ProgressDialog(this);
        initView();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE))
            { WebView.setWebContentsDebuggingEnabled(true); }
        }

        mWebView = (WebView) findViewById(R.id.maim_web);
        mWebView.setWebViewClient(new MyWebViewClient() {
            @Override
            public void onPageFinished(WebView view, String urlFromWebview) {
                Log.i(TAG, "onPageFinished");
                progressBar.setVisibility(View.GONE);
                if (dialog.isShowing()) {
                    if (urlFromWebview.equals("https://tvsharing.ru/user/profile")) {
                        mWebView.loadUrl("https://tvsharing.ru/balance");
                    }
                    else if (urlFromWebview.equals("https://tvsharing.ru/balance")) {
                        mWebView.loadUrl("https://tvsharing.ru/packets/all");
                    }
                    else if (urlFromWebview.equals("https://tvsharing.ru/packets/all")) {
                        mWebView.loadUrl("https://tvsharing.ru/packets/list");
                    }
                    else if (urlFromWebview.equals("https://tvsharing.ru/packets/list")) {
                        mWebView.loadUrl("https://tvsharing.ru/dealers/list");
                    }
                    else if (urlFromWebview.equals("https://tvsharing.ru/dealers/list")) {
                        mWebView.loadUrl("https://tvsharing.ru/server/load");
                    }
                    else if (urlFromWebview.equals("https://tvsharing.ru/server/load")) {
                        mWebView.loadUrl("https://tvsharing.ru/messages");
                    }
                    else if (urlFromWebview.equals("https://tvsharing.ru/messages")) {
                        mWebView.loadUrl("https://tvsharing.ru/ref");
                    }
                    else if (urlFromWebview.equals("https://tvsharing.ru/ref")) {
                        String cookies = CookieManager.getInstance().getCookie(urlFromWebview);
                        e.putString("cookie", cookies);
                        e.apply();
                        cookie = cookies;
                        CookieSyncManager.getInstance().sync();
                        dialog.dismiss();
                        mWebView.loadUrl("https://tvsharing.ru/news/all");
                        mWebView.setVisibility(View.VISIBLE);
                    }
                }
                else {
                    mWebView.setVisibility(View.VISIBLE);
                }
                if (!urlFromWebview.equals("https://tvsharing.ru/user/login") && !urlFromWebview.equals("https://tvsharing.ru/news/all") && !urlFromWebview.equals("https://tvsharing.ru/user/forgot") && !urlFromWebview.equals("https://tvsharing.ru/user/register")) {
                    if (!isLoggedIn) {
                        saveCookie(urlFromWebview);
                    }
                }
                super.onPageFinished(view, urlFromWebview);
            }
        });
        mWebView.setWebChromeClient(new MyWebChromeClient(this, MainActivity.this));

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);

        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);

        mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        mWebView.setScrollbarFadingEnabled(false);

        SharedPreferences getSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        cookie = getSharedPreferences.getString("cookie", "");
        isLoggedIn = getSharedPreferences.getBoolean("loggedIn", false);
        e = getSharedPreferences.edit();
        CookieManager manager = CookieManager.getInstance();

        manager.setAcceptCookie(true);
        manager.setCookie(url, cookie);

        CookieSyncManager.getInstance().sync();

        if (!isLoggedIn) {
            url = "https://tvsharing.ru/user/login";
        }
        System.out.println("COOOKie " + cookie);
        mWebView.loadUrl(url);
        verifyStoragePermissions(this);
    }

    private void saveCookie(final String urlFromWebview) {
        e.putBoolean("loggedIn", true);
        e.apply();
        isLoggedIn = true;
        dialog.setTitle("Cookie saving");
        dialog.setMessage("Please wait...");
        dialog.show();
        Thread sender = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl("https://tvsharing.ru/user/profile");
                    }
                });
            }
        });
        sender.start();
    }

    public static void clearCookies(Context context)
    {

        String cookies = "";
        e.putString("cookie", cookies);
        e.apply();
        cookie = cookies;
        e.putBoolean("loggedIn", false);
        e.apply();
        isLoggedIn = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Log.d("AAAA", "Using clearCookies code for API >=" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else
        {
            Log.d("AAAA", "Using clearCookies code for API <" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieSyncManager cookieSyncMngr=CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager=CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
        mWebView.loadUrl("https://tvsharing.ru/user/login");
        CookieSyncManager.getInstance().sync();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                super.onBackPressed();
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_profile:
                url = "https://tvsharing.ru/user/profile";
                break;
            case R.id.nav_balance:
                url = "https://tvsharing.ru/balance";
                break;
            case R.id.nav_buy_packets:
                url = "https://tvsharing.ru/packets/all";
                break;
            case R.id.nav_my_packets:
                url = "https://tvsharing.ru/packets/list";
                break;
            case R.id.nav_clients:
                url = "https://tvsharing.ru/dealers/list";
                break;
            case R.id.nav_view_settings:
                url = "https://tvsharing.ru/server/load";
                break;
            case R.id.nav_messages:
                url = "https://tvsharing.ru/messages";
                break;
            case R.id.nav_news:
                url = "https://tvsharing.ru/news/all";
                break;
            case R.id.nav_refs:
                url = "https://tvsharing.ru/ref";
                break;
            case R.id.nav_exit:
                confirmClearCookies();
                break;
        }

        if (!isLoggedIn && !url.equals("https://tvsharing.ru/news/all")) {
            url = "https://tvsharing.ru/user/login";
        }
        mWebView.loadUrl(url);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void confirmClearCookies() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

//        // set title
//        alertDialogBuilder.setTitle("Delete item");

        // set dialog message
        alertDialogBuilder
                .setMessage("Выйти?")
                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mWebView.clearCache(true);
                        mWebView.clearHistory();
                        clearCookies(MainActivity.this);
                    }
                })
                .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (MyWebChromeClient.mUploadMessagesAboveL != null) {
            new WebViewHelper(this, MainActivity.this).onActivityResultAboveL(requestCode, resultCode, intent);
        }

        if (MyWebChromeClient.mUploadMessage == null) return;

        Uri uri = null;

        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            uri = cameraUri;
        }

        if (requestCode == REQUEST_CHOOSE && resultCode == RESULT_OK) {
            uri = afterChosePic(intent);
        }

        MyWebChromeClient.mUploadMessage.onReceiveValue(uri);
        MyWebChromeClient.mUploadMessage = null;
        super.onActivityResult(requestCode, resultCode, intent);
    }
}
