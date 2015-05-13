package com.cocos.appshare.info;

import java.lang.ref.WeakReference;

import android.graphics.Bitmap;

public class ShareInfo {
    public String mWxAppId;
    public boolean mPicture = false;
    public String mImgUrl;
    public String mShareTitle;
    public String mShareContent;
    public String mShareUrl;
    public WeakReference<Bitmap> mThumbIcon;
    public WeakReference<Bitmap> mBitmap;
}
