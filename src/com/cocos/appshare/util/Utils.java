package com.cocos.appshare.util;

import android.content.Context;
import android.util.DisplayMetrics;

public class Utils {
    /**
     * 判断是否为平板
     * 
     * @return
     */
    public static boolean isPad(Context context) {
        try {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
            double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
            // 屏幕尺寸
            double screenInches = Math.sqrt(x + y);
            // 大于6尺寸则为Pad
            if (screenInches >= 6.0) {
                return true;
            }
        } catch (Exception e) {
            Log.d(Log.TAG, "error : " + e);
        }
        return false;
    }
}
