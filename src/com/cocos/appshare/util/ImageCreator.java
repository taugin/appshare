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

import com.cocos.appshare.R;

public class ImageCreator {

    private static final int FACTION = 8;
    private Context mContext;
    private float margin = 0;
    private int mScreenWidth = 0;
    private int mScreenHeight = 0;

    private RectF mHeaderRect;
    private RectF mScoreRect;
    private RectF mRoundRect;
    private RectF mQRCodeRect;
    private RectF mFooterRect;

    public ImageCreator(Context context) {
        mContext = context;
        margin = dp2px(mContext, 15);
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
    }

    public Bitmap createWxShareWithQRcodeForHome(String shotImg, String qrText) {
        String bgImg = "share_bg.png";
        String logoImg = "share_logo.png";
        String footerImg = "share_footer.png";
        return createWxShareWithQRcodeForHome(bgImg, shotImg, qrText, logoImg, footerImg);
    }

    public Bitmap createWxShareWithQRcodeForHome(String bgImg, String screenshotImg, String qrText, String logoImg, String footerImg) {
        Bitmap canvasBmp = Bitmap.createBitmap(mScreenWidth, mScreenHeight, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(canvasBmp);
        Paint paint = new Paint();
        //Xfermode oldXform = paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        //paint.setAlpha(255);

        Bitmap bgBmp = decodeResources(R.drawable.share_bg);
        drawBackground(canvas, bgBmp);

        Bitmap logoBmp = decodeResources(R.drawable.share_logo);
        drawHeader(canvas, logoBmp);

        Bitmap shotImg = BitmapFactory.decodeFile(screenshotImg);
        drawBody(canvas, shotImg, paint);

        drawRoundRect(canvas);

        drawRQCodeBmp(canvas, qrText);

        Bitmap footerBmp = decodeResources(R.drawable.share_footer);
        drawFooter(canvas, footerBmp);
        return canvasBmp;
    }

    private void drawBackground(Canvas canvas, Bitmap bgBmp) {
        if (bgBmp != null) {
            canvas.drawBitmap(bgBmp, 0, 0, null);
            bgBmp.recycle();
            bgBmp = null;
        }
    }

    private void drawHeader(Canvas canvas, Bitmap logoBmp) {
        if (logoBmp != null) {
            int logoW = logoBmp.getWidth();
            int logoH = logoBmp.getHeight();
            int logoHeight = mScreenHeight / FACTION;
            float scale = (float) logoHeight / logoH;
            logoW = (int) (scale * logoW);
            float dx = (mScreenWidth - logoW) / 2;
            mHeaderRect = new RectF(dx, 0, dx + logoW, logoHeight);
            Matrix matrix = new Matrix();
            matrix.setScale(scale, scale);
            canvas.save();
            canvas.translate(dx, 0);
            canvas.drawBitmap(logoBmp, matrix, null);
            canvas.restore();
            logoBmp.recycle();
            logoBmp = null;
        }
    }

    private void drawBody(Canvas canvas, Bitmap shotImg, Paint paint) {
        if (shotImg != null) {
            int w = shotImg.getWidth();
            int h = shotImg.getHeight();
            int scoreH = mScreenHeight - 2 * mScreenHeight/FACTION - 2 * (int)margin;
            float scale = (float)scoreH / h;
            int scoreW = (int) (w * scale);
            float left = (mScreenWidth - scoreW) / 2;
            float top = mScreenHeight/FACTION + margin;
            mScoreRect = new RectF(left, top, left + scoreW, top + scoreH);
            // Draw Round Rect
            Matrix matrix = new Matrix();
            matrix.setScale(scale, scale);
            canvas.save();
            canvas.translate(left, top);
            canvas.drawBitmap(shotImg, matrix, paint);
            canvas.restore();
            shotImg.recycle();
            shotImg = null;
            System.gc();
        }
    }

    private void drawRoundRect(Canvas canvas) {
        float radius = dp2px(mContext, 15);
        mRoundRect = new RectF();
        mRoundRect.left = mScoreRect.left - margin;
        mRoundRect.top = mScoreRect.top - margin;
        mRoundRect.right = mScoreRect.right + margin;
        mRoundRect.bottom = mScoreRect.bottom + margin;
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#FF086988"));
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(dp2px(mContext, 3));
        paint.setAntiAlias(true);
        canvas.drawRoundRect(mRoundRect, radius, radius, paint);
    }

    private void drawRQCodeBmp(Canvas canvas, String qrText) {
        QRCodeHelper helper = new QRCodeHelper(mContext);
        int qrCodeW = (int)mScoreRect.width() / 2;
        int qrCodeH = (int)mScoreRect.width() / 2;
        Bitmap qrCodeBmp = helper.createImage(qrText, qrCodeW, qrCodeH);
        if (qrCodeBmp != null) {
            mQRCodeRect = new RectF();
            mQRCodeRect.left = mScoreRect.left + margin;
            mQRCodeRect.top = mScoreRect.bottom - qrCodeH - margin;
            mQRCodeRect.right = mScoreRect.left + qrCodeW;
            mQRCodeRect.bottom = mScoreRect.bottom - margin;
            float dx = mQRCodeRect.left;
            float dy = mQRCodeRect.top;
            canvas.save();
            canvas.translate(dx, dy);
            canvas.drawBitmap(qrCodeBmp, 0, 0, null);
            canvas.restore();
            qrCodeBmp.recycle();
            qrCodeBmp = null;
            System.gc();
            float x = mScoreRect.left + margin;
            float y = mScoreRect.bottom - dp2px(mContext, 3);
            Paint paint = new Paint();
            paint.setTextSize(margin - dp2px(mContext, 4));
            paint.setColor(Color.WHITE);
            paint.setAntiAlias(true);
            String text = mContext.getResources().getString(R.string.longpress_scan);
            canvas.drawText(text, x, y, paint);
        }
    }

    private void drawRQCodeBmpLeft(Canvas canvas, Bitmap qrCodeBmp) {
        if (qrCodeBmp != null) {
            int ew = qrCodeBmp.getWidth();
            int qrCodeW = (int) ((float)mScoreRect.width() / 2);
            int qrCodeH = qrCodeW;
            float qrCodeScale =  (float)qrCodeW / ew;
            Log.d(Log.TAG, "qrCodeW : " + qrCodeW);
            mQRCodeRect = new RectF();
            mQRCodeRect.left = mScoreRect.left + margin;
            mQRCodeRect.top = mScoreRect.bottom - qrCodeH - margin;
            mQRCodeRect.right = mScoreRect.left + qrCodeW;
            mQRCodeRect.bottom = mScoreRect.bottom - margin;
            float dx = mQRCodeRect.left;
            float dy = mQRCodeRect.top;

            Matrix matrix = new Matrix();
            matrix.setScale(qrCodeScale, qrCodeScale);
            canvas.save();
            canvas.translate(dx, dy);
            canvas.drawBitmap(qrCodeBmp, matrix, null);
            canvas.restore();
            qrCodeBmp.recycle();
            qrCodeBmp = null;
            System.gc();
            float x = mScoreRect.left + margin;
            float y = mScoreRect.bottom - dp2px(mContext, 3);
            Paint paint = new Paint();
            paint.setTextSize(margin - dp2px(mContext, 4));
            paint.setColor(Color.WHITE);
            paint.setAntiAlias(true);
            String text = mContext.getResources().getString(R.string.longpress_scan);
            canvas.drawText(text, x, y, paint);
        }
    }

    private void drawFooter(Canvas canvas, Bitmap footerBmp) {
        if (footerBmp != null) {
            int footerW = footerBmp.getWidth();
            int footerH = footerBmp.getHeight();
            int logoHeight = mScreenHeight / FACTION;
            float scale = (float) logoHeight / footerH;
            footerW = (int) (scale * footerW);
            float dx = (mScreenWidth - footerW) / 2;
            float dy = mScreenHeight - mScreenHeight / FACTION;
            mFooterRect = new RectF(dx, 0, dx + footerW, logoHeight);
            Matrix matrix = new Matrix();
            matrix.setScale(scale, scale);
            canvas.save();
            canvas.translate(dx, dy);
            canvas.drawBitmap(footerBmp, matrix, null);
            canvas.restore();
            footerBmp.recycle();
            footerBmp = null;
            System.gc();
        }
    }

    public int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private Bitmap decodeResources(int resId) {
        try {
            InputStream is = mContext.getResources().openRawResource(resId);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            is.close();
            return bitmap;
        }catch(Exception e) {
            Log.d(Log.TAG, "error : " + e);
        }
        return null;
    }
}
