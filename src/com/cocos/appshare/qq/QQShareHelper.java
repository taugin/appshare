package com.cocos.appshare.qq;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import com.cocos.appshare.info.ShareInfo;

public class QQShareHelper {

    private Context mContext;
    private ShareInfo mShareInfo;

    public QQShareHelper(Context context, ShareInfo shareInfo) {
        mContext = context;
        mShareInfo = shareInfo;
    }

    public void shareToQQ() {
        Intent intent = new Intent(mContext, QQShareActivity.class);
        intent.putExtra("imgurl", mShareInfo.mImgUrl);
        intent.putExtra("picture", mShareInfo.mPicture);
        intent.putExtra("title", mShareInfo.mShareTitle);
        intent.putExtra("content", mShareInfo.mShareContent);
        intent.putExtra("url", mShareInfo.mShareUrl);
        Bitmap bitmap = null;
        if (mShareInfo.mBitmap != null) {
            bitmap = mShareInfo.mBitmap.get();
        }
        if (bitmap != null) {
            Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(
                    mContext.getContentResolver(), bitmap, null, null));
            intent.putExtra("sharebitmap", uri);
        }
        mContext.startActivity(intent);
    }
}
