package com.cocos.appshare;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.cocos.appshare.info.ShareInfo;
import com.cocos.appshare.qq.QQShareHelper;
import com.cocos.appshare.wx.WXShareHelper;

public class ShareAppDialog extends Dialog implements OnItemClickListener {
    private static final int ID_SHARE_WX_FRIENDS = 0;
    private static final int ID_SHARE_WX_CIRCLE_FRIENDS = 1;
    private static final int ID_SHARE_QQ = 2;
    private static final int ID_SHARE_MORE = 3;

    private static final int[][] SHARE_ITEM = {
            { ID_SHARE_WX_FRIENDS, R.string.share_wx_friends, R.drawable.share_wx },
            { ID_SHARE_WX_CIRCLE_FRIENDS, R.string.share_wx_circle_friends, R.drawable.share_pyq },
            { ID_SHARE_QQ, R.string.share_qq, R.drawable.share_qq },
            { ID_SHARE_MORE, R.string.share_more, R.drawable.share_more } };

    private WXShareHelper mWXShare;
    private QQShareHelper mQQShare;
    private ShareInfo mShareInfo;
    private Context mContext;
    private GridView mGridView;
    private ImageAdapter mImageAdapter;
    private OnShareAppItemClickListener mOnShareAppItemClickListener;

    public ShareAppDialog(Context context, ShareInfo shareInfo) {
        super(context, R.style.translucent);
        setCanceledOnTouchOutside(false);
        mShareInfo = shareInfo;
        mContext = context;
        mWXShare = new WXShareHelper(getContext(), shareInfo);
        mQQShare = new QQShareHelper(getContext(), shareInfo);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.share_dlg_layout_gridview);
        mGridView = (GridView) findViewById(R.id.share_gridview);
        mImageAdapter = new ImageAdapter(mContext);
        mGridView.setAdapter(mImageAdapter);
        mGridView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        Intent intent = null;
        switch (SHARE_ITEM[position][0]) {
        case ID_SHARE_WX_FRIENDS:
            mWXShare.shareToWechatFriends();
            if (mOnShareAppItemClickListener != null) {
                String text = mContext.getResources().getString(SHARE_ITEM[position][1]);
                mOnShareAppItemClickListener.onShareAppItemClick(text);
            }
                break;
        case ID_SHARE_WX_CIRCLE_FRIENDS:
            mWXShare.shareToWechatCircleFriends();
            if (mOnShareAppItemClickListener != null) {
                String text = mContext.getResources().getString(SHARE_ITEM[position][1]);
                mOnShareAppItemClickListener.onShareAppItemClick(text);
            }
                break;
        case ID_SHARE_QQ:
            mQQShare.shareToQQ();
            if (mOnShareAppItemClickListener != null) {
                String text = mContext.getResources().getString(SHARE_ITEM[position][1]);
                mOnShareAppItemClickListener.onShareAppItemClick(text);
            }
            break;
        case ID_SHARE_MORE:
            if (mShareInfo.mPicture) {
                intent = getSharePicIntent();
            } else {
                intent = getShareIntent();
            }
            if (intent != null) {
                // mContext.startActivity(intent);
                ShareSdkDialog dialog = new ShareSdkDialog(mContext, intent);
                dialog.setOnShareAppItemClickListener(mOnShareAppItemClickListener);
                dialog.show();
            }
            break;
        }
        dismiss();
    }

    private Intent getShareIntent() {
        String extraText = mShareInfo.mShareContent + " "
                + mShareInfo.mShareUrl;
        String title = mContext.getResources().getString(R.string.app_name);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
        intent.putExtra(Intent.EXTRA_TEXT, extraText);
        Intent shareIntent = Intent.createChooser(intent, title);
        return intent;
    }

    private Intent getSharePicIntent() {
        if (mShareInfo.mBitmap == null || mShareInfo.mBitmap.get() == null) {
            return null;
        }
        Bitmap bitmap = mShareInfo.mBitmap.get();
        Intent intent = new Intent(Intent.ACTION_SEND);
        // TODO
        intent.setType("image/*");
        Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(
                mContext.getContentResolver(), bitmap, null, null));
        intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        Intent shareIntent = Intent.createChooser(intent, "分享");
        return intent;
    }

    private class ImageAdapter extends BaseAdapter {

        private Context mContext;

        public ImageAdapter(Context context) {
            mContext = context;
        }
        @Override
        public int getCount() {
            return SHARE_ITEM.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
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
            textView.setText(SHARE_ITEM[position][1]);
            textView.setCompoundDrawablesWithIntrinsicBounds(0,
                    SHARE_ITEM[position][2], 0, 0);
            return convertView;
        }

    }

    public void setOnShareAppItemClickListener(OnShareAppItemClickListener l) {
        mOnShareAppItemClickListener = l;
    }
}
