package com.cocos.appshare;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

import com.cocos.appshare.info.ShareInfo;
import com.cocos.appshare.util.ImageCreator;
import com.cocos.appshare.util.Log;
import com.cocos.appshare.util.ShareConstant;
import com.cocos.appshare.wx.WXShareHelper;

public class SharePreview extends Activity implements OnClickListener {

    private static final int ID_PYQ = 0x10000001;
    private ImageView mShareView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String scoreImg = null;
        String qrCodeText = null;
        if (intent != null) {
            scoreImg = intent.getStringExtra(ShareConstant.SHOT_IMAGE);
            qrCodeText = intent.getStringExtra(ShareConstant.QRCODE_TEXT);
        }
        Log.d(Log.TAG, "scoreImg : " + scoreImg);
        Log.d(Log.TAG, "qrCodeImg : " + qrCodeText);
        if (TextUtils.isEmpty(scoreImg)) {
            finish();
            return ;
        }
        RelativeLayout layout = new RelativeLayout(this);
        layout.setBackgroundResource(android.R.color.white);
        mShareView = new ImageView(this);
        mShareView.setScaleType(ScaleType.CENTER_INSIDE);
        ImageCreator creator = new ImageCreator(this);
        Bitmap bitmap = creator.createWxShareWithQRcodeForHome(scoreImg, qrCodeText);
        Log.d(Log.TAG, "bitmap : " + bitmap);
        if (bitmap == null) {
            finish();
            return ;
        }
        mShareView.setImageBitmap(bitmap);
        layout.addView(mShareView);

        ImageButton shareImg = new ImageButton(this);
        shareImg.setOnClickListener(this);
        shareImg.setId(ID_PYQ);
        shareImg.setImageResource(R.drawable.share_pyq);
        shareImg.setScaleType(ScaleType.CENTER_INSIDE);
        int w = dp2px(this, 48);
        int h = dp2px(this, 48);
        RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(w, h);
        lp1.rightMargin = dp2px(this, 5);
        lp1.bottomMargin = dp2px(this, 5);
        lp1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lp1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layout.addView(shareImg, lp1);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int marginH = (int) (metrics.widthPixels * 0.02);
        int marginV = (int) (metrics.heightPixels * 0.02);
        int paddingH = (int) (metrics.widthPixels * 0.03);
        int paddingV = (int) (metrics.heightPixels * 0.03);
        // layout.setPadding(paddingH, paddingV, paddingH, paddingV);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1, -1);
        // params.leftMargin = params.rightMargin = marginH;
        // params.topMargin = params.bottomMargin = marginV;
        setContentView(layout, params);

        try {
            MediaPlayer mediaPlay = MediaPlayer.create(this, R.raw.camera_click);
            mediaPlay.start();
        }catch(Exception e) {
            Log.d(Log.TAG, "error : " + e);
        }
    }

    @Override
    public void onClick(View v) {
        finish();
        wxShare();
    }

    private Bitmap getImageBitmap() {
        if (mShareView != null) {
            mShareView.setDrawingCacheEnabled(true);
            Bitmap srcBmp = mShareView.getDrawingCache();
            Bitmap dstBmp = Bitmap.createBitmap(srcBmp);
            mShareView.setDrawingCacheEnabled(false);
            return dstBmp;
        }
        return null;
    }

    private void wxShare() {
        ShareInfo shareInfo = new ShareInfo();
        Bitmap bitmap = getImageBitmap();
        if (bitmap == null) {
            return ;
        }
        shareInfo.mBitmap = new WeakReference<Bitmap>(bitmap);
        shareInfo.mPicture = true;
        WXShareHelper mWXShare = new WXShareHelper(this, shareInfo);
        mWXShare.shareToWechatCircleFriends();
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
}
