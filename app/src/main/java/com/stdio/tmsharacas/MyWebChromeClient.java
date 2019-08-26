package com.stdio.tmsharacas;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import static com.stdio.tmsharacas.MainActivity.TAG;

public class MyWebChromeClient extends WebChromeClient {

    Context context;
    Activity activity;

    public MyWebChromeClient (Context mContext, Activity mActivity) {
        context = mContext;
        activity = mActivity;
    }

    public static ValueCallback<Uri> mUploadMessage;
    public static ValueCallback<Uri[]> mUploadMessagesAboveL;

    // For Android 3.0+
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        Log.d(TAG,"openFileChooser");
        if (mUploadMessage != null) return;
        mUploadMessage = uploadMsg;
        new WebViewHelper(context, activity).selectImage();
    }

    // For Android < 3.0
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        openFileChooser(uploadMsg, "");
    }

    // For Android  > 4.1.1
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        openFileChooser(uploadMsg, acceptType);
    }

    // For Android 5.0
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {

        if (mUploadMessagesAboveL != null) {
            mUploadMessagesAboveL.onReceiveValue(null);
        } else {
            mUploadMessagesAboveL = filePathCallback;
            new WebViewHelper(context, activity).selectImage();
        }
        return true;
    }
}