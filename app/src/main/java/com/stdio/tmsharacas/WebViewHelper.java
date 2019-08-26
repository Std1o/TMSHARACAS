package com.stdio.tmsharacas;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import java.io.File;

import static android.app.Activity.RESULT_OK;
import static com.stdio.tmsharacas.MainActivity.REQUEST_CAMERA;
import static com.stdio.tmsharacas.MainActivity.REQUEST_CHOOSE;

public class WebViewHelper {

    Context context;
    Activity activity;
    public static Uri cameraUri;

    public WebViewHelper (Context mContext, Activity mActivity) {
        context = mContext;
        activity = mActivity;
    }

    public void selectImage() {
        if (!FileUtils.checkSDcard(context)) {
            return;
        }
        String[] selectPicTypeStr = {"Фото", "Галерея"};
        new AlertDialog.Builder(context)
                .setOnCancelListener(new ReOnCancelListener())
                .setItems(selectPicTypeStr,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    // 相机拍摄
                                    case 0:
                                        openCarcme();
                                        break;
                                    // 手机相册
                                    case 1:
                                        chosePicture();
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }).show();
    }

    private void chosePicture() {
        Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        innerIntent.setType("image/*");
        Intent wrapperIntent = Intent.createChooser(innerIntent, null);
        activity.startActivityForResult(wrapperIntent, REQUEST_CHOOSE);
    }

    /**
     * 选择照片后结束
     *
     * @param data
     */
    public static Uri afterChosePic(Intent data) {
        if (data != null) {
            final String path = data.getData().getPath();
            if (path != null && (path.endsWith(".png") || path.endsWith(".PNG") || path.endsWith(".jpg") || path.endsWith(".JPG"))) {
                return data.getData();
            }
        }
        return null;
    }

    /**
     * 打开照相机
     */
    private void openCarcme() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String imagePaths = Environment.getExternalStorageDirectory().getPath() + "/BigMoney/Images/" + (System.currentTimeMillis() + ".jpg");
        // 必须确保文件夹路径存在，否则拍照后无法完成回调
        File vFile = new File(imagePaths);
        if (!vFile.exists()) {
            File vDirPath = vFile.getParentFile();
            vDirPath.mkdirs();
        } else {
            if (vFile.exists()) {
                vFile.delete();
            }
        }
        cameraUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider",vFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
        activity.startActivityForResult(intent, REQUEST_CAMERA);
    }


    /**
     * 返回文件选择
     */



    /**
     * 5.0以后机型 返回文件选择
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResultAboveL(int requestCode, int resultCode, Intent data) {

        Uri[] results = null;

        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            results = new Uri[]{cameraUri};
        }

        if (requestCode == REQUEST_CHOOSE && resultCode == RESULT_OK) {
            if (data != null) {
                String dataString = data.getDataString();
                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }

        MyWebChromeClient.mUploadMessagesAboveL.onReceiveValue(results);
        MyWebChromeClient.mUploadMessagesAboveL = null;
        return;
    }


    /**
     * dialog监听类
     */
    private class ReOnCancelListener implements DialogInterface.OnCancelListener {
        @Override
        public void onCancel(DialogInterface dialogInterface) {
            if (MyWebChromeClient.mUploadMessage != null) {
                MyWebChromeClient.mUploadMessage.onReceiveValue(null);
                MyWebChromeClient.mUploadMessage = null;
            }

            if (MyWebChromeClient.mUploadMessagesAboveL != null) {
                MyWebChromeClient.mUploadMessagesAboveL.onReceiveValue(null);
                MyWebChromeClient.mUploadMessagesAboveL = null;
            }
        }
    }

}
