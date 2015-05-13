package com.cocos.appshare.util;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.DisplayMetrics;

public class ImageCreator {

    private Context mContext;
    public ImageCreator(Context context) {
        mContext = context;
    }

    public Bitmap createWxShareWithQRcodeForHome(String scoreImg, String qrCodeImg) {
        String bgImg = "share_bg.png";
        String logoImg = "eyebrow.png";
        return createWxShareWithQRcodeForHome(bgImg, scoreImg, qrCodeImg, logoImg);
    }

    public Bitmap createWxShareWithQRcodeForHome(String bgImg, String scoreImg, String qrCodeImg, String logoImg) {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        Bitmap canvasBmp = Bitmap.createBitmap(metrics.widthPixels, metrics.heightPixels, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(canvasBmp);
        Paint paint = new Paint();
        //Xfermode oldXform = paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        //paint.setAlpha(255);
        Bitmap bgBmp = null;
        Bitmap scoreBmp = null;
        Bitmap qrCodeBmp = null;
        Bitmap logoBmp = null;
        InputStream is = null;
        try {
            is = mContext.getAssets().open(bgImg);
            bgBmp = BitmapFactory.decodeStream(is);
            is.close();
            is = mContext.getAssets().open(qrCodeImg);
            qrCodeBmp = BitmapFactory.decodeStream(is);
            is.close();
            is = mContext.getAssets().open(logoImg);
            logoBmp = BitmapFactory.decodeStream(is);
            is.close();
        }catch(Exception e) {
            Log.d(Log.TAG, "error : " + e);
        }

        scoreBmp = BitmapFactory.decodeFile(scoreImg);

        float margin = dp2px(mContext, 15);
        float dx = 0;
        float dy = 0;

        if (bgBmp != null) {
            canvas.drawBitmap(bgBmp, 0, 0, null);
            bgBmp.recycle();
            bgBmp = null;
        }

        int logoHeight = 0;
        if (logoBmp != null) {
            int logoW = logoBmp.getWidth();
            int logoH = logoBmp.getHeight();
            // logoHeight = Math.min(logoH, metrics.heightPixels / 4);
            logoHeight = metrics.heightPixels / 4;
            float scale = (float) logoHeight / logoH;
            logoW = (int) (scale * logoW);
            dx = (metrics.widthPixels - logoW) / 2;
            Log.d(Log.TAG, "dx : " + dx + " , logoHeight : " + logoHeight);
            Matrix matrix = new Matrix();
            matrix.setScale(scale, scale);
            canvas.save();
            canvas.translate(dx, 0);
            canvas.drawBitmap(logoBmp, matrix, paint);
            canvas.restore();
            logoBmp.recycle();
            logoBmp = null;
        }

        if (scoreBmp != null) {
            int w = scoreBmp.getWidth();
            int h = scoreBmp.getHeight();
            int minH = metrics.heightPixels - metrics.heightPixels/4;
            float scale = (float)minH / h;
            int scroreW = (int) (w * scale);
            dx = (metrics.widthPixels - scroreW) / 2;
            dy = logoHeight + margin;

            // Draw Round Rect
            drawRoundRect(canvas, paint, dx, logoHeight, metrics, margin);

            Matrix matrix = new Matrix();
            matrix.setScale(scale, scale);
            canvas.save();
            canvas.translate(dx, dy);
            canvas.drawBitmap(scoreBmp, matrix, paint);
            canvas.restore();
            scoreBmp.recycle();
            scoreBmp = null;

            if (qrCodeBmp != null) {
                int ew = qrCodeBmp.getWidth();
                int eh = qrCodeBmp.getHeight();
                int qrCodeW = (int) ((float)scroreW / 3);
                int qrCodeH = qrCodeW;
                float qrCodeScale =  (float)qrCodeW / ew;
                Log.d(Log.TAG, "qrCodeW : " + qrCodeW);
                dx = (metrics.widthPixels - qrCodeW) / 2;
                dy = metrics.heightPixels - qrCodeH - margin;
                matrix.setScale(qrCodeScale, qrCodeScale);
                canvas.save();
                canvas.translate(dx, dy);
                canvas.drawBitmap(qrCodeBmp, matrix, null);
                canvas.restore();
                qrCodeBmp.recycle();
                qrCodeBmp = null;
            }
        }

        return canvasBmp;
    }
    
    private void drawRoundRect(Canvas canvas, Paint paint, float dx, float dy, DisplayMetrics metrics, float margin) {
        float radius = dp2px(mContext, 15);
        int rw = (int) (metrics.widthPixels - (dx - margin));
        int rh = metrics.heightPixels;
        RectF rect = new RectF(dx - margin, dy, rw, rh + margin);
        paint.setColor(Color.parseColor("#FF086988"));
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(dp2px(mContext, 3));
        canvas.drawRoundRect(rect, radius, radius, paint);
    }

    public int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
