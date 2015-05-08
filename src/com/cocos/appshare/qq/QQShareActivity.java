package com.cocos.appshare.qq;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import com.cocos.appshare.OnShareResultListener;
import com.cocos.appshare.R;
import com.cocos.appshare.util.Log;
import com.cocos.appshare.util.ShareResultUtil;
import com.tencent.connect.auth.QQAuth;
import com.tencent.connect.share.QQShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;

public class QQShareActivity extends Activity implements IUiListener {
    private static final String IMG_URL = "http://app.download.anzhuoshangdian.com/weishangdian/icon/1415616530.png";
    private QQShare mQQShare = null;
    private QQAuth mQQAuth;

    private boolean mPicture;
    private String mImgUrl;
    private String mTitle;
    private String mContent;
    private String mUrl;
    private Uri mBitmapUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mPicture = intent.getBooleanExtra("picture", false);
        mImgUrl = intent.getStringExtra("imgurl");
        mTitle = intent.getStringExtra("title");
        mContent = intent.getStringExtra("content");
        mUrl = intent.getStringExtra("url");
        mBitmapUri = intent.getParcelableExtra("sharebitmap");
        mQQAuth = QQAuthManager.get(this).getQQAuth();
        mQQShare = new QQShare(this, mQQAuth.getQQToken());
        shareToQQ();
        finish();
    }

    @Override
    public void onCancel() {
        Log.d(Log.TAG, "");
    }

    @Override
    public void onComplete(Object obj) {
        Log.d(Log.TAG, "obj = " + obj);
        OnShareResultListener l = ShareResultUtil.getOnShareResultListener();
        boolean result = false;
        String des = getResources().getString(R.string.errcode_failure);
        int ret = -1;
        if (obj != null) {
            try {
                JSONObject jobj = new JSONObject(obj.toString());
                if (jobj.has("ret")) {
                    ret = jobj.getInt("ret");
                    if (ret == 0) {
                        result = true;
                        des = getResources()
                                .getString(R.string.errcode_success);
                    }
                }
            } catch (JSONException e) {
                Log.d(Log.TAG, "error : " + e);
            }
        }
        if (l != null) {
            l.onShareResult(result, des);
        }
        // Toast.makeText(this, des, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError(UiError error) {
        Log.d(Log.TAG, "error = " + error.errorMessage);
    }

    public void shareToQQ() {
        Bundle params = new Bundle();
        int type = mPicture ? QQShare.SHARE_TO_QQ_TYPE_IMAGE : QQShare.SHARE_TO_QQ_TYPE_DEFAULT;
        if (mPicture) {
            String imgUrl = getRealPathFromURI(mBitmapUri);
            Log.d(Log.TAG, "imgUrl = " + imgUrl);
            params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, imgUrl);
        } else {
            params.putString(QQShare.SHARE_TO_QQ_TITLE, mTitle);
            params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, mUrl);
            params.putString(QQShare.SHARE_TO_QQ_SUMMARY, mContent);
            String imgUrl = TextUtils.isEmpty(mImgUrl) ? IMG_URL : mImgUrl;
            Log.d(Log.TAG, "imgUrl = " + imgUrl);
            params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imgUrl);
        }
        String appName = getResources().getString(R.string.app_name);
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, appName);
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, type);

        params.putInt(QQShare.SHARE_TO_QQ_EXT_INT,
                0x0 & QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);
        doShareToQQ(params);
    }
    
    /**
     * 用异步方式启动分�?
     * 
     * @param params
     */
    private void doShareToQQ(final Bundle params) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mQQShare.shareToQQ(QQShareActivity.this, params,
                        QQShareActivity.this);
            }
        }).start();
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                    getContentResolver(), uri);
            return bitmap;
        } catch (Exception e) {
            Log.d(Log.TAG, "error : " + e);
        }
        return null;
    }
}
