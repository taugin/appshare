package com.cocos.appshare.wx;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.text.TextUtils;

import com.cocos.appshare.util.Log;

public class ShareController {

    private static ShareController sShareController;
    private Context mContext;
    private HashMap<String, String> mShareMap;
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

    private void init() {
        try {
            InputStream is = mContext.getAssets().open("channelshare.xml");
            parseXml(is);
            is.close();
        } catch (IOException e) {
            Log.d(Log.TAG, "error : " + e);
        }
    }

    private void parseXml(InputStream in) {
        int eventType;
        String channel = null;
        String wxappid = null;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xmlParser = factory.newPullParser();
            xmlParser.setInput(in, "UTF-8");
            eventType = xmlParser.getEventType();
            while (eventType != XmlResourceParser.END_DOCUMENT) {
                if(eventType == XmlResourceParser.START_TAG) {
                    String strName = xmlParser.getName();
                    if ("channelinfo".equals(strName)) {
                        mShareMap = new HashMap<String, String>();
                    } else if ("item".equals(strName)) {
                        channel = xmlParser.getAttributeValue(null, "channel");
                        wxappid = xmlParser.getAttributeValue(null, "wxappid");
                        if (!TextUtils.isEmpty(channel) && !TextUtils.isEmpty(channel)) {
                            mShareMap.put(channel, wxappid);
                        }
                        Log.d(Log.TAG, "channel : " + channel + " , wxappid : " + wxappid);
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

    public boolean allowWxShare(String channel) {
        if (mShareMap == null) {
            init();
        }
        if (mShareMap != null) {
            if (mShareMap.containsKey(channel)) {
                String wxAppId = mShareMap.get(channel);
                if (!TextUtils.isEmpty(wxAppId)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public String getWxAppId(String channel) {
        if (mShareMap == null) {
            init();
        }
        if (mShareMap != null) {
            if (mShareMap.containsKey(channel)) {
                return mShareMap.get(channel);
            }
        }
        return null;
    }

    public String getDefaulWxAppId() {
        return getWxAppId("000000");
    }
}
