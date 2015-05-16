package com.cocos.appshare.wx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;

import com.cocos.appshare.R;
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
        String extMessage = mContext.getResources().getString(R.string.wxext);
        wxShareFromSDK(bitmap, extMessage);
    }

    public void sharePicToWxPyq(String shotImage, String extMessage) {
        ImageCreator creator = new ImageCreator(mContext);
        Bitmap bitmap = creator.createWxShareWithQRcodeForHome(shotImage);
        if (bitmap == null) {
            return ;
        }
        wxShareFromSDK(bitmap, extMessage);
    }

    private void wxShareFromSDK(Bitmap bitmap, String extMessage) {
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
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setClassName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI");
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, shareUri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra("Kdescription", extMessage);
                try {
                    mContext.startActivity(intent);
                } catch(ActivityNotFoundException e) {
                    
                }
            }
        }
    }
}
