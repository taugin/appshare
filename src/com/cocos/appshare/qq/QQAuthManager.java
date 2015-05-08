package com.cocos.appshare.qq;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.cocos.appshare.util.Log;
import com.tencent.connect.auth.QQAuth;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

public class QQAuthManager implements IUiListener {

    private static final String APP_ID = "1103417764";

    private static final String OPEN_ID = "key_open_id";
    private static final String ACCESS_TOKEN = "key_access_token";
    private static final String EXPIRES_IN = "key_expires_in";

    private static QQAuthManager sQQAuthManager;

    private Activity mActivity;
    private Tencent mTencent;
    private QQAuth mQQAuth;

    public static QQAuthManager get(Activity activity) {
        if (!(activity instanceof Activity)) {
            throw new IllegalArgumentException(
                    "context should be a Activity instance");
        }
        if (sQQAuthManager == null) {
            sQQAuthManager = new QQAuthManager(activity);
        }
        return sQQAuthManager;
    }

    public QQAuthManager(Activity activity) {
        mActivity = activity;
        mTencent = Tencent.createInstance(APP_ID,
                mActivity.getApplicationContext());
        mQQAuth = QQAuth.createInstance(APP_ID,
                mActivity.getApplicationContext());
    }

    public void init() {
        Log.d(Log.TAG, "");
    }

    public QQAuth getQQAuth() {
        return mQQAuth;
    }
    private boolean setSession() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(mActivity);
        String openId = sharedPreferences.getString(OPEN_ID, "");
        String accessToken = sharedPreferences.getString(ACCESS_TOKEN, "");
        String expiresIn = sharedPreferences.getString(EXPIRES_IN, "0");
        long expiresTime = Long.parseLong(expiresIn);
        long expire = (expiresTime - System.currentTimeMillis()) / 1000;
        if (expire < 0) {
            return false;
        } else {
            mQQAuth.setOpenId(mActivity, openId);
            mQQAuth.setAccessToken(accessToken, String.valueOf(expire));
        }
        return true;
    }
    public void login() {
        Log.d(Log.TAG, "");
        if (!setSession()) {

        }
        mQQAuth.login(mActivity, "all", this);
        // mTencent.login(mActivity, "all", this);
        // mTencent.loginWithOEM(mActivity, "all", this, "10000144", "10000144",
        // "xxxx");
    }

    public void logout() {
        Log.d(Log.TAG, "");
        mQQAuth.logout(mActivity);
        clearSession();
        Toast.makeText(mActivity, "注销成功！", Toast.LENGTH_SHORT).show();
    }

    private void clearSession() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(mActivity);
        Editor editor = sharedPreferences.edit();
        editor.remove(OPEN_ID);
        editor.remove(ACCESS_TOKEN);
        editor.remove(EXPIRES_IN);
        editor.commit();
    }
    @SuppressLint("CommitPrefEdits")
    private void saveSession(String openId, String accessToken, String expiresIn) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(mActivity);
        long time = System.currentTimeMillis() + Long.parseLong(expiresIn)
                * 1000;
        Editor editor = sharedPreferences.edit();
        editor.putString(OPEN_ID, openId);
        editor.putString(ACCESS_TOKEN, accessToken);
        editor.putString(EXPIRES_IN, String.valueOf(time));
        editor.commit();
    }

    @Override
    public void onCancel() {
        Log.d(Log.TAG, "");
    }

    @Override
    public void onComplete(Object obj) {
        Log.d(Log.TAG, "obj = " + obj);
        try {
            JSONObject jsonObj = (JSONObject) obj;
            String openid = jsonObj.getString("openid");
            String expires_in = jsonObj.getString("expires_in");
            String access_token = jsonObj.getString("access_token");
            saveSession(openid, access_token, expires_in);
        } catch (JSONException e) {
            Log.d(Log.TAG, "error : " + e);
        }
        Toast.makeText(mActivity, "授权登录成功！", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(UiError e) {
        Log.d(Log.TAG, "e = " + e);
    }
}
