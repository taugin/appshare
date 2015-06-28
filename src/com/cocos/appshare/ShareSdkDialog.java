package com.cocos.appshare;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.cocos.appshare.util.Log;
import com.cocos.appshare.util.Utils;

public class ShareSdkDialog extends Dialog implements OnItemClickListener {

    private Intent mSendIntent;
    private Context mContext;
    private GridView mGridView;
    private ImageAdapter mImageAdapter;
    private OnShareAppItemClickListener mOnShareAppItemClickListener;

    public ShareSdkDialog(Context context, Intent shareIntent) {
        super(context, R.style.share_dialog_style);
        mSendIntent = shareIntent;
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.share_dlg_layout_gridview);
        mGridView = (GridView) findViewById(R.id.share_gridview);
        mGridView.setNumColumns(Utils.isPad(mContext) ? 4 : 2);

        List<ShareClass> list = getAllShareIntent(mSendIntent);
        mImageAdapter = new ImageAdapter(mContext, list);
        mGridView.setAdapter(mImageAdapter);
        mGridView.setOnItemClickListener(this);
        
        Window dialogWindow = getWindow();  
        LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        lp.width = (int) (metrics.widthPixels * 0.8f);
        //lp.height = (int) (metrics.heightPixels * 0.8f);
        dialogWindow.setAttributes(lp);
    }

    private class ImageAdapter extends ArrayAdapter<ShareClass> {

        private Context mContext;

        public ImageAdapter(Context context, List<ShareClass> list) {
            super(context, 0, list);
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            TextView textView = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.share_dlg_item, null);
                textView = (TextView) convertView;
            } else {
                textView = (TextView) convertView;
            }
            ShareClass shareClass = getItem(position);
            if (shareClass != null) {
                textView.setText(shareClass.shareName);
                if (shareClass.shareIcon != null) {
                    shareClass.shareIcon.setBounds(0, 0, dp2px(mContext, 48), dp2px(mContext, 48));
                    textView.setCompoundDrawables(null, shareClass.shareIcon, null, null);
                }
            }
            return convertView;
        }

    }
    private List<ShareClass> getAllShareIntent(Intent intent) {
        PackageManager pm = getContext().getPackageManager();
        List<ResolveInfo> lists = pm.queryIntentActivities(intent, 0);
        if (lists == null || lists.size() <= 0) {
            return null;
        }
        List<ShareClass> allShareClass = new ArrayList<ShareClass>();
        ShareClass shareClass = null;
        for (ResolveInfo info : lists) {
            if (info != null && info.activityInfo != null && !TextUtils.isEmpty(info.activityInfo.packageName)) {
                // Log.d(Log.TAG, "info.activityInfo.packageName : " + info.activityInfo.packageName + " , " + info.activityInfo.name);
                shareClass = new ShareClass();
                try {
                    String title = info.loadLabel(pm).toString();
                    if (TextUtils.isEmpty(title)) {
                        title = info.activityInfo.loadLabel(pm).toString();
                    }
                    shareClass.shareName = title;
                    shareClass.shareIcon = info.activityInfo.loadIcon(pm);
                    shareClass.appName = info.activityInfo.applicationInfo.loadLabel(pm).toString();
                } catch(Exception e) {
                    Log.d(Log.TAG, "error : " + e);
                }
                shareClass.componentName = new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
                allShareClass.add(shareClass);
            }
        }
        return allShareClass;
    }

    class ShareClass {
        public String appName;
        public String shareName;
        public Drawable shareIcon;
        public ComponentName componentName;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        dismiss();
        if (mImageAdapter != null) {
            ShareClass shareClass = mImageAdapter.getItem(position);
            if (shareClass != null && shareClass.componentName != null) {
                try {
                    mSendIntent.setComponent(shareClass.componentName);
                    mContext.startActivity(mSendIntent);
                    if (mOnShareAppItemClickListener != null) {
                        mOnShareAppItemClickListener.onShareAppItemClick(shareClass.appName + "-" + shareClass.shareName);
                    }
                } catch(ActivityNotFoundException e) {
                    Log.d(Log.TAG, "error : " + e);
                } catch(Exception e) {
                    Log.d(Log.TAG, "error : " + e);
                }
            }
        }
    }

    public int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public int px2dp(Context context, float px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        Log.d(Log.TAG, "scale = " + scale);
        return (int) (px / scale + 0.5f);
    }

    public void setOnShareAppItemClickListener(OnShareAppItemClickListener l) {
        mOnShareAppItemClickListener = l;
    }
}
