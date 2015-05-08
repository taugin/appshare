package com.cocos.appshare.wx;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.Toast;

import com.cocos.appshare.R;
import com.cocos.appshare.info.ShareInfo;
import com.cocos.appshare.util.Log;
import com.cocos.appshare.util.WXUtil;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXShareHelper {
    // 发送的目标场景，WXSceneSession表示发送到会话
    private static final int WXSceneSession = 0;
    // 发送的目标场景，WXSceneTimeline表示发送朋友圈
    private static final int WXSceneTimeline = 1;

    private static final String APP_ID = "wx8b2508265fe571c6";
    private static final String APP_ID_DX = "wx8cbde386754aba50";
    private IWXAPI api;
    private ShareInfo mShareInfo;
    private Context mContext;

    public WXShareHelper(Context context, ShareInfo shareInfo) {
        mContext = context;
        mShareInfo = shareInfo;
        String appId = APP_ID;
        if ("com.bhvr.homejam.egame".equals(context.getPackageName())) {
            appId = APP_ID_DX;
        }
        api = WXAPIFactory.createWXAPI(context, appId, true);
        api.registerApp(appId);
    }

    public boolean shareToWechatFriends() {
        if (mShareInfo.mPicture) {
            return sharePicToWx(WXSceneSession);
        } else {
            return shareToWechat(WXSceneSession);
        }
    }

    public boolean shareToWechatCircleFriends() {
        if (mShareInfo.mPicture) {
            return sharePicToWx(WXSceneTimeline);
        } else {
            return shareToWechat(WXSceneTimeline);
        }
    }

    private boolean shareToWechat(int scene) {
        Log.d(Log.TAG, "mShareInfo = " + mShareInfo.mShareTitle);
        if (!api.isWXAppInstalled() || !api.isWXAppSupportAPI()) {
            Toast.makeText(mContext, R.string.not_installed_tip,
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = mShareInfo.mShareUrl;
        WXMediaMessage msg = new WXMediaMessage(webpage);
        // msg.title = mContext.getResources().getString(R.string.app_name);
        msg.title = mShareInfo.mShareTitle;
        if (scene == WXSceneTimeline) {
            if (!TextUtils.isEmpty(mShareInfo.mShareContent)) {
                msg.title = mShareInfo.mShareContent;
            }
        }
        msg.description = mShareInfo.mShareContent;
        try {
            Drawable drawable = mContext.getApplicationInfo().loadIcon(mContext.getPackageManager());
            int w = drawable.getIntrinsicWidth();
            int h = drawable.getIntrinsicHeight();
            Bitmap bmp = Bitmap.createBitmap(w, h, Config.ARGB_8888);
            drawable.setBounds(0, 0, w, h);
            Canvas canvas = new Canvas(bmp);
            drawable.draw(canvas);
            if (mShareInfo.mThumbIcon != null) {
                Bitmap thumb = mShareInfo.mThumbIcon.get();
                if (thumb != null) {
                    msg.setThumbImage(thumb);
                }
            } else {
                msg.setThumbImage(bmp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = scene;
        api.sendReq(req);
        return true;
    }

    private boolean sharePicToWx(int scene) {
        if (!api.isWXAppInstalled() || !api.isWXAppSupportAPI()) {
            Toast.makeText(mContext, R.string.not_installed_tip,
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        if (mShareInfo.mBitmap == null || mShareInfo.mBitmap.get() == null) {
            return false;
        }
        Bitmap bitmap = mShareInfo.mBitmap.get();
        Log.d(Log.TAG, "bitmap = " + bitmap);
        WXImageObject imgObj = new WXImageObject(scaleBitmapIfneed(bitmap));
        // imgObj.imageData = WXUtil.bmpToByteArray(bitmap, false);
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;
        msg.thumbData = createThumbData(bitmap);
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = "img" + String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = scene;
        api.sendReq(req);
        bitmap.recycle();
        bitmap = null;
        return true;
    }

    private byte[] createThumbData(Bitmap bitmap) {
        bitmap = scaleThumbIfneed(bitmap);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int options = 90;
        bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
        while (baos.toByteArray().length > 32 * 1024) {
            baos.reset();
            options -= 10;
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
            Log.d(Log.TAG,
                    "options : " + options + " , len : "
                            + baos.toByteArray().length);
        }
        byte [] thumb = baos.toByteArray();
        try {
            baos.close();
        } catch (IOException e) {
            Log.d(Log.TAG, "error : " + e);
        }
        return thumb;
    }

    private Bitmap scaleBitmapIfneed(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Bitmap.Config config = bitmap.getConfig();
        int bytePerPixel = 1;
        if (config == Bitmap.Config.ALPHA_8) {
            bytePerPixel = 1;
        } else if (config == Bitmap.Config.RGB_565) {
            bytePerPixel = 2;
        } else if (config == Bitmap.Config.ARGB_4444) {
            bytePerPixel = 2;
        } else if (config == Bitmap.Config.ARGB_8888) {
            bytePerPixel = 4;
        }
        int byteCount = w * h * bytePerPixel;
        if (byteCount > 10 * 1024 * 1024) {
            double scalePow = 10 * 1024 * 1024 / (double) byteCount;
            float scale = (float) Math.sqrt(scalePow);
            int newW = (int) (w * scale);
            int newH = (int) (h * scale);
            bitmap = Bitmap.createScaledBitmap(bitmap, newW, newH, false);
        }
        return bitmap;
    }

    private Bitmap scaleThumbIfneed(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Bitmap.Config config = bitmap.getConfig();
        int bytePerPixel = 1;
        if (config == Bitmap.Config.ALPHA_8) {
            bytePerPixel = 1;
        } else if (config == Bitmap.Config.RGB_565) {
            bytePerPixel = 2;
        } else if (config == Bitmap.Config.ARGB_4444) {
            bytePerPixel = 2;
        } else if (config == Bitmap.Config.ARGB_8888) {
            bytePerPixel = 4;
        }
        int byteCount = w * h * bytePerPixel;
        if (byteCount > 320 * 1024) {
            double scalePow = 320 * 1024 / (double) byteCount;
            float scale = (float) Math.sqrt(scalePow);
            int newW = (int) (w * scale);
            int newH = (int) (h * scale);
            bitmap = Bitmap.createScaledBitmap(bitmap, newW, newH, false);
        }
        return bitmap;
    }
}
