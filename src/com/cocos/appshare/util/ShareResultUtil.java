package com.cocos.appshare.util;

import com.cocos.appshare.OnShareResultListener;

public class ShareResultUtil {

    private static OnShareResultListener mOnShareResultListener;

    public static void setOnShareResultListener(OnShareResultListener l) {
        mOnShareResultListener = l;
    }

    public static OnShareResultListener getOnShareResultListener() {
        return mOnShareResultListener;
    }
}
