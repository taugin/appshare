package com.cocos.appshare.channel;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.TextUtils;

import com.cocos.appshare.util.Log;

public class ChannelConfig {

    private static final String HOME_BASEURL = "http://home.cocospay.com/";
    public static final String CHANNEL = "channel";
    public static String channel;
    public static String imei;

    public static String reemAppid; // 兑换码appid

    public static String flurryAppid;

    public static String clientAppid; // 强制更新sdk appkey

    public static String coco_aid;

    public static String coco_cid;

    private static byte[] base64DecodeChars = new byte[] { -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59,
            60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
            10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1,
            -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37,
            38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1,
            -1, -1 };

    public static void loadConfig(Context context)
            throws XmlPullParserException {

        Bundle metaData = null;
        try {
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            metaData = ai.metaData;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return;
        }

        channel = getMetaValue(metaData, "wlss_channel")
                .replace("channel:", "");

        if (!channel.equals("000023") && !channel.equals("000005")) {
            try {
                // Return an AssetManager instance for your application's
                // package
                InputStream is = context.getAssets().open("channal.xml");
                if (is != null) {
                    int size = is.available();
                    // Read the entire asset into a local byte buffer.
                    byte[] buffer = new byte[size];
                    is.read(buffer);
                    is.close();

                    // Convert the buffer into a string.
                    String text = new String(buffer, "GB2312");
                    byte[] b = decode(text);
                    String result = null;
                    result = new String(b, "utf-8");
                    channel = result;
                }
            } catch (IOException e) {
                // Should never happen!
                throw new RuntimeException(e);
            }
        }

        // imei = ((TelephonyManager)
        // context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        reemAppid = getMetaValue(metaData, "redeemAppid").replace("appid:", "");
        // clientAppid = getMetaValue(metaData,
        // "clientInstallKey").replace("appid:", "");
        flurryAppid = getMetaValue(metaData, "flurryAppid");
        coco_aid = getMetaValue(metaData, "bi_coco_aid");
        coco_cid = getMetaValue(metaData, "bi_coco_cid");
    }

    public static String getMetaValue(Bundle metaData, String metaKey) {
        return metaData.getString(metaKey);
    }

    public static String getConfigInfo() {
        HashMap<String, String> mapInfo = new HashMap<String, String>();
        mapInfo.put(CHANNEL, channel);
        JSONObject config = new JSONObject(mapInfo);
        return config.toString();
    }

    public static boolean isCM() {
        return "000266".equals(channel);
    }

    public static boolean isCT() {
        return "000032".equals(channel);
    }

    public static boolean isCMM() {
        return "000013".equals(channel);
    }

    public static boolean isCU() {
        return "000056".equals(channel);
    }

    public static boolean isTelcomOperators() {
        return isCM() || isCT() || isCMM() || isCU();
    }

    public static boolean isDebugOn() {
        return false;
    }

    public static byte[] decode(String str) throws UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer();
        byte[] data = str.getBytes("US-ASCII");
        int len = data.length;
        int i = 0;
        int b1, b2, b3, b4;
        while (i < len) {

            do {
                b1 = base64DecodeChars[data[i++]];
            } while (i < len && b1 == -1);
            if (b1 == -1)
                break;

            do {
                b2 = base64DecodeChars[data[i++]];
            } while (i < len && b2 == -1);
            if (b2 == -1)
                break;
            sb.append((char) ((b1 << 2) | ((b2 & 0x30) >>> 4)));

            do {
                b3 = data[i++];
                if (b3 == 61)
                    return sb.toString().getBytes("iso8859-1");
                b3 = base64DecodeChars[b3];
            } while (i < len && b3 == -1);
            if (b3 == -1)
                break;
            sb.append((char) (((b2 & 0x0f) << 4) | ((b3 & 0x3c) >>> 2)));

            do {
                b4 = data[i++];
                if (b4 == 61)
                    return sb.toString().getBytes("iso8859-1");
                b4 = base64DecodeChars[b4];
            } while (i < len && b4 == -1);
            if (b4 == -1)
                break;
            sb.append((char) (((b3 & 0x03) << 6) | b4));
        }
        return sb.toString().getBytes("utf-8");
    }

    public static String readChannelId(Context context) {
        String channel = null;
        try {
            // Return an AssetManager instance for your application's
            // package
            InputStream is = context.getAssets().open("channal.xml");
            if (is != null) {
                int size = is.available();
                // Read the entire asset into a local byte buffer.
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();

                // Convert the buffer into a string.
                String text = new String(buffer, "GB2312");
                byte[] b = decode(text);
                channel = new String(b, "utf-8");
            }
            return channel;
        } catch (IOException e) {
            Log.d(Log.TAG, "error : " + e);
        }
        return null;
    }

    public static String getChannelDownloadUrl(Context context) {
        String channel = readChannelId(context);
        if (TextUtils.isEmpty(channel)) {
            channel = "000000";
        }
        String url = HOME_BASEURL + "?channel=" + channel;
        return url;
    }
}