package com.cocos.appshare.wx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import com.cocos.appshare.R;
import com.cocos.appshare.channel.ChannelConfig;
import com.cocos.appshare.util.ImageCreator;
import com.cocos.appshare.util.Log;

public class WXShareNoSDK {

    private Context mContext;
    public WXShareNoSDK(Context context) {
        mContext = context;
    }

    public void sharePicToWxPyq(String shotImage) {
        ImageCreator creator = new ImageCreator(mContext);
        Bitmap bitmap = creator.createWxShareWithQRcodeForHome(shotImage);
        if (bitmap == null) {
            return ;
        }
        String downUrl = ChannelConfig.getChannelDownloadUrl(mContext);
        String extMessage = mContext.getResources().getString(R.string.wxext, downUrl);
        wxSharePicNoSDK(bitmap, extMessage);
    }

    public void sharePicToWxPyq(String shotImage, String extMessage) {
        ImageCreator creator = new ImageCreator(mContext);
        Bitmap bitmap = creator.createWxShareWithQRcodeForHome(shotImage);
        if (bitmap == null) {
            return ;
        }
        wxSharePicNoSDK(bitmap, extMessage);
    }

    private void wxSharePicNoSDK(Bitmap bitmap, String extMessage) {
        if (bitmap == null) {
            return ;
        }
        File externalDir = Environment.getExternalStorageDirectory();
        if (externalDir != null) {
            String shareImage = externalDir.getAbsoluteFile() + File.separator + "wxshare.jpg";
            try {
                FileOutputStream fos = new FileOutputStream(shareImage);
                bitmap.compress(CompressFormat.JPEG, 85, fos);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(Log.TAG, "error : " + e);
            } catch (IOException e) {
                Log.d(Log.TAG, "error : " + e);
            }
            File shareImageFile = new File(shareImage);
            if (shareImageFile.exists()) {
                Uri shareUri = Uri.fromFile(shareImageFile);
                //Intent intent = getWxSharePicIntent();
                //intent.setClassName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI");
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, shareUri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra("Kdescription", extMessage);
                try {
                    mContext.startActivity(getWxSharePicIntent(intent));
                } catch(ActivityNotFoundException e) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            String text = mContext.getResources().getString(R.string.share_activity_notfound);
                            Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
    }

    private Intent getWxSharePicIntent(Intent intent) {
        PackageManager pm = mContext.getPackageManager();
        List<ResolveInfo> lists = pm.queryIntentActivities(intent, 0);
        if (lists == null) {
            return intent;
        }
        for (ResolveInfo info : lists) {
            if (info != null && info.activityInfo != null && !TextUtils.isEmpty(info.activityInfo.packageName)) {
                if (info.activityInfo.packageName.equals("com.tencent.mm") && info.activityInfo.name.contains("ShareToTimeLineUI")) {
                    Log.d(Log.TAG, "info.activityInfo.packageName : " + info.activityInfo.packageName + " , " + info.activityInfo.name);
                    intent.setClassName("com.tencent.mm", info.activityInfo.name);
                    break;
                }
            }
        }
        return intent;
    }
}
