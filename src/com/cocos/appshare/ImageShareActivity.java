package com.cocos.appshare;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

import com.cocos.appshare.info.ShareInfo;
import com.cocos.appshare.util.Log;
import com.cocos.appshare.wx.WXShareHelper;

public class ImageShareActivity extends Activity implements OnClickListener {

    private static final int ID_PYQ = 0x10000001;
    private String mFileName = null;
    private ImageView mShareView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null) {
            mFileName = intent.getStringExtra("share_image");
        }
        Log.d(Log.TAG, "mFileName : " + mFileName);
        if (TextUtils.isEmpty(mFileName)) {
            finish();
            return ;
        }
        RelativeLayout layout = new RelativeLayout(this);
        layout.setBackgroundResource(android.R.color.white);
        mShareView = new ImageView(this);
        mShareView.setScaleType(ScaleType.CENTER_INSIDE);
        Bitmap bitmap = createShareBitmap(mFileName);
        mShareView.setImageBitmap(bitmap);
        layout.addView(mShareView);

        ImageView shareImg = new ImageView(this);
        shareImg.setOnClickListener(this);
        shareImg.setId(ID_PYQ);
        shareImg.setImageResource(R.drawable.share_pyq);
        shareImg.setScaleType(ScaleType.CENTER_INSIDE);
        int w = dp2px(this, 48);
        int h = dp2px(this, 48);
        RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(w, h);
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
            MediaPlayer mediaPlay = new MediaPlayer();
            mediaPlay.setDataSource(this, Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
            mediaPlay.prepare();
            mediaPlay.start();
        }catch(Exception e) {
            Log.d(Log.TAG, "error : " + e);
        }
    }

    private Bitmap createShareBitmap(String fileName) {
        Bitmap bitmap = BitmapFactory.decodeFile(fileName).copy(Bitmap.Config.ARGB_8888, true);
        Bitmap extra = BitmapFactory.decodeFile("/sdcard/erweima.jpg");
        if (bitmap != null) {
            Canvas canvas = new Canvas(bitmap);
            if (extra != null) {
                int bh = bitmap.getHeight();
                int eh = extra.getHeight();
                canvas.drawBitmap(extra, 0, bh - eh, null);
            }
        }
        return bitmap;
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
