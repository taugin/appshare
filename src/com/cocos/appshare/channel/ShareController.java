package com.cocos.appshare.channel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.XmlResourceParser;
import android.text.TextUtils;

import com.cocos.appshare.util.Log;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class ShareController {

    private static ShareController sShareController;
    private Context mContext;
    private HashMap<String, ChannelItem> mShareMap;
    private ShareController(Context context) {
        mContext = context;
    }

    public static ShareController get(Context context) {
        if (sShareController == null) {
            sShareController = new ShareController(context);
        }
        sShareController.init();
        return sShareController;
    }

    private InputStream decodeString(InputStream is) {
        if (is == null) {
            return null;
        }
        try {
            byte[] buffer = new byte[1024];
            int read = -1;
            StringBuilder builder = new StringBuilder();
            while ((read = is.read(buffer)) > 0) {
                builder.append(new String(buffer, 0, read, "UTF-8"));
            }
            is.close();
            if (builder.length() > 0) {
                byte[] decodedString = ChannelConfig.decode(builder.toString());
                InputStream inputStream = new ByteArrayInputStream(decodedString);
                return inputStream;
            }
        } catch (IOException e) {
            Log.d(Log.TAG, "error : " + e);
        }
        return null;
    }
    private void init() {
        try {
            InputStream is = mContext.getAssets().open("channelshare.xml");
            // is = decodeString(is);
            if (is != null) {
                parseXml(is);
                is.close();
            }
        } catch (IOException e) {
            Log.d(Log.TAG, "error : " + e);
        }
    }

    private void parseXml(InputStream in) {
        int eventType;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xmlParser = factory.newPullParser();
            xmlParser.setInput(in, "UTF-8");
            eventType = xmlParser.getEventType();
            String strName = null;
            ChannelItem channelItem = null;
            while (eventType != XmlResourceParser.END_DOCUMENT) {
                if(eventType == XmlResourceParser.START_TAG) {
                    strName = xmlParser.getName();
                    if ("channelinfo".equals(strName)) {
                        mShareMap = new HashMap<String, ChannelItem>();
                    } else if ("item".equals(strName)) {
                        channelItem = new ChannelItem();
                    } else if ("channel".equals(strName)) {
                        channelItem.channelId = xmlParser.nextText();
                    } else if ("pkgname".equals(strName)) {
                        channelItem.pkgName = xmlParser.nextText();
                    } else if ("signmd5".equals(strName)) {
                        channelItem.signMd5 = xmlParser.nextText();
                    } else if ("wxappid".equals(strName)) {
                        channelItem.wxAppId = xmlParser.nextText();
                    }
                } else if(eventType == XmlResourceParser.END_TAG) {
                    strName = xmlParser.getName();
                    if ("item".equals(strName)) {
                        if (channelItem != null && !TextUtils.isEmpty(channelItem.channelId)) {
                            Log.d(Log.TAG, "channelItem : " + channelItem);
                            mShareMap.put(channelItem.channelId, channelItem);
                        }
                    }
                }
                eventType = xmlParser.next();
            }
        } catch (XmlPullParserException e) {
            Log.d(Log.TAG, "error : " + e);
        } catch (IOException e) {
            Log.d(Log.TAG, "error : " + e);
        }
    }

    public String getWxAppId(String channel) {
        Log.d(Log.TAG, "channel : " + channel);
        if (mShareMap != null) {
            if (mShareMap.containsKey(channel)) {
                ChannelItem item = mShareMap.get(channel);
                if (item != null) {
                    return item.wxAppId;
                }
            }/* else {
                ChannelItem item = mShareMap.get("000000");
                if (item != null) {
                    String pkgName = mContext.getPackageName();
                    String signMd5 = getSignMd5();
                    if ((pkgName != null && pkgName.equals(item.pkgName)) && (signMd5 != null && signMd5.equals(item.signMd5))) {
                        return item.wxAppId;
                    }
                }
            }*/
        }
        return null;
    }

    public boolean allowWxShare() {
        IWXAPI api = WXAPIFactory.createWXAPI(mContext, null, true);
        if (!api.isWXAppInstalled() || !api.isWXAppSupportAPI()) {
            return false;
        }
        String channel = ChannelConfig.readChannelId(mContext);
        String wxAppId = getWxAppId(channel);
        return !TextUtils.isEmpty(wxAppId);
    }

    public String getDefaulWxAppId() {
        return getWxAppId("000000");
    }

    public String getSignMd5() {
        try {
            PackageInfo info = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), PackageManager.GET_SIGNATURES);
            return stringToMD5(info.signatures[0].toByteArray());
        } catch (NameNotFoundException e) {
            Log.d(Log.TAG, "error : " + e);
        } catch (Exception e) {
            Log.d(Log.TAG, "error : " + e);
        }
        return null;
    }
    
    /**
     * 将字符串转成MD5值
     * 
     * @param string
     * @return
     */
    public static String stringToMD5(byte[] buffer) {
        byte[] hash = null;
        try {
            hash = MessageDigest.getInstance("MD5").digest(buffer);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    class ChannelItem {
        public String channelId;
        public String pkgName;
        public String signMd5;
        public String wxAppId;

        public String toString() {
            String str = "";
            str += channelId + " , ";
            str += signMd5 + " , ";
            str += wxAppId + " , ";
            str += pkgName;
            return str;
        }
    }
}
